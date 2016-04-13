package cl.cfg

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.GivenWhenThen
import org.scalatest.FlatSpec
import java.nio.file.Files
import java.util.Arrays
import cl.cfg.Configuration.ConfigurationException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Configuration interface specification
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ConfigurationSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "Configuration"
  
  it can "load properties from a predefined list of property files " in {
    Configuration.getConfiguration().getString("one") should equal ("one")
    Configuration.getConfiguration().getString("two") should equal ("two")
  }
  
  it can "be loaded from a predefined property file (cl.cfg.properties)" in {
    Configuration.getConfiguration().getInt("int_property") should be (1)
  }
  
  it can "be loaded from a predefined XML property file (cl.cfg.xml)" in {
    Configuration.getConfiguration().getInt("another_int_property") should be (43)
  }
  
  Given("that both default propery files (.properties and .xml) are present")
  it should "load both files and XML file properties will take precedence" in {
    Configuration.getConfiguration().getDouble("double_property") should be (-1.0)
  }
  
  it can "be loaded from a single file in classpath. Latest properties override existing." in {
    Configuration.getConfiguration("cfg_test.properties").getInt("int_property") should be (42)
  }
  
  it can "be loaded from a single file given its path. Latest properties override existing." in {
    val file = Files.createTempFile("temp", ".properties")
    Files.write(file, Arrays.asList("int_property=47"))
    Configuration.getConfiguration(file.toString()).getInt("int_property") should be (47)
  }
  
  it should "return values of different types for configured properties" in {
    Configuration.getConfiguration().getString("string_property") should equal ("Hello, World")
    Configuration.getConfiguration().getInt("int_property") should equal (47)
    Configuration.getConfiguration().getDouble("double_property") should equal (3.14)
    Configuration.getConfiguration().getBoolean("boolean_property") should be (true)

    Configuration.getConfiguration().getStringArray("string_array_property1") should equal (Array("1"))
    Configuration.getConfiguration().getStringArray("string_array_property2") should equal (Array("1","2","3"))
    Configuration.getConfiguration().getIntArray("int_array_property1") should equal (Array(1))
    Configuration.getConfiguration().getIntArray("int_array_property2") should equal (Array(1,2,3))
    Configuration.getConfiguration().getDoubleArray("double_array_property1") should equal (Array(1.1))
    Configuration.getConfiguration().getDoubleArray("double_array_property2") should equal (Array(1.1,2.2,3.3))
  }
  
  it should "prefer configured property values over default values" in {
    Configuration.getConfiguration().getString("string_property", "bye") should equal ("Hello, World")
    Configuration.getConfiguration().getInt("int_property", 666) should equal (47)
    Configuration.getConfiguration().getDouble("double_property", 1.0) should equal (3.14)
    Configuration.getConfiguration().getBoolean("boolean_property", false) should be (true)
    
    Configuration.getConfiguration().getStringArray("string_array_property2", Array("1")) should equal (Array("1","2","3"))
    Configuration.getConfiguration().getIntArray("int_array_property2", Array(1)) should equal (Array(1,2,3))
    Configuration.getConfiguration().getDoubleArray("double_array_property2", Array(1.1)) should equal (Array(1.1,2.2,3.3))
  }
  
  it should "throw ConfigurationException on accessing unknown properties without default values" in {
    intercept[ConfigurationException] { Configuration.getConfiguration().getString("bad_string_property") }
    intercept[ConfigurationException] { Configuration.getConfiguration().getInt("bad_int_property") }
    intercept[ConfigurationException] { Configuration.getConfiguration().getDouble("bad_double_property") }
    intercept[ConfigurationException] { Configuration.getConfiguration().getBoolean("bad_boolean_property") }
    
    intercept[ConfigurationException] { Configuration.getConfiguration().getStringArray("bad_string_array_property2") }
    intercept[ConfigurationException] { Configuration.getConfiguration().getIntArray("bad_int_array_property2") }
    intercept[ConfigurationException] { Configuration.getConfiguration().getDoubleArray("bad_double_array_property2") }
  }
  
  it should "return default values on accessing unknown properties with default values" in {
    Configuration.getConfiguration().getString("bad_string_property", "bye") should equal ("bye")
    Configuration.getConfiguration().getInt("bad_int_property", 666) should equal (666)
    Configuration.getConfiguration().getDouble("bad_double_property", 1.0) should equal (1.0)
    Configuration.getConfiguration().getBoolean("bad_boolean_property", false) should be (false)
    
    Configuration.getConfiguration().getStringArray("bad_string_array_property2", Array("1")) should equal (Array("1"))
    Configuration.getConfiguration().getIntArray("bad_int_array_property2", Array(1)) should equal (Array(1))
    Configuration.getConfiguration().getDoubleArray("bad_double_array_property2", Array(1.1)) should equal (Array(1.1))
  }
  
  it should "return default values, if given, when value parsing errors happen" in {
    val c = Configuration.getConfiguration("bad.properties")
    
    Configuration.getConfiguration().getInt("int_property", 777) should equal (777)
    Configuration.getConfiguration().getDouble("double_property", 2.0) should equal (2.0)
    Configuration.getConfiguration().getBoolean("boolean_property", false) should be (false)
    
    Configuration.getConfiguration().getIntArray("int_array_property1", Array(3,2,1)) should equal (Array(3,2,1))
    Configuration.getConfiguration().getDoubleArray("double_array_property1", Array(2.2)) should equal (Array(2.2))
  }
  
}