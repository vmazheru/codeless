package cl.files.serializers

import java.nio.charset.Charset

import scala.language.existentials

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import cl.core.configurable.Configurable
import cl.core.lang.Control.using
import cl.files.Person
import cl.files.serializers.Serializer.SerializerBuildException
import cl.files.serializers.Serializer.serializer
import cl.files.serializers.SerializersTestSupport.jsonInputFile
import cl.files.serializers.SerializersTestSupport.jsonInputFileRussian
import cl.files.serializers.SerializersTestSupport.jsonInputFileWithEmptyLines
import cl.files.serializers.SerializersTestSupport.newFile
import cl.files.serializers.SerializersTestSupport.withFiles
import cl.json.JsonMapper

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class JsonSerializerConfigurationSpec extends FlatSpec with Matchers {
  
  behavior of "JSON serializer"
  
  it should "always skip empty lines and ignore 'skip empty lines' configuration setting" in {
  
    withFiles(jsonInputFileWithEmptyLines, newFile) { (src, dest) =>
      import scala.language.existentials
      val config = Configurable.empty().`with`[java.lang.Boolean](SerializerConfiguration.skipEmptyLines, false).locked
      using(serializer(src, dest, SerializationType.JSON, classOf[Person], config)) { serializer =>
        serializer.getIterator.read
      } should equal (Person.peopleDB())
    }
    
  }
  
  it should "read/write a file with specified encoding" in {
    
    withFiles(jsonInputFileRussian, newFile) { (src, dest) =>
      import scala.language.existentials
      val config = Configurable.empty().`with`(SerializerConfiguration.charset, Charset.forName("Windows-1251")).locked
      
      // verify the read() works with the file written by some other process with encoding Windows-1251
      using(serializer[Person, Person](src, dest, SerializationType.JSON, classOf[Person], config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDBInRussian())
        
        // write the file with this encoding
        serializer.getWriter.write(data)
      }
      
      // read again from the destination file to verify that write() actually worked
      using(serializer(dest, newFile, SerializationType.JSON, classOf[Person], config)) { serializer =>
        serializer.getIterator.read should equal (Person.peopleDBInRussian())
      }
      
      // try to read with default encoding (UTF-8).  It should fail
      using(serializer(dest, newFile, SerializationType.JSON, classOf[Person])) { serializer =>
        serializer.getIterator.read should not equal (Person.peopleDBInRussian())
      }
    }
  
  }
  
  it should "serialize / deserialize objects with the same given JSON mapper" in {
    
    // some non-standard JSON mapper
    val jsonMapper = JsonMapper.getJsonMapper(false).`with`[java.lang.Boolean](JsonMapper.wrapRootValue, true).locked
    import scala.language.existentials
    val config = Configurable.empty().`with`(SerializerConfiguration.jsonMapper, jsonMapper).locked
    
    withFiles(jsonInputFile(jsonMapper), newFile) { (src, dest) =>
      using(serializer[Person, Person](src, dest, SerializationType.JSON, classOf[Person], config)) { serializer =>
        // read data with non-standard JSON mapper
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB())
        
        // write data with the same mapper
        serializer.getWriter.write(data)
      }
      
      // read again to verify that the writer did it right
      using(serializer(dest, newFile, SerializationType.JSON, classOf[Person], config)) { serializer =>
        serializer.getIterator.read should equal (Person.peopleDB())
      }
      
      // try to read the file with the default JSON mapper, it should fail (default mapper create 
      // not populated objects)
      using(serializer(dest, newFile, SerializationType.JSON, classOf[Person])) { serializer =>
        serializer.getIterator.read should not equal (Person.peopleDB())
      }
    }
    
  }
  
  it should "throw an exception when iterator class is not set" in {
    a [SerializerBuildException] should be thrownBy {
      serializer(newFile, newFile, SerializationType.JSON)
    }
  }
  
}