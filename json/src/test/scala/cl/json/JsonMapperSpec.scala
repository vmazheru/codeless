package cl.json

import java.time.LocalDateTime

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers

import scala.beans.BeanProperty

import cl.core.configurable.ConfigurableException
import cl.json.JsonMapper.JsonMapperException

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class JsonMapperSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "JSON mapper"
  
  it can "serialize/deserialize objects to/from JSON (by setting target object fields directly)" in {
    test(JsonMapper.getJsonMapper, scalaPerson, classOf[ScalaPerson])
  }
  
  it can "serialize/deserialize objects to/from JSON (by calling setters/getters), when visibility is set to METHOD" in {
    val mapper = JsonMapper.getJsonMapper(false).`with`(JsonMapper.visibility, JsonMapper.Visibility.METHOD).locked
    test(mapper, javaPerson, classOf[JavaPerson])
  }  
  
  it must "fail when its configuration is not locked" in {
    intercept[ConfigurableException] {
      JsonMapper.getJsonMapper(false).toJson(new ScalaPerson)
    }
    intercept[ConfigurableException] {
      JsonMapper.getJsonMapper(false).fromJson("{}", classOf[ScalaPerson])
    }
  }
  
  it must "NOT fail on unknown properties by default" in {
    val p = new ScalaPersonWithEyes("John Smith", LocalDateTime.now(),
        new ScalaAddress("123 Main Street", "North Plains", 12345), Array(7), "Red")
    test(JsonMapper.getJsonMapper, p, classOf[ScalaPerson]) // no failure on "eyeColor" property
  }
  
  it must "fail on unknown properties when 'failOnUnknownProperties' attributes is set to TRUE" in {
    val p = new ScalaPersonWithEyes("John Smith", LocalDateTime.now(),
        new ScalaAddress("123 Main Street", "North Plains", 12345), Array(7), "Red")
    val mapper = JsonMapper.getJsonMapper(false).`with`(JsonMapper.failOnUnknownProperties, java.lang.Boolean.TRUE).locked
    val json = mapper.toJson(p)
    intercept[JsonMapperException] { mapper.fromJson(json, classOf[ScalaPerson]) } // failure on "eyeColor" property
  }
  
  it must "print json in one line when pretty printing is not enabled (which is by default)" in {
    test(JsonMapper.getJsonMapper, scalaPerson, classOf[ScalaPerson], json => json should not contain ('\n')) 
  }
  
  it must "print json in multiple line swhen pretty printing is enabled" in {
    val mapper = JsonMapper.getJsonMapper(false).`with`(JsonMapper.prettyPrinting, java.lang.Boolean.TRUE).locked
    test(mapper, scalaPerson, classOf[ScalaPerson], json => json should contain ('\n'))
  }
  
  it must "NOT wrap JSON into a top-level (root) element by default" in {
    test (JsonMapper.getJsonMapper, scalaPerson, classOf[ScalaPerson], json => json should not contain ("ScalaPerson")) 
  }
  
  it must "wrap JSON into a top-level (root) element when 'wrapRootValue' is set to TRUE" in {
    val mapper = JsonMapper.getJsonMapper(false).`with`(JsonMapper.wrapRootValue, java.lang.Boolean.TRUE).locked
    test (mapper, scalaPerson, classOf[ScalaPerson], json => json.contains("ScalaPerson") should be (true))
  }
  
  it must "NOT unwrap single-value arrays by default" in {
    test (JsonMapper.getJsonMapper, scalaPerson, classOf[ScalaPerson], json => json.contains("\"luckyNumbers\":[7]") should be (true))
  }
  
  it must "unwrap single-value arrays when 'unwrapSingleElementArrays' is set to TRUE" in {
    val mapper = JsonMapper.getJsonMapper(false).`with`(JsonMapper.unwrapSingleElementArrays, java.lang.Boolean.TRUE).locked
    test (mapper, scalaPerson, classOf[ScalaPerson], json => json.contains("\"luckyNumbers\":7") should be (true))
  }
  
  it must "NOT print map null values" in {
    scalaPerson.addSkill("cooking", 6)
    scalaPerson.addSkill("jogging", null.asInstanceOf[java.lang.Integer])
    test (JsonMapper.getJsonMapper, scalaPerson, classOf[ScalaPerson], json => {
        json.contains("jogging") should be (false)
        json.contains("cooking") should be (true)
      })
  }
  
  private def test[T](mapper: JsonMapper, obj: Object, klass: Class[T], jsonCheck: String => Unit) {
    val json = mapper.toJson(obj)
    if (jsonCheck != null) jsonCheck(json)
    val fromJson = mapper.fromJson(json, klass)
    fromJson should equal (obj)
  }
  
  private def test[T](mapper: JsonMapper, obj: Object, klass: Class[T]) { test(mapper, obj, klass, null) }

  private val scalaPerson = new ScalaPerson("John Smith", LocalDateTime.now(),
      new ScalaAddress("123 Main Street", "North Plains", 12345), Array(7))
  
  private val javaPerson = new JavaPerson("John Smith", LocalDateTime.now(),
      new JavaAddress("123 Main Street", "North Plains", 12345))

}

class ScalaPerson(
    val name: String,
    val dob: LocalDateTime,
    val address: ScalaAddress,
    val luckyNumbers: Array[Int]) {
  def this() { this (null, null, null, null) }

  private val skills: java.util.Map[String, Integer] = new java.util.HashMap()
  
  override def equals(o: Any): Boolean = {
    val other = o.asInstanceOf[ScalaPerson]
    name == other.name && dob == other.dob && address == other.address && luckyNumbers(0) == other.luckyNumbers(0)
  }
  
  def addSkill(name: String, level: Integer) = skills.put(name, level)
}

class JavaPerson(
    @BeanProperty var name: String,
    @BeanProperty var dob: LocalDateTime,
    @BeanProperty var address: JavaAddress) {
  def this() { this (null, null, null) }
  
  override def equals(o: Any): Boolean = {
    val other = o.asInstanceOf[JavaPerson]
    name == other.name && dob == other.dob && address == other.address
  }
}

class ScalaPersonWithEyes (
    override val name: String,
    override val dob: LocalDateTime,
    override val address: ScalaAddress,
    override val luckyNumbers: Array[Int],
    val eyeColor: String) extends ScalaPerson

class ScalaAddress(
    val street: String,
    val city: String,
    val zip: Int) {
  def this() { this(null, null, null.asInstanceOf[Int]) } 
  
  override def equals(o: Any): Boolean = {
    val other = o.asInstanceOf[ScalaAddress]
    street == other.street && city == other.city && zip == other.zip
  }
}

class JavaAddress(
    @BeanProperty var street: String,
    @BeanProperty var city: String,
    @BeanProperty var zip: Int) {
  def this() { this(null, null, null.asInstanceOf[Int]) } 
  
  override def equals(o: Any): Boolean = {
    val other = o.asInstanceOf[JavaAddress]
    street == other.street && city == other.city && zip == other.zip
  }
}

    
