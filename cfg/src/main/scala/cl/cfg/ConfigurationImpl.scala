package cl.cfg

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

import cl.cfg.Configuration.ConfigurationException
import cl.core.function.ScalaToJava.toConsumer
import cl.core.lang.Control.using

protected object CfgImpl {
  
  private val LIST_FILE     = "cl.cfg.txt";
  private val PROP_XML_FILE = "cl.cfg.xml";
  private val PROP_TXT_FILE = "cl.cfg.properties";

  private var cfg: CfgImpl = null 

  def instance() =
    this.synchronized({
      if (cfg == null) {
        cfg = new CfgImpl
        
        val listFile = getClass().getClassLoader().getResourceAsStream(LIST_FILE)
        if (listFile != null) {
          using(new BufferedReader(new InputStreamReader(listFile))) { reader =>
            reader.lines().forEach((line: String) => {
              cfg.merge(new CfgImpl(line)) // we want to report missing file in the listing
              () // no return value
            })
          }
        }
        try {
          cfg.merge(new CfgImpl(PROP_TXT_FILE)).merge(new CfgImpl(PROP_XML_FILE))
        } catch {
          case _: ConfigurationException => // but we don't care if there is no predefined property file in the app
        }
      }
      cfg
    })
  
  def load(fileName: String) =
    this.synchronized({
      instance.merge(new CfgImpl(fileName))
    })
}

protected class CfgImpl(val fileName: String) extends Configuration {

  val props = if (fileName == null) new Properties else loadProperties(fileName)

  def this() {
    this(null)
  }

  override def getString(key: String): String = get(key)
  override def getInt(key: String): Int = check(getString(key), _.toInt)
  override def getDouble(key: String): Double = check(getString(key), _.toDouble)
  override def getBoolean(key: String): Boolean = check(getString(key), _.toBoolean)
  override def getStringArray(key: String): Array[String] = check(getString(key), split)
  override def getIntArray(key: String): Array[Int] = check(getString(key), split(_).map(_.toInt))
  override def getDoubleArray(key: String): Array[Double] = check(getString(key), split(_).map(_.toDouble))

  override def getString(key: String, default: String): String = opt(() => getString(key), default)
  override def getInt(key: String, default: Int): Int = opt(() => getInt(key), default)
  override def getDouble(key: String, default: Double): Double = opt(() => getDouble(key), default)
  override def getBoolean(key: String, default: Boolean): Boolean = opt(() => getBoolean(key), default)
  override def getStringArray(key: String, default: Array[String]): Array[String] = opt(() => getStringArray(key), default)
  override def getIntArray(key: String, default: Array[Int]): Array[Int] = opt(() => getIntArray(key), default)
  override def getDoubleArray(key: String, default: Array[Double]): Array[Double] = opt(() => getDoubleArray(key), default)
  
  private def check[T](value: String, f: String => T) =
    try {
      f(value)
    } catch {
      case e: Exception => throw new ConfigurationException(e.getMessage)
    }

  private def get(key: String) = {
    val v = props.get(key)
    if (v == null) throw new ConfigurationException("property not found by key " + key)
    v.asInstanceOf[String]
  }

  private def opt[T](f: () => T, default: T) =
    try {
      f()
    } catch {
      case e: ConfigurationException => default
    }

  private def split(s: String) = s.split(",")

  private def loadProperties(fileName: String) = {
    val file = new File(fileName)
    val is = if (file.exists()) new FileInputStream(file)
             else getClass().getClassLoader().getResourceAsStream(fileName)
    if (is == null) throw new ConfigurationException("file " + fileName + " not found")
    val isXml = fileName.toLowerCase.endsWith(".xml")
    val p = new Properties
    using(is) { in => if (isXml) p.loadFromXML(in) else p.load(in) }
    p
  }
  
  private def merge(other: CfgImpl) = {
    props.putAll(other.props)
    this
  }
}