package cl.serializers.iterators

import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.stream.Collectors.toList

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import cl.core.configurable.ConfigurableException
import cl.core.function.ScalaToJava._
import cl.core.lang.Control.using
import cl.serializers.Person
import cl.serializers.SerializersTestSupport.emptyFile
import cl.serializers.SerializersTestSupport.isString
import cl.serializers.SerializersTestSupport.javaInputFile
import cl.serializers.SerializersTestSupport.jsonInputFile
import cl.serializers.SerializersTestSupport.stringInputFile
import cl.serializers.SerializersTestSupport.withFile

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ObjectIteratorSpec extends FlatSpec with Matchers {
  
  behavior of "object iterator"
  
  it can "read one object from the input" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      iterator.next() should equal (if (isString(iterator)) Person.peopleDBStrings().get(0) else Person.peopleDB().get(0))
    }
  }
  
  it can "read all objects with one call to read()" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      iterator.read should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }
  }
  
  it can "iterate through all objects in the input" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      val list: java.util.List[AnyRef] = new ArrayList
      while (iterator.hasNext()) {
        list.add(iterator.next())
      }
      list should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }
  }
  
  it can "read next N objects from the input" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      val list: java.util.List[AnyRef] = new ArrayList
      val n = Person.peopleDB.size / 2
      while (iterator.hasNext()) {
        val nextN = iterator.next(n)
        nextN.size should be <= (n)
        list.addAll(nextN)
      }
      list should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }
  }
  
  it can "collect objects in batches and exectute a function on each batch" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      val list: java.util.List[Person] = new ArrayList
      val n = Person.peopleDB.size / 2
      val consumer: java.util.List[Person] => Unit = batch => {
        list.addAll(batch)
        batch.size should be <= n
      }
      
      iterator.forEachBatch(n, consumer)
      list should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }    
  }
  
  it can "convert itself into a Java stream" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      iterator.stream().collect(toList()) should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }
  }
  
  it should "not be able to read objects when configuration is unlocked" in {
    def check[T](i: ObjectIterator[T]) {
      a [ConfigurableException] should be thrownBy { i.read }
      a [ConfigurableException] should be thrownBy { i.hasNext }
      a [ConfigurableException] should be thrownBy { i.next }
      a [ConfigurableException] should be thrownBy { i.next(2) }
      a [ConfigurableException] should be thrownBy { i.stream }
    }
    javaIterators(javaInputFile(), false).foreach(check)
    jsonIterators(jsonInputFile(), false).foreach(check)
    stringIterators(stringInputFile(), false).foreach(check)
  }
  
  it should "correctly operate on an empty file" in {
    forAllIteratorsWithEmptyFile { (iterator: ObjectIterator[Person]) =>
        iterator.hasNext() should be (false)
        a [NoSuchElementException] should be thrownBy { iterator.next } 
    }
  }
  
  it should "not advance throught the input on multiple calls to hasNext()" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      for(i <- 0 to Person.peopleDB().size() * 2) {
        iterator.hasNext() should be (true)
      }
      iterator.read should equal (if (isString(iterator)) Person.peopleDBStrings() else Person.peopleDB())
    }
  }
  
  it should "eventually throw NoSuchElementException when next() called without hasNext()" in {
    forAllIterators { (iterator: ObjectIterator[Person]) =>
      a [NoSuchElementException] should be thrownBy {
        val times = Person.peopleDB().size + 1
        for(i <- 0 to times) {
          if (i == times - 1) { // we've reached the end, hasNext() has to say "false"
            iterator.hasNext() should be (false)
          } else {
            iterator.hasNext() should be (true)
          }
          iterator.next()
          null // otherwise it's trying to collect objects in for comprehension, and since it
               // expects Person instances, it fails with ClassCastException for StringIterator
        }
      }
    }
  }
  
  private def forAllIterators[T](test: ObjectIterator[T] => Unit) {
    testIterators(javaInputFile, javaIterators) (test.asInstanceOf[ObjectIterator[Person] => Unit])
    testIterators(jsonInputFile, jsonIterators) (test.asInstanceOf[ObjectIterator[Person] => Unit])
    testIterators(stringInputFile, stringIterators) (test.asInstanceOf[ObjectIterator[String] => Unit])
  }
  
  private def forAllIteratorsWithEmptyFile[T](test: ObjectIterator[T] => Unit) {
    testIterators(emptyFile, javaIterators) (test.asInstanceOf[ObjectIterator[Person] => Unit])
    testIterators(emptyFile, jsonIterators) (test.asInstanceOf[ObjectIterator[Person] => Unit])
    testIterators(emptyFile, stringIterators) (test.asInstanceOf[ObjectIterator[String] => Unit])
  }
  
  private def withIterators[T](iterators: List[ObjectIterator[T]])(f: ObjectIterator[T] => Unit) {
    iterators.foreach { iter => using (iter) (f) }
  }
  
  private def testIterators[T](fileF: () => File, iterF: File => List[ObjectIterator[T]])(iteratorTest: ObjectIterator[T] => Unit) {
    withFile(fileF.apply()) { f => withIterators (iterF.apply(f)) (iteratorTest) }    
  }
  
  private def javaIterators(file: File, lockConfiguration: Boolean) = {
    List[ObjectIterator[Person]](
      JavaIterator.fromFile(file, lockConfiguration),
      JavaIterator.fromInputStream(new FileInputStream(file), lockConfiguration))
  }
  private def javaIterators(file: File): List[ObjectIterator[Person]] = javaIterators(file, true)
  
  private def jsonIterators(file: File, lockConfiguration: Boolean) = {
    List(
      JsonIterator.fromFile(file, classOf[Person], lockConfiguration),
      JsonIterator.fromInputStream(new FileInputStream(file), classOf[Person], lockConfiguration))
  }
  private def jsonIterators(file: File): List[ObjectIterator[Person]] = jsonIterators(file, true)
  
  private def stringIterators(file: File, lockConfiguration: Boolean) = {
    List[ObjectIterator[String]](
        StringIterator.fromFile(file, lockConfiguration),
        StringIterator.fromInputStream(new FileInputStream(file), lockConfiguration))
  }
  private def stringIterators(file: File): List[ObjectIterator[String]] = stringIterators(file, true)
  
}