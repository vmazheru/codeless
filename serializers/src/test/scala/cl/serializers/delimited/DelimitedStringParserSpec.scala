package cl.serializers.delimited

import java.text.SimpleDateFormat
import java.util.HashMap

import scala.beans.BeanProperty

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import DelimitedStringParser._
import cl.core.function.ScalaToJava._
import org.junit.runner.RunWith
import cl.core.function.FunctionWithException
import cl.serializers.delimited.DelimitedStringParser.PropertySetException
import cl.core.ds.Counter
import cl.core.configurable.ConfigurableException

/**
 * Specification for DelimitedStringParser
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringParserSpec extends FlatSpec with Matchers {
  
  behavior of "delimited string parser"

  //////////////// test automatic value setting ////////////////
  
  it should "be able to set properties of common types automatically" in {
    val parser = get(classOf[AllTypesTest], iToP(
        0 -> "string",
        1 -> "bytePrim",
        2 -> "byteObj",
        3 -> "shortPrim",
        4 -> "shortObj",
        5 -> "intPrim",
        6 -> "intObj",
        7 -> "longPrim",
        8 -> "longObj",
        9 -> "floatPrim",
        10 -> "floatObj",
        11 -> "doublePrim",
        12 -> "doubleObj",
        13 -> "boolPrim",
        14 -> "boolObj",
        15 -> "charPrim",
        16 -> "charObj",
        17 -> "bigInteger",
        18 -> "bigDecimal",
        19 -> "date",
        20 -> "localDate",
        21 -> "localTime",
        22 -> "localDateTime",
        23 -> "zonedDateTime"
    ))
    
    val d: java.util.Date = new java.util.Date
    val ld: java.time.LocalDate = java.time.LocalDate.now
    val lt: java.time.LocalTime = java.time.LocalTime.now
    val ldt: java.time.LocalDateTime = java.time.LocalDateTime.now
    val zdt: java.time.ZonedDateTime = java.time.ZonedDateTime.now
    
    val result = parser.parse(Array(
        "Hello, World!",
        "1",              // byte
        "2",              // Byte
        "111",            // short
        "222",            // Short
        "12345",          // int
        "23456",          // Integer
        "123456789",      // long
        "234567890",      // Long
        "12345.678",      // float
        "23456.789",      // Float
        "1.2222222",      // double
        "2.3333333",      // Double 
        "true",           // boolean
        "false",          // Boolean
        "a",              // char
        "B",              // Character
        "1234567890",     // BigInteger
        "1234567.897654", // BigDecimal
        d.toString,       // Date
        ld.toString,      // LocalDate
        lt.toString,      // LocalTime
        ldt.toString,     // LocalDateTime
        zdt.toString      // ZonedDateTime
    ))
    
    result.string should be ("Hello, World!")
    result.bytePrim should be (1)
    result.byteObj should be (2)
    result.shortPrim should be (111)
    result.shortObj should be (222)
    result.intPrim should be (12345)
    result.intObj should be (23456)
    result.longPrim should be (123456789)
    result.longObj should be (234567890)
    val epsilon = 1e-3f
    Math.abs(result.floatPrim - 12345.678) < (epsilon) should be (true)
    Math.abs(result.floatObj - 23456.789)  < (epsilon) should be (true)
    Math.abs(result.doublePrim - 1.2222222) < (epsilon) should be (true)
    Math.abs(result.doubleObj - 2.3333333)  < (epsilon) should be (true)
    result.boolPrim should be (true)
    result.boolObj should be (java.lang.Boolean.FALSE)
    result.charPrim should be ('a')
    result.charObj should be ('B')
    result.bigInteger should be (new java.math.BigInteger("1234567890"))
    result.bigDecimal should be (new java.math.BigDecimal("1234567.897654"))
    result.date.toString should be (d.toString)
    result.localDate should be (ld)
    result.localTime should be (lt)
    result.localDateTime should be (ldt)
    result.zonedDateTime should be (zdt)
  }
  
  
  ///////////////// test value parsers /////////////////////
  
  it should "execute a value parser for a property when it is given" in {
    class Test {
      var s: String = null
      var d: java.util.Date = null
    }
    
    // provide a parser only for the Date field.  The String filed still
    // must be set
    val parser = get(() => new Test,
        iToP(0 -> "s", 1 -> "d"),
        pToParser("d" -> ((s: String) => new SimpleDateFormat("yyyy-MM-dd").parse(s))))
        
    val result = parser.parse(Array("Hello, World!", "2017-06-05"))
    result.s should be ("Hello, World!")
    result.d should be (new SimpleDateFormat("yyyy-MM-dd").parse("2017-06-05"))
  }
  
  
  ////////////////// tests useSetters ///////////////////
  
  it should "use or not use setters when instructed" in {
    class Test {
      var setterCalls = 0
      var stringProp: String = null
      def setStringProp(prop: String) { 
        stringProp = prop
        setterCalls += 1
      }
    }
    
    // test with useSetters = true
    val parser1 = get(() => new Test, iToP(0 -> "stringProp"), false)
        .`with`(useSetters, java.lang.Boolean.TRUE).locked
    
    val result1 = parser1.parse(Array("Hello, World!"))
    result1.stringProp should be ("Hello, World!")
    result1.setterCalls should be (1)
    
    // test with useSetters = false
    val parser2 = get(() => new Test, iToP(0 -> "stringProp"))
    val result2 = parser2.parse(Array("Hello, World!"))
    result2.stringProp should be ("Hello, World!")
    result2.setterCalls should be (0)
  }
  
  it should "fail when trying to use setters and no value parser is given for types other than String" in {
    class Test {
      @BeanProperty var intProp = 0
    }
    
    val ex = intercept[RuntimeException] {    
      get(() => new Test, iToP(0 -> "intProp"), false)
          .`with`(useSetters, java.lang.Boolean.TRUE).locked  
    }
    
    ex.getCause.getClass should be (classOf[NoSuchMethodException])
  }
  
  it should "succeed when trying to use setters and no value parser is given for type String" in {
    class Test {
      @BeanProperty var stringProp: String = null
    }

    val parser = get(() => new Test, iToP(0 -> "stringProp"), false)
        .`with`(useSetters, java.lang.Boolean.TRUE).locked  
    
    val result = parser.parse(Array("Hello, World!"))
    result.stringProp should be ("Hello, World!")
  }
  
  
  ////////////// test error handling (onPropertyError) ///////////
  
  it should "throw an exception when the field type is unknown and there is no value parser given" in {
    class UnknownClass
    
    class Test {
      var s: UnknownClass = null
    }
    
    intercept[RuntimeException] {
        get(() => new Test, iToP(0 -> "s")).parse(Array("value for Unknown class"))
    }
  }
  
  it should "throw an exception when the value parser throws an error" in {
    class Test {
      var d: java.util.Date = null
      
      intercept[RuntimeException] {
        val parser = get(() => new Test, iToP(0 -> "d"),
          pToParser("d" -> ((s: String) => new SimpleDateFormat("yyyy-MM-dd").parse(s))))
        parser.parse(Array("bad input"))
      }
    }
  }

  it should "execute onPropertyError callback when a property cannot be set because of an parsing error" in {
    class Test {
      var d: java.util.Date = null
    }
    
    val onErrorExecutedTimes = new Counter()
    
    val onError: java.util.function.Consumer[PropertySetException] =
      (ex: PropertySetException) => {
        onErrorExecutedTimes.increment()
        
        ex.getCause.getClass should be (classOf[java.text.ParseException])
        ex.getObject.getClass should be (classOf[Test])
        ex.getObject[Test]() shouldNot be (null)
        ex.getProperty should be ("d")
        ex.getValue should be ("bad input")
        
        () // return Unit to make it a consumer, not a function
      } 
    
    val parser = get(() => new Test, iToP(0 -> "d"),
        pToParser("d" -> ((s: String) => new SimpleDateFormat("yyyy-MM-dd").parse(s))),
        false).`with`(onPropertySetError, onError).locked
    parser.parse(Array("bad input"))
    
    onErrorExecutedTimes.get should be (1)
  }
  
  
  ////////////// test null input ////////////////
  
  it should "return null when the input is null" in {
    class Test {
      @BeanProperty var stringProp: String = null
    }
    val parser = get(() => new Test, iToP(0 -> "stringProp"))
    parser.parse(null) should be (null)
  }
  
  it should "not set properties for null values in the array" in {
    class Test {
      @BeanProperty var stringProp: String = "some initial value"
    }
    
    val parser = get(() => new Test, iToP(0 -> "stringProp"), false)
        .`with`(useSetters, java.lang.Boolean.TRUE).locked
        
    val result = parser.parse(Array(null))
    result should not be (null)
    result.stringProp should be ("some initial value")
  }
  
  
  //////////////////// test locking //////////////
  
  it should "not execute parse() until it is locked" in {
    class Test {
      @BeanProperty var stringProp: String = null
    }
    
    val parser = get(() => new Test, iToP(0 -> "stringProp"), false)
        .`with`(useSetters, java.lang.Boolean.TRUE)
    
    intercept[ConfigurableException] {
      parser.parse(Array("some value"))
    }
    
    parser.locked.parse(Array("some value")).stringProp should be ("some value") //ok
  }
  
  
  private def iToP(mappings: (Int, String)*) = {
    val map = new HashMap[Integer, String]
    for (m <- mappings) map.put(m._1, m._2)
    map
  }
  
  private def pToParser(mappings: (String, FunctionWithException[String, Object])*) = {
    val map = new HashMap[String, FunctionWithException[String, Object]]
    for (m <- mappings) map.put(m._1, m._2)
    map
  }
  
}

class AllTypesTest {
    var string: String = null
    var bytePrim: Byte = 0
    var byteObj: java.lang.Byte = null
    var shortPrim: Short = 0
    var shortObj: java.lang.Short = null
    var intPrim: Int = 0
    var intObj: Integer = null
    var longPrim: Long = 0
    var longObj: java.lang.Long = null
    var floatPrim: Float = 0
    var floatObj: java.lang.Float = null
    var doublePrim: Double = 0
    var doubleObj: java.lang.Double = null
    var boolPrim: Boolean = false
    var boolObj: java.lang.Boolean = null
    var charPrim: Char = 0
    var charObj: Character = null
    var bigInteger: java.math.BigInteger = null
    var bigDecimal: java.math.BigDecimal = null
    var date: java.util.Date = null
    var localDate: java.time.LocalDate = null
    var localTime: java.time.LocalTime = null
    var localDateTime: java.time.LocalDateTime = null
    var zonedDateTime: java.time.ZonedDateTime = null
}





























































