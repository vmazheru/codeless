package cl.serializers

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers

import cl.core.configurable.Configurable
import cl.core.function.ScalaToJava._
import cl.core.lang.Control.using
import cl.json.JsonMapper
import cl.serializers.Serializer.SerializerBuildException
import cl.serializers.Serializer.psvSerializer
import cl.serializers.SerializersTestSupport._
import cl.serializers.delimited.DelimitedStringJoiner
import cl.serializers.delimited.DelimitedStringSplitter
import java.util.Optional
import cl.core.configurable.ConfigurableObject
import cl.serializers.iterators.DelimitedStringIterator
import java.time.LocalDate
import java.util.Arrays
import cl.serializers.delimited.DelimitedStringParser.PropertySetException
import java.io.PrintWriter


@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringSerializerConfigurationSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "delimited string serializer"
  
  it should "skip empty lines when instructed to do so" in {
    withFiles(psvInputFileWithEmptyLines, newFile) { (src, dest) =>
      val config = Configurable.empty().`with`(
          SerializerConfiguration.skipEmptyLines, java.lang.Boolean.TRUE).locked
      
      using(getPsvSerializer(src, dest, config)) { serializer =>
        serializer.getIterator.read
      } should equal (Person.peopleDB())
    }
  }
  
  it should "read/write a file with specified encoding" in {
    withFiles(psvInputFileRussian, newFile) { (src, dest) =>
      val config = Configurable.empty().`with`(SerializerConfiguration.charset, 
          Charset.forName("Windows-1251")).locked
      
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
    val config = Configurable.empty()
      .`with`[java.lang.Integer](SerializerConfiguration.numHeaderLines, 3).locked
    
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
      Serializer.serializer(newFile, newFile, SerializationType.DELIMITED)
    }
  }
  
  it should "use preconfigured delimited string splitter and joiner" in {
    withFiles(tsvInputFile, newFile) { (src, dest) =>
      using(getTsvSerializer(src, dest)) { serializer =>
        serializer.copy()
        
        // verify that destination is a TAB separated file
        using (new BufferedReader(new FileReader(dest))) { reader =>
          reader.readLine() should equal ("name\tdob\tgender\taddress")
          reader.readLine().contains("\t") should be (true)
        }
      }
    }
  }
  
  it should "generate header if absent when instructed by configuration" in {
    // verify that it does generate the header if configuration says TRUE
    withFiles(psvInputFileWithNoHeader, newFile) { (src, dest) =>
      using(getPsvSerializerForNoHeaderFile(src, dest, true)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (getPsvSerializerForNoHeaderFile(src, dest, true)) { serializer => 
        serializer.getIterator.read should equal (Person.peopleDB)
      }
      
      // verify that header is written to the destination file
      using (new BufferedReader(new FileReader(dest))) { reader =>
        reader.readLine() should equal ("name|dob|gender|address")
      }
    }
    
    // verify that it does NOT generate the header if configuration says FALSE
    withFiles(psvInputFileWithNoHeader, newFile) { (src, dest) =>
      using(getPsvSerializerForNoHeaderFile(src, dest, false)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (getPsvSerializerForNoHeaderFile(src, dest, false)) { serializer => 
        serializer.getIterator.read should equal (Person.peopleDB)
      }
      
      // verify that header is written to the destination file
      using (new BufferedReader(new FileReader(dest))) { reader =>
        reader.readLine() should not equal ("name|dob|gender|address")
      }
    }
  }
  
  it should "generate column names based on given naming strategies" in {
    withFiles(psvInputFileWithCapitalizedHeader, newFile) { (src, dest) =>
      val config = Configurable.empty
        .`with`[java.util.function.Function[String, String]](
            SerializerConfiguration.propertyToColumn, (s: String) => s.toUpperCase())
        .`with`[java.util.function.Function[String, String]](
            SerializerConfiguration.columnToProperty, (s: String) => s.toLowerCase())
        .locked
      
      using(getPsvSerializer(src, dest, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (getPsvSerializer(src, dest, config)) { serializer => 
        serializer.getIterator.read should equal (Person.peopleDB)
      }
      
      // verify that header is written to the destination file
      using (new BufferedReader(new FileReader(dest))) { reader =>
        reader.readLine() should equal ("NAME|DOB|GENDER|ADDRESS")
      }
    }
  }
  
  it should "use getters and setters when told to do so" in {
    withFiles(psvInputFile, newFile) { (src, dest) =>
      val config = Configurable.empty
        .`with`(SerializerConfiguration.useGetters, java.lang.Boolean.TRUE)
        .`with`(SerializerConfiguration.useSetters, java.lang.Boolean.TRUE)
        .locked
      
      using(getPsvSerializer(src, dest, config)) { serializer =>
        val data = serializer.getIterator.read
        data should equal (Person.peopleDB)
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (getPsvSerializer(src, dest, config)) { serializer => 
        serializer.getIterator.read should equal (Person.peopleDB)
      }
    }
  }
  
  it should "output only defined properties (including null values) with 'exactProperties=true'" in {
    withFiles(newFile, newFile) { (src, dest) =>
      import scala.collection.JavaConversions.mapAsJavaMap
    
      val columnIndexToProperty: java.util.Map[Integer, String] = Map(
        new Integer(2) -> "name", 
        new Integer(1) -> "dob", 
        new Integer(0) -> "gender")
      
      val config = Configurable.empty
        .`with`(SerializerConfiguration.columnIndexToProperty, columnIndexToProperty)
        .`with`(SerializerConfiguration.exactProperties, java.lang.Boolean.TRUE)
        .locked
      
      val data = Arrays.asList(
          new Person("John", LocalDate.of(1968, 2, 8), Person.Gender.MALE, null),
          new Person("Jenny", null, Person.Gender.FEMALE, null))        
        
      using(getPsvSerializer(src, dest, config)) { serializer =>
        serializer.getWriter.write(data)
      }
      
      // verify that data is OK in the destination file
      using (new BufferedReader(new FileReader(dest))) { reader =>
        reader.readLine() should equal ("gender|dob|name")
        reader.readLine() should equal ("MALE|1968-02-08|John")
        reader.readLine() should equal ("FEMALE||Jenny")
      }      
    }
  }
  
  it should "call custom error handler on property set error" in {
    withFiles(newFile, newFile) { (src, dest) =>

      var exception: PropertySetException = null
      
      val onError: java.util.function.Consumer[PropertySetException] = 
        (err: PropertySetException) => {
          exception = err
        }
      
      val config = Configurable.empty
        .`with`(SerializerConfiguration.onPropertySetError, onError)
        .locked
        
      using (new PrintWriter(src)) { writer =>
        writer.println("name|dob|gender|address")
        writer.println("John|1968-02-08|MALE|invalid address")
      }
      
      using(getPsvSerializer(src, dest, config)) { serializer =>
        val data = serializer.getIterator.read()
        System.out.println(data)
      }
      
      exception should not be (null)
      exception.getProperty should equal ("address")
      exception.getValue should equal ("invalid address")
      val person = exception.getObject.asInstanceOf[Person]
      person should not be (null)
      person.getName should equal ("John")
      person.getDob should equal (LocalDate.of(1968, 2, 8))
      person.getGender should equal (Person.Gender.MALE)
      person.getAddress should be (null)
    }
  }
  
  private[this] def getPsvSerializerConfig(configuration: Configurable[_]) = {
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
    
    config
  }
  
  private[this] def getPsvSerializer(
      src: File, dest: File, configuration: Configurable[_]): Serializer[Person, Person] = 
        psvSerializer(src, dest, classOf[Person], getPsvSerializerConfig(configuration).locked)
        
  private[this] def getPsvSerializerForNoHeaderFile(
      src: File, dest: File, generateHeader: Boolean): Serializer[Person, Person] = {
    import scala.collection.JavaConversions.mapAsJavaMap
    
    val columnIndexToProperty: java.util.Map[Integer, String] = Map(
        new Integer(0) -> "name", 
        new Integer(1) -> "dob", 
        new Integer(2) -> "gender", 
        new Integer(3) -> "address")
        
    val config = Configurable.empty()
      .`with`(SerializerConfiguration.columnIndexToProperty, columnIndexToProperty)
      .`with`[java.lang.Integer](SerializerConfiguration.numHeaderLines, 0)
      .`with`[java.lang.Boolean](SerializerConfiguration.generateHeaderIfAbsent, generateHeader)
      
    val finalConfig = getPsvSerializerConfig(config)
    
    psvSerializer(src, dest, classOf[Person], getPsvSerializerConfig(finalConfig).locked)
  }
  
  private[this] def getTsvSerializer(src: File, dest: File): Serializer[Person, Person] = {
    import scala.collection.JavaConversions.mapAsJavaMap
    
    val jsonMapper = JsonMapper.getJsonMapper
    val valueParsers = new java.util.HashMap[String, java.util.function.Function[String, Object]]
    valueParsers.put("address", (s: String) => jsonMapper.fromJson(s, classOf[Person.Address]))
    
    val addressSerializer: java.util.function.Function[Object, String] = 
      (address: Object) => jsonMapper.toJson(address)
    val valueSerializers: java.util.Map[String, java.util.function.Function[Object, String]] = 
      Map("address" -> addressSerializer)
      
    val splitter = DelimitedStringSplitter.tab
    val joiner = DelimitedStringJoiner.tab
      
    class GenericConfigurable extends ConfigurableObject[GenericConfigurable]
    
    val config = Configurable.empty()
      .`with`(SerializerConfiguration.valueParsers, valueParsers)
      .`with`(SerializerConfiguration.valueSerializers, valueSerializers)
      .`with`(SerializerConfiguration.delimitedStringSplitter, splitter)
      .`with`(SerializerConfiguration.delimitedStringJoiner, joiner)
      .locked

    Serializer.serializer(src, dest,
        SerializationType.DELIMITED, SerializationType.DELIMITED,
        Optional.of(classOf[Person]),
        Optional.of(config).asInstanceOf[Optional[Configurable[_]]])  
  }
  
}