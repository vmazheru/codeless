package cl.serializers.writers

import java.io.File
import java.io.FileOutputStream
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import cl.core.lang.Control.using
import cl.serializers.Person
import cl.serializers.SerializersTestSupport._
import cl.serializers.iterators.JavaIterator
import cl.serializers.iterators.ObjectIterator
import cl.serializers.iterators.JsonIterator
import cl.serializers.iterators.StringIterator
import java.util.Collections
import cl.serializers.iterators.DelimitedStringIterator
import cl.serializers.SerializerConfiguration
import java.util.HashMap
import cl.json.JsonMapper
import cl.serializers.delimited.DelimitedStringJoiner
import cl.serializers.delimited.DelimitedStringSplitter

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ObjectWriterSpec  extends FlatSpec with Matchers {
  
  behavior of "object writer"
  
  it can "write a single object" in {
    forObjectWriters { w: ObjectWriter[Person] =>
      w.write(Person.peopleDB().get(0))
    } { iterator: ObjectIterator[Person] => 
      iterator.next should equal (Person.peopleDB().get(0))
    }
    forStringWriters { w: ObjectWriter[String] => 
      w.write(Person.peopleDBStrings().get(0))
    } { iterator: ObjectIterator[String] => 
      iterator.next should equal (Person.peopleDBStrings().get(0))
    }
  }
  
  it can "write the entire collection of objects in one call" in {
    forObjectWriters { w: ObjectWriter[Person] =>
      w.write(Person.peopleDB())
    } { iterator: ObjectIterator[Person] => 
      iterator.read should equal (Person.peopleDB())
    }
    forStringWriters { w: ObjectWriter[String] => 
      w.write(Person.peopleDBStrings())
    } { iterator: ObjectIterator[String] => 
      iterator.read should equal (Person.peopleDBStrings())
    }
  }
  
  it can "write a Java stream content in one call" in {
    forObjectWriters { w: ObjectWriter[Person] =>
      w.write(Person.peopleDB().stream())
    } { iterator: ObjectIterator[Person] => 
      iterator.read should equal (Person.peopleDB())
    }
    forStringWriters { w: ObjectWriter[String] => 
      w.write(Person.peopleDBStrings().stream())
    } { iterator: ObjectIterator[String] => 
      iterator.read should equal (Person.peopleDBStrings())
    }
  }
  
  it can "write an interator content in one call" in {
    forObjectWriters { w: ObjectWriter[Person] =>
      w.write(Person.peopleDB().iterator())
    } { iterator: ObjectIterator[Person] => 
      iterator.read should equal (Person.peopleDB())
    }
    forStringWriters { w: ObjectWriter[String] => 
      w.write(Person.peopleDBStrings().iterator())
    } { iterator: ObjectIterator[String] => 
      iterator.read should equal (Person.peopleDBStrings())
    }
  }
  
  it should "not fail on empty collections" in {
    forObjectWriters { w: ObjectWriter[Person] =>
      w.write(Collections.emptyList[Person])
    } { iterator: ObjectIterator[Person] => 
      iterator.read should equal (Collections.emptyList)
    }
    forStringWriters { w: ObjectWriter[String] => 
      w.write(Collections.emptyList[String])
    } { iterator: ObjectIterator[String] => 
      iterator.read should equal (Collections.emptyList)
    }
  }
  
  private def forObjectWriters[T](test: ObjectWriter[T] => Unit) (verification: ObjectIterator[T] => Unit) {
    testWriters(emptyFile, javaWriters, javaIterator) (test.asInstanceOf[ObjectWriter[Person] => Unit]) (verification.asInstanceOf[ObjectIterator[Person] => Unit])
    testWriters(emptyFile, jsonWriters, jsonIterator) (test.asInstanceOf[ObjectWriter[Person] => Unit]) (verification.asInstanceOf[ObjectIterator[Person] => Unit])
    testWriters(emptyFile, psvWriters, psvIterator) (test.asInstanceOf[ObjectWriter[Person] => Unit]) (verification.asInstanceOf[ObjectIterator[Person] => Unit])
  }
  
  private def forStringWriters[T](test: ObjectWriter[String] => Unit) (verification: ObjectIterator[String] => Unit) {
    testWriters(emptyFile, stringWriters, stringIterator) (test.asInstanceOf[ObjectWriter[String] => Unit]) (verification.asInstanceOf[ObjectIterator[String] => Unit])
  }
  
  private def testWriters[T](file: File, writersF: File => List[ObjectWriter[T]], objectIteratorF: File => ObjectIterator[T]) 
      (test: ObjectWriter[T] => Unit) (verification: ObjectIterator[T] => Unit) {
    withFile(file) {f =>
      writersF.apply(file).foreach { w => 
        using (w) (test)
        val iterator = objectIteratorF.apply(file)
        verification.apply(iterator)
      } 
    }
  }
  
  private def javaWriters(file: File, lockConfiguration: Boolean) = {
    List[ObjectWriter[Person]](
      JavaWriter.toFile(file, lockConfiguration),
      JavaWriter.toOutputStream(new FileOutputStream(file), lockConfiguration))
  }
  private def javaWriters(file: File): List[ObjectWriter[Person]] = javaWriters(file, true)
  private def javaIterator(file: File) = JavaIterator.fromFile[Person](file)
  
  private def jsonWriters(file: File, lockConfiguration: Boolean) = {
    List[ObjectWriter[Person]](
      JsonWriter.toFile(file, lockConfiguration),
      JsonWriter.toOutputStream(new FileOutputStream(file), lockConfiguration))
  }
  private def jsonWriters(file: File): List[ObjectWriter[Person]] = jsonWriters(file, true)
  private def jsonIterator(file: File) = JsonIterator.fromFile(file, classOf[Person])
  
  private def psvWriters(file: File, lockConfiguration: Boolean) = {
    import scala.collection.JavaConversions.mapAsJavaMap
    import cl.core.function.ScalaToJava._
    
    val columnIndexToProperty: java.util.Map[Integer, String] = Map(
        new Integer(0) -> "name", 
        new Integer(1) -> "dob", 
        new Integer(2) -> "gender", 
        new Integer(3) -> "address")
    
    val jsonMapper = JsonMapper.getJsonMapper
        
    val addressSerializer: java.util.function.Function[Object, String] = 
      (address: Object) => jsonMapper.toJson(address)
        
    val valueSerializers: java.util.Map[String, java.util.function.Function[Object, String]] = 
      Map("address" -> addressSerializer)
        
    val fWriter = DelimitedStringWriter.toFile(file, classOf[Person], false)
        .`with`(SerializerConfiguration.columnIndexToProperty, columnIndexToProperty)
        .`with`(SerializerConfiguration.valueSerializers, valueSerializers)
        .`with`(SerializerConfiguration.delimitedStringJoiner, DelimitedStringJoiner.pipe())
    val sWriter = DelimitedStringWriter.toOutputStream(new FileOutputStream(file), classOf[Person], false)
        .`with`(SerializerConfiguration.columnIndexToProperty, columnIndexToProperty)
        .`with`(SerializerConfiguration.valueSerializers, valueSerializers)
        .`with`(SerializerConfiguration.delimitedStringJoiner, DelimitedStringJoiner.pipe())
        
    if (lockConfiguration) {
      fWriter.locked()
      sWriter.locked()
    }
    
    List[ObjectWriter[Person]](fWriter, sWriter)
  }
  private def psvWriters(file: File): List[ObjectWriter[Person]] = psvWriters(file, true)
  private def psvIterator(file: File) = {
    import scala.collection.JavaConversions.mapAsJavaMap
    import cl.core.function.ScalaToJava._
    
    val columnIndexToProperty: java.util.Map[Integer, String] = Map(
        new Integer(0) -> "name", 
        new Integer(1) -> "dob", 
        new Integer(2) -> "gender", 
        new Integer(3) -> "address")
        
    val jsonMapper = JsonMapper.getJsonMapper
        
    val addressParser: java.util.function.Function[String, Object] = 
      (address: String) => jsonMapper.fromJson(address, classOf[Person.Address])
        
    val valueParsers: java.util.Map[String, java.util.function.Function[String, Object]] = 
      Map("address" -> addressParser)
    
    DelimitedStringIterator.fromFile(file, classOf[Person], false)
      .`with`(SerializerConfiguration.columnIndexToProperty, columnIndexToProperty)
      .`with`(SerializerConfiguration.valueParsers, valueParsers)
      .`with`(SerializerConfiguration.numHeaderLines, new Integer(0))
      .`with`(SerializerConfiguration.delimitedStringSplitter, DelimitedStringSplitter.pipe())
      .locked()
  }
  
  private def stringWriters(file: File, lockConfiguration: Boolean) = {
    List[ObjectWriter[String]](
      StringWriter.toFile(file, lockConfiguration),
      StringWriter.toOutputStream(new FileOutputStream(file), lockConfiguration))
  }
  private def stringWriters(file: File): List[ObjectWriter[String]] = stringWriters(file, true)
  private def stringIterator(file: File) = StringIterator.fromFile(file)
  
}