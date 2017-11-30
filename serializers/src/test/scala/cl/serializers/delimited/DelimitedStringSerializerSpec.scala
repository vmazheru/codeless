package cl.serializers.delimited


import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import cl.core.configurable.ConfigurableException
import java.util.Arrays
import java.util.Collections.emptyMap
import org.scalatest.GivenWhenThen
import scala.collection.JavaConversions.mapAsJavaMap
import cl.serializers.delimited.DelimitedStringSerializer._
import java.math.BigInteger

/**
 * Specification for a delimited string serializer.
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringSerializerSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "delimited string serializer"

  ////////////// test 'index to property' and 'exact properties' //////////////////////
  
  Given ("a test object with four fields one of which is not initialized")
  case class Test (i: Int, eng: String, ita: String, other: String)
  val testObj = new Test(1, "one", "uno", null)
  
  it should "place values in the order specified by index to property map" in {
    val indexToProperty = Map(new Integer(0) -> "eng", new Integer(1) -> "i")
    val s = DelimitedStringSerializer.get[Test](indexToProperty)
    s.serialize(testObj) should be (Array("one", "1", "uno", ""))
  }
  
  it should "ignore values that are not in the index to property map when exactProperties set to TRUE" in {
    val indexToProperty = Map(new Integer(0) -> "eng", new Integer(1) -> "i")
    val s = DelimitedStringSerializer.get[Test](indexToProperty, false)
      .`with`(exactProperties, java.lang.Boolean.TRUE).locked()
    s.serialize(testObj) should be (Array("one", "1"))
  }
  
  it should "output all fields (including nulls) when no index to property map is given" in {
    val s = DelimitedStringSerializer.get[Test]()
    s.serialize(testObj) should be (Array("1", "one", "uno", ""))
  }
  
  it should "output no fields when no index to property map is given and exactProperties set to TRUE" in {
    val s = DelimitedStringSerializer.get[Test](false)
      .`with`(exactProperties, java.lang.Boolean.TRUE).locked()
    s.serialize(testObj) should be (Array())
  }
  
  ////////////// test use getters ///////////////
  
  it should "use getters when instructed to do so" in {
    class Test (i: Int, eng: String, ita: String, other: String) {
      def getNumber = i
      def getEnglish = eng
      def getItalian = ita
    }
    
    val indexToProperty = Map(
        new Integer(0) -> "english",
        new Integer(1) -> "italian",
        new Integer(2) -> "number")
    
    val s = DelimitedStringSerializer.get[Test](indexToProperty, false)
      .`with`(useGetters, java.lang.Boolean.TRUE).locked()
    
    s.serialize(new Test(1, "one", "uno", null)) should be (Array("one", "uno", "1"))
  }
  
  it should "still obey 'exact properties' setting when using getters" in {
    class Test (i: Int, eng: String, ita: String, other: String) {
      def getNumber = i
      def getEnglish = eng
      def getItalian = ita
    }
    
    val indexToProperty = Map(
        new Integer(0) -> "english",
        new Integer(1) -> "italian")
    
    val s = DelimitedStringSerializer.get[Test](indexToProperty, false)
      .`with`(useGetters, java.lang.Boolean.TRUE)
      .`with`(exactProperties, java.lang.Boolean.TRUE).locked()
    
    s.serialize(new Test(1, "one", "uno", null)) should be (Array("one", "uno"))
  }
  
  ////////////// test value serializers /////////
  
  it should "apply custom value serializers" in {
    import cl.core.function.ScalaToJava._
    val upper: java.util.function.Function[Object, String] = (obj: Object) => obj.toString.toUpperCase
    
    val valueSerializers: java.util.Map[String, java.util.function.Function[Object, String]] = 
      Map("eng" -> upper, "ita" -> upper)
    
    val s = DelimitedStringSerializer.get[Test](Map[Integer, String](), valueSerializers)
    s.serialize(new Test(1, "one", "uno", null)) should be (Array("1", "ONE", "UNO", ""))
  }
  
  ////////////// test null input ////////////////
  
  it should "return null on null input" in {
    DelimitedStringSerializer.get().serialize(null) should be (null)
  }
  
  //////////////////// test locking //////////////
  
  it should "not execute serialize() until it's locked" in {
    intercept[ConfigurableException] {
      DelimitedStringSerializer.get(false).serialize(null)
    }
  }
}