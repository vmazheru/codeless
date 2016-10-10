package cl.files.serializers

import scala.util.Random
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import cl.core.lang.Control.using
import cl.core.function.ScalaToJava._
import cl.files.Person
import cl.files.serializers.Serializer._
import cl.files.serializers.SerializersTestSupport._
import cl.files.serializers.iterators.JavaIterator
import cl.files.serializers.iterators.JsonIterator
import cl.files.serializers.iterators.ObjectIterator
import cl.files.serializers.writers.JavaWriter
import cl.files.serializers.writers.JsonWriter
import cl.files.serializers.writers.ObjectWriter
import java.io.File
import cl.files.serializers.iterators.StringIterator
import cl.files.serializers.writers.StringWriter
import java.util.Optional
import cl.core.configurable.Configurable
import cl.files.Person.Gender
import java.util.stream.Collectors
import java.util.Objects
import cl.core.ds.Counter

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class SerializerSpec extends FlatSpec with Matchers {
  
  behavior of "serializer"
  
  it should "write / read  very large files without a problem" in {
    val numObjects = 1000000 //1 million

    withFile(newFile) { file =>
      testSerializer(Person.peopleDB(), numObjects, file,
        JavaWriter.toFile(file), JavaIterator.fromFile(file),SerializationType.JAVA)
    }
    
    withFile(newFile) { file =>
      testSerializer(Person.peopleDB(), numObjects, file,
        JsonWriter.toFile(file), JsonIterator.fromFile(file, classOf[Person]),SerializationType.JSON)
    }
    
    withFile(newFile) { file =>
      testSerializer(Person.peopleDBStrings(), numObjects, file,
        StringWriter.toFile(file), StringIterator.fromFile(file),SerializationType.STRING)
    }
    
  }
  
  it can "copy data from one file to another while possilby switching to a different serialization type" in {
    withFiles(jsonInputFile, newFile) { (src, dest) =>
      using(serializer(src, dest, SerializationType.JSON, SerializationType.JAVA,
          Optional.of(classOf[Person]), Optional.empty[Configurable[_]]())) { serializer =>
        serializer.copy()
      }
      JavaIterator.fromFile(dest).read should equal(Person.peopleDB)
    }
  }

  it can "filter objects out from source to destination" in {
    withFiles(javaInputFile, newFile) { (src, dest) =>
      val predicate = (person: Person) => person.getGender == Gender.f()
      using(javaSerializer[Person, Person](src, dest)) { serializer =>
        serializer.filter(predicate)
      }
      JavaIterator.fromFile(dest).read should equal(
          Person.peopleDB.stream.filter(predicate).collect(Collectors.toList[Person]))
    }
  }
  
  it can "map objects to objects of some other type" in {
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, String](src, dest, SerializationType.JAVA, SerializationType.STRING)) { serializer =>
        serializer.map((p: Person) => p.toString)
      }
      StringIterator.fromFile(dest).read should equal(Person.peopleDBStrings)
    }
  }
  
  it can "filter and then map objects" in {
    val predicate = (person: Person) => person.getGender == Gender.f()
    val mapFunction = (p: Person) => p.toString
    
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, String](src, dest, SerializationType.JAVA, SerializationType.STRING)) { serializer =>
        serializer.filterAndMap(predicate, mapFunction)
      }
      
      val expectedStream: java.util.stream.Stream[String] = Person.peopleDB.stream.filter(predicate).map(mapFunction)
      val expected = expectedStream.collect(Collectors.toList[String])
      StringIterator.fromFile(dest).read should equal(expected)
    }
  }
  
  it can "map and then filter objects" in {
    val mapFunction = (p: Person) => p.toString
    val predicate = (s: String) => s.contains("Olga")
    
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, String](src, dest, SerializationType.JAVA, SerializationType.STRING)) { serializer =>
        serializer.mapAndFilter(mapFunction, predicate)
      }
      
      val expectedStream: java.util.stream.Stream[String] = Person.peopleDB.stream.map(mapFunction)
      val expected = expectedStream.filter(predicate).collect(Collectors.toList[String])
      StringIterator.fromFile(dest).read should equal(expected)
    }
  }
  
  it can "execute a function on each object one at a time" in {
    val counter = new Counter
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, Person](src, dest, SerializationType.JAVA, SerializationType.JAVA)) { serializer =>
        serializer.forEach((p: Person) => counter.increment())
      }
      counter.getValue should equal (Person.peopleDB.size)
      JavaIterator.fromFile(dest).read should equal(Person.peopleDB)
    }
  }
  
  it can "collect objects in batches and execute a function on each batch" in {
    val counter = new Counter
    withFiles(javaInputFile, newFile) { (src, dest) =>
      using(serializer[Person, Person](src, dest, SerializationType.JAVA, SerializationType.JAVA)) { serializer =>
        val batchSize = Person.peopleDB.size / 2
        serializer.forEachBatch(batchSize, 
            (batch: java.util.List[Person]) => {
              counter.add(batch.size)
              batch.size should be <= batchSize
            })
      }
      counter.getValue should equal (Person.peopleDB.size)
      JavaIterator.fromFile(dest).read should equal(Person.peopleDB)
    }
  }  
  
  private def testSerializer[T](people: java.util.List[T], numObjects: Int, file: File,
      writer: => ObjectWriter[T], iterator: => ObjectIterator[T], serializerType: SerializationType) {
    
      withTiming (serializerType + " serializer write") { () => 
        using (writer) { w =>
          writeObjects[T](people, numObjects, w)
        }
      }
      
      val objectCount =
      withTiming (serializerType + " serializer read") { () => 
        using (iterator) { iter => 
          iter.stream().count()
        }
      }

      objectCount should equal (numObjects)
      
      println(serializerType + " serialized file size in KB: " + file.length() / 1024)
  }
  
  private def withTiming[T](activity: String)(f: () => T) = {
    val start = System.currentTimeMillis()
    val result = f()
    val end = System.currentTimeMillis()
    println (activity + " took " + (end - start) + " ms")
    result
  }
  
}