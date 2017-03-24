package cl.files.filesorters

import java.io.File
import java.io.UncheckedIOException
import java.util.ArrayList
import java.util.Collections
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import cl.core.lang.Control.using
import cl.files.serializers.Serializer._
import cl.files.serializers.Serializer
import cl.files.serializers.iterators.JavaIterator
import cl.files.serializers.iterators.JsonIterator
import cl.files.serializers.iterators.StringIterator
import cl.util.file.sorter.ExternalMergeFileSorter
import cl.util.file.sorter.InMemoryFileSorter
import cl.util.file.sorter.FileSorter
import cl.files.serializers.writers.StringWriter
import cl.files.serializers.writers.ObjectWriter
import java.util.Random
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import cl.util.file.sorter.Person
import java.io.PrintWriter
import cl.json.JsonMapper
import cl.core.function.ScalaToJava.toConsumer

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class FileSorterSpec extends FlatSpec with Matchers {
  
  behavior of "all file sorters"
  
  it should "read data from input file, sort it, and write it to the destination" in {
    val sorters = Array(
        (serializer: Serializer[Person, Person]) => new InMemoryFileSorter(serializer).locked(),
        (serializer: Serializer[Person, Person]) => new ExternalMergeFileSorter(serializer).locked()
    )
    withFileSorters(sorters) { f =>
      withFiles(jsonInputFile, newFile) { (src, dest) => 
        using (jsonSerializer[Person, Person](src, dest, classOf[Person])) { serializer =>
          f(serializer).sort()
        }
        verifyJsonFileSorted(dest)
      }    
    }
  }

  it should "respect 'remove duplicates' configuration setting" in {
    val sorters = Array(
        (serializer: Serializer[Person, Person]) => 
          new InMemoryFileSorter(serializer).`with`[java.lang.Boolean](FileSorter.removeDuplicates, true).locked(),
        (serializer: Serializer[Person, Person]) =>
          new ExternalMergeFileSorter(serializer).`with`[java.lang.Boolean](FileSorter.removeDuplicates, true).locked()
    )
    withFileSorters(sorters) { f =>
      withFiles(javaInputFileWithDuplicates, newFile) { (src, dest) =>
        using (javaSerializer[Person, Person](src, dest)) { serializer =>
          f(serializer).sort()
        }
        verifyJavaFileSorted(dest)
      }
    }
  }
  
  it should "properly handle an existing but empty input file" in {
    val sorters = Array(
        (serializer: Serializer[Person, Person]) => new InMemoryFileSorter(serializer).locked(),
        (serializer: Serializer[Person, Person]) => new ExternalMergeFileSorter(serializer).locked()
    )
    withFileSorters(sorters) { f =>
      withFiles(newFile, new File("dest.ser")) { (src, dest) =>
        using (javaSerializer[Person, Person](src, dest)) { serializer =>
          f(serializer).sort()
        }
        dest.exists() should be (true)
        JavaIterator.fromFile(dest).read.isEmpty() should be (true)
      }
    }
  }
  
  it should "throw IO exception (coming from the iterator) when the input file does not exist" in {
    val sorters = Array(
        (serializer: Serializer[Person, Person]) => new InMemoryFileSorter(serializer).locked(),
        (serializer: Serializer[Person, Person]) => new ExternalMergeFileSorter(serializer).locked()
    )
    withFileSorters(sorters) { f =>
      withFiles(new File("source.ser"), new File("dest.ser")) { (src, dest) =>
        a[UncheckedIOException] should be thrownBy (
          using (javaSerializer[Person, Person](src, dest)) { serializer =>
            f(serializer).sort()
          }
        )
        dest.exists() should be (false)
      }
    }
  }
  
  behavior of "default file sorter"
  
  it should "switch from in-memory file sorter to external merge file sorter on file size threshold" in {
    var inMemoryTime: Long = 0
    var externalMergeTime: Long = 0
    
    withFiles(largeStringInputFile(1000000), newFile) { (src, dest) =>
      using (stringSerializer[String, String](src, dest)) { serializer =>
        val fileSorter = FileSorter.getFileSorter(serializer, src.length(), false)
          .`with`[java.lang.Long](FileSorter.inMemorySizeThreshold, src.length() + 1).locked()
        
        val start = System.currentTimeMillis()
        fileSorter.sort()
        inMemoryTime = System.currentTimeMillis() - start
      }
      
      verifyLargeStringFileSorted(dest)
    }
    
    withFiles(largeStringInputFile(1000000), newFile) { (src, dest) =>
      using (stringSerializer[String, String](src, dest)) { serializer =>
        val fileSorter = FileSorter.getFileSorter(serializer, src.length(), false)
          .`with`[java.lang.Long](FileSorter.inMemorySizeThreshold, src.length() - 1).locked()
        
        val start = System.currentTimeMillis()
        fileSorter.sort()
        externalMergeTime = System.currentTimeMillis() - start
      }
      
      verifyLargeStringFileSorted(dest)
    }
    
    println ("External merge sorting time: " + externalMergeTime)
    println ("In-memory sorting time: " + inMemoryTime)
    externalMergeTime > inMemoryTime should be (true)
    
  }
  
  private[this] def withFileSorters[T](fileSorters: Array[Serializer[T,T] => FileSorter[T]])
                        (test: (Serializer[T,T] => FileSorter[T]) => Unit) {
    for (f <- fileSorters) {
      test(f)
    }
  }
  
  private[this] def verifyJsonFileSorted(f: File) {
    using (JsonIterator.fromFile(f, classOf[Person])) { iterator =>
      val people = Person.peopleDB
      Collections.sort(people)
      iterator.read() should equal (people)
    }
  }
  
  private[this] def verifyLargeStringFileSorted(f: File) {
    using (StringIterator.fromFile(f)) { iterator =>
      var prev = iterator.next
      while (iterator.hasNext()) {
        var n = iterator.next
        n.compareTo(prev) >= 0 should be (true)
        prev = n
      }
    }
  }
  
  private[this] def verifyJavaFileSorted(f: File) {
    using (JavaIterator.fromFile(f)) { iterator =>
      val people = Person.peopleDB
      Collections.sort(people)
      iterator.read() should equal (people)
    }
  }
  
  private[this] def verifyJavaFileWithDuplicatesSorted(f: File) {
    using (JavaIterator.fromFile(f)) { iterator =>
      val people = new ArrayList(Person.peopleDB)
      people.addAll(Person.peopleDB)
      Collections.sort(people)
      iterator.read() should equal (people)
    }
  }
  
  private[this] def withFiles(src: File, dest: File)(f: (File, File) => Unit) {
    try { f.apply(src, dest) } finally { src.delete(); dest.delete(); }
  }
  
  private[files] def largeStringInputFile(numObjects: Int) = {
    val file = File.createTempFile("string", "")
    using (StringWriter.toFile(file)) { writer =>
      writeObjects(Person.peopleDBStrings(), numObjects, writer)
    }
    file
  }
  
  private[files] def writeObjects[T](list: java.util.List[T], numObjects: Int, writer: ObjectWriter[T]) {
    
    def randomElem[T](list: java.util.List[T]) = {
      val size = list.size();
      val r = new Random(System.currentTimeMillis())
      list.get(r.nextInt(size))
    }
    
    var i = 0
    while (i < numObjects) {
      writer.write(randomElem(list))
      i += 1
    }
  }
  
  private[this] def newFile() = File.createTempFile("tmp", "")
  
  private[this] def javaInputFileWithDuplicates() = {
    val file = File.createTempFile("java", "")
    using(new ObjectOutputStream(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
    }
    file
  }
  
  private[files] def jsonInputFile() = {
    val file = File.createTempFile("json", "")
    val jsonMapper = JsonMapper.getJsonMapper
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }
  
}