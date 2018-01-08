package cl.serializers

import java.nio.charset.Charset
import java.io.File

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers

import cl.serializers.SerializersTestSupport._
import cl.core.configurable.Configurable
import cl.json.JsonMapper
import cl.core.function.ScalaToJava._
import cl.core.lang.Control.using
import cl.serializers.Serializer.psvSerializer
import cl.serializers.Serializer.serializer
import scala.io.Source
import cl.serializers.Serializer.SerializerBuildException
import java.io.BufferedReader
import java.io.FileReader


@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringSerializerConfigurationSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "delimited string serializer"
  
  it should "skip empty lines when instructed to do so" in {
    withFiles(psvInputFileWithEmptyLines, newFile) { (src, dest) =>
      val config = Configurable.empty().`with`(SerializerConfiguration.skipEmptyLines, java.lang.Boolean.TRUE).locked
      
      using(getPsvSerializer(src, dest, config)) { serializer =>
        serializer.getIterator.read
      } should equal (Person.peopleDB())
    }
  }
  
  it should "read/write a file with specified encoding" in {
    withFiles(psvInputFileRussian, newFile) { (src, dest) =>
      val config = Configurable.empty().`with`(SerializerConfiguration.charset, Charset.forName("Windows-1251")).locked
      
      // verify the read() works with the file written by some other process with encoding Windows-1251
      using(getPsvSerializer(src, dest, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDBInRussian())
        
        // write the file with this encoding
        serializer.getWriter.write(data)
      }
      
      // read again from the destination file to verify that write() actually worked
      using(getPsvSerializer(dest, newFile, config)) { serializer =>
        serializer.getIterator.read should equal (Person.peopleDBInRussian())
      }
      
      // try to read with default encoding (UTF-8).  It should fail
      using(getPsvSerializer(dest, newFile, Configurable.empty)) { serializer =>
        serializer.getIterator.read should not equal (Person.peopleDBInRussian())
      }
    }
  }
  
  it should "copy one or more header lines, if present in the input file, to the output file" in {
    val config = Configurable.empty().`with`[java.lang.Integer](SerializerConfiguration.numHeaderLines, 3).locked
    
    withFiles(psvInputFileWithAdditionalHeaderLines, newFile) { (src, dest) =>
      using(getPsvSerializer(src, dest, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (getPsvSerializer(src, dest, config)) { serializer => 
        serializer.getIterator.read should equal (Person.peopleDB)
      }
      
      // verify that header is written to destination and lines are in correct order
      using (new BufferedReader(new FileReader(dest))) { reader =>
        reader.readLine() should equal ("name|dob|gender|address")
        reader.readLine() should equal ("Header 1")
        reader.readLine() should equal ("Header 2")
      }
    }
  }  
  
  it should "throw an exception when iterator class is not set" in {
    a [SerializerBuildException] should be thrownBy {
      serializer(newFile, newFile, SerializationType.DELIMITED)
    }
  }
  
  private[this] def getPsvSerializer(
      src: File, dest: File, configuration: Configurable[_]): Serializer[Person, Person] = {
    import scala.collection.JavaConversions.mapAsJavaMap
    
    val jsonMapper = JsonMapper.getJsonMapper
    val valueParsers = new java.util.HashMap[String, java.util.function.Function[String, Object]]
    valueParsers.put("address", (s: String) => jsonMapper.fromJson(s, classOf[Person.Address]))
    
    val addressSerializer: java.util.function.Function[Object, String] = 
      (address: Object) => jsonMapper.toJson(address)
    val valueSerializers: java.util.Map[String, java.util.function.Function[Object, String]] = 
      Map("address" -> addressSerializer)
      
    val config = Configurable.empty().withConfigurationFrom(configuration)
      .`with`(SerializerConfiguration.valueParsers, valueParsers).asInstanceOf[Configurable[_]]
      .`with`(SerializerConfiguration.valueSerializers, valueSerializers).asInstanceOf[Configurable[_]]
      .locked

    psvSerializer(src, dest, classOf[Person], config)
  }
  
}