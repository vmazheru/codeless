package cl.files.serializers

import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import cl.core.lang.Control.using
import cl.files.serializers.Serializer._
import cl.files.serializers.SerializersTestSupport._
import cl.files.Person
import cl.files.serializers.iterators.JavaIterator

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class JavaSerializerConfigurationSpec extends FlatSpec with Matchers {
  
  behavior of "java serializer"
  
  it should "respect it's configuration settings" in {
    /*
     * There is no configuration for java serializer, so, the test below just reads and writes objects.
     */
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, Person](src, dest, SerializationType.JAVA)) { serializer =>
        serializer.getWriter.write(serializer.getIterator.read())
      }
      JavaIterator.fromFile[Person](dest).read() should equal(Person.peopleDB())
    }
  }
  
}