package cl.serializers

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import cl.core.lang.Control.using
import cl.core.function.ScalaToJava.toConsumer
import cl.json.JsonMapper
import cl.serializers.iterators.ObjectIterator
import cl.serializers.iterators.StringIterator
import cl.serializers.writers.ObjectWriter
import cl.serializers.writers.StringWriter
import java.nio.charset.StandardCharsets
import java.util.Random
import cl.serializers.writers.JsonWriter

object SerializersTestSupport {
 
  def withFile(file: File)(f: File => Unit) {
    try { f.apply(file) } finally { file.delete() }
  }
  
  def withFiles(src: File, dest: File)(f: (File, File) => Unit) {
    try { f.apply(src, dest) } finally { src.delete(); dest.delete(); }
  }
  
  def isString[T](iterator: ObjectIterator[T]) = iterator.getClass == classOf[StringIterator]
  def isString[T](writer: ObjectWriter[T]) = writer.getClass == classOf[StringWriter]
  
  def emptyFile() = {
    val file  = newFile
    file.createNewFile()
    file
  }
  
  def newFile() = File.createTempFile("tmp", "")
  
  def javaInputFile() = {
    val file = File.createTempFile("java", "")
    using(new ObjectOutputStream(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
    }
    file
  }
  
  def javaInputFileWithDuplicates() = {
    val file = File.createTempFile("java", "")
    using(new ObjectOutputStream(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
    }
    file
  }
  
  def jsonInputFile() = {
    val file = File.createTempFile("json", "")
    val jsonMapper = JsonMapper.getJsonMapper
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }
  
  def jsonInputFileWithEmptyLines() = {
    val file = File.createTempFile("json", "")
    val jsonMapper = JsonMapper.getJsonMapper
    using(new PrintWriter(file)) { out =>
      Person.peopleDB().forEach((p: Person) => {
        out.println(jsonMapper.toJson(p))
        out.println
      })
    }
    file
  }
  
  def jsonInputFile(jsonMapper: JsonMapper) = {
    val file = File.createTempFile("json", "")
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }
  
  def jsonInputFileRussian() = {
    val file = File.createTempFile("json", "")
    val jsonMapper = JsonMapper.getJsonMapper
    using(new PrintWriter(file, "Windows-1251")) { out =>
      Person.peopleDBInRussian().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }
  
  def psvInputFile() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      out.println("name|dob|gender|address")
      Person.peopleDB().forEach((p: Person) => out.println(p.toPsv()))
    }
    file
  }
  
  def psvInputFileRussian() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file, "Windows-1251")) { out =>
      out.println("name|dob|gender|address")
      Person.peopleDBInRussian().forEach((p: Person) => out.println(p.toPsv()))
    }
    file
  }
  
  def stringInputFile() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(p.toString()))
    }
    file
  }
  
  def stringInputFileInRussian() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file, "Windows-1251")) { out =>
      Person.peopleDBInRussian().forEach((p: Person) => out.println(p.toString()))
    }
    file
  }
  
  def stringInputFileWithEmptyLines() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file)) { out =>
      Person.peopleDB().forEach((p: Person) => {
        out.println(p.toString())
        out.println
      })
    }
    file
  }
  
  def stringInputFileWithHeader() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file)) { out =>
      out.println("Header 1");
      out.println("Header 2");
      out.println("Header 3");
      Person.peopleDB().forEach((p: Person) => out.println(p.toString()))
    }
    file
  }
  
  def largeStringInputFile(numObjects: Int) = {
    val file = File.createTempFile("string", "")
    using (StringWriter.toFile(file)) { writer =>
      writeObjects(Person.peopleDBStrings(), numObjects, writer)
    }
    file
  }
  
  def writeObjects[T](list: java.util.List[T], numObjects: Int, writer: ObjectWriter[T]) {
    
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
}