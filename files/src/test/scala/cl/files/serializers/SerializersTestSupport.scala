package cl.files.serializers

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import cl.core.lang.Control.using
import cl.core.function.ScalaToJava.toConsumer
import cl.json.JsonMapper
import cl.files.serializers.iterators.ObjectIterator
import cl.files.serializers.iterators.StringIterator
import cl.files.serializers.writers.ObjectWriter
import cl.files.serializers.writers.StringWriter
import cl.files.Person
import java.nio.charset.StandardCharsets
import java.util.Random
import cl.files.serializers.writers.JsonWriter

object SerializersTestSupport {
 
  private[files] def withFile(file: File)(f: File => Unit) {
    try { f.apply(file) } finally { file.delete() }
  }
  
  private[files] def withFiles(src: File, dest: File)(f: (File, File) => Unit) {
    try { f.apply(src, dest) } finally { src.delete(); dest.delete(); }
  }
  
  private[files] def isString[T](iterator: ObjectIterator[T]) = iterator.getClass == classOf[StringIterator]
  private[files] def isString[T](writer: ObjectWriter[T]) = writer.getClass == classOf[StringWriter]
  
  private[files] def emptyFile() = {
    val file  = newFile
    file.createNewFile()
    file
  }
  
  private[files] def newFile() = File.createTempFile("tmp", "")
  
  private[files] def javaInputFile() = {
    val file = File.createTempFile("java", "")
    using(new ObjectOutputStream(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.writeObject(p))
    }
    file
  }
  
  private[files] def javaInputFileWithDuplicates() = {
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
  
  private[files] def jsonInputFileWithEmptyLines() = {
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
  
  private[files] def jsonInputFile(jsonMapper: JsonMapper) = {
    val file = File.createTempFile("json", "")
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }
  
  private[files] def jsonInputFileRussian() = {
    val file = File.createTempFile("json", "")
    val jsonMapper = JsonMapper.getJsonMapper
    using(new PrintWriter(file, "Windows-1251")) { out =>
      Person.peopleDBInRussian().forEach((p: Person) => out.println(jsonMapper.toJson(p)))
    }
    file
  }  
  
  private[files] def stringInputFile() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(new FileOutputStream(file))) { out =>
      Person.peopleDB().forEach((p: Person) => out.println(p.toString()))
    }
    file
  }
  
  private[files] def stringInputFileInRussian() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file, "Windows-1251")) { out =>
      Person.peopleDBInRussian().forEach((p: Person) => out.println(p.toString()))
    }
    file
  }
  
  private[files] def stringInputFileWithEmptyLines() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file)) { out =>
      Person.peopleDB().forEach((p: Person) => {
        out.println(p.toString())
        out.println
      })
    }
    file
  }
  
  private[files] def stringInputFileWithHeader() = {
    val file = File.createTempFile("txt", "")
    using(new PrintWriter(file)) { out =>
      out.println("Header 1");
      out.println("Header 2");
      out.println("Header 3");
      Person.peopleDB().forEach((p: Person) => out.println(p.toString()))
    }
    file
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
}