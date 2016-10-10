package cl.files.serializers

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import cl.core.configurable.Configurable
import cl.core.function.ScalaToJava._
import cl.core.lang.Control.using
import cl.files.serializers.Serializer._
import cl.files.serializers.SerializersTestSupport._
import cl.files.Person
import java.util.stream.Collectors
import java.nio.charset.Charset
import cl.files.serializers.iterators.StringIterator
import java.io.BufferedReader
import java.io.FileReader

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class StringSerializerConfigurationSpec extends FlatSpec with Matchers {
  
  behavior of "string serializer"
  
  it should "respect 'skip empty lines' configuration setting" in {
    
    // the default setting is to skip empty lines
    // and the serializer will read the entire file with no problem
    withFiles(stringInputFileWithEmptyLines, newFile) { (src, dest) =>
      using(serializer(src, dest, SerializationType.STRING)) { serializer =>
        serializer.getIterator.read
      } should equal (Person.peopleDBStrings())
    }

    // when the setting is not to skip empty lines,
    // the resulting collection should have all empty lines 
    withFiles(stringInputFileWithEmptyLines, newFile) { (src, dest) =>
      import scala.language.existentials
      val config = Configurable.empty().`with`[java.lang.Boolean](SerializerConfiguration.skipEmptyLines, false).locked
      
      // the expected result is to have an empty string after every person
      val expected = Person.peopleDBStrings().stream()
        .flatMap((s: String) => java.util.stream.Stream.of(s, "")).collect(Collectors.toList())
      
      using(serializer(src, dest, SerializationType.STRING, config)) { serializer =>
        serializer.getIterator.read
      } should equal (expected)
    }    
  }
  
  it should "read/write a file with specified encoding" in {
    
    withFiles(stringInputFileInRussian, newFile) { (src, dest) =>
      import scala.language.existentials
      val config = Configurable.empty().`with`(SerializerConfiguration.charset, Charset.forName("Windows-1251")).locked
      
      // verify the read() works with the file written by some other process with encoding Windows-1251
      using(serializer[Person, Person](src, dest, SerializationType.STRING, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDBStringsInRussian())
        
        // write the file with this encoding
        serializer.getWriter.write(data)
      }
      
      // read again from the destination file to verify that write() actually worked
      using(serializer(dest, newFile, SerializationType.STRING, classOf[Person], config)) { serializer =>
        serializer.getIterator.read should equal (Person.peopleDBStringsInRussian())
      }
      
      // try to read with default encoding (UTF-8).  It should fail
      using(serializer(dest, newFile, SerializationType.STRING, classOf[Person])) { serializer =>
        serializer.getIterator.read should not equal (Person.peopleDBStringsInRussian())
      }
    }
  
  }
  
  it should "copy one or more header lines, if present in the input file, to the output file" in {
    import scala.language.existentials
    val config = Configurable.empty().`with`[java.lang.Integer](SerializerConfiguration.numHeaderLines, 3).locked
    
    withFiles(stringInputFileWithHeader, newFile) { (src, dest) =>
      using(serializer[Person, Person](src, dest, SerializationType.STRING, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDBStrings)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (StringIterator.fromFile(dest, false).withConfigurationFrom(config).locked) { iter => 
        iter.read() should equal (Person.peopleDBStrings())
      }
      
      // verify that header is written to destination and lines are in correct order
      using (new BufferedReader(new FileReader(dest))) { reader => 
        reader.readLine() should equal ("Header 1")
        reader.readLine() should equal ("Header 2")
        reader.readLine() should equal ("Header 3")
      }
    }
  }
  
}