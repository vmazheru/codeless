package cl.files.serializers;

import static cl.files.serializers.Serializer.*;
import static cl.files.serializers.SerializerConfiguration.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.Test;

import cl.core.configurable.Configurable;
import cl.files.Person;
import cl.files.serializers.iterators.JavaIterator;
import cl.files.serializers.iterators.ObjectIterator;
import cl.files.serializers.iterators.StringIterator;
import cl.files.serializers.writers.JavaWriter;
import cl.files.serializers.writers.JsonWriter;
import cl.files.serializers.writers.ObjectWriter;
import cl.files.serializers.writers.StringWriter;

/**
 * Contains examples of using object iterators, writers, and serializers in Java code.
 */
public class SerializerExamples {
    
    /**
     * Illustrates reading from a file using {@code JavaIterator}.
     */
    @Test
    public void readJavaFile() throws IOException {
        File input  = javaInputFile();
        try (ObjectIterator<Person> iterator = JavaIterator.fromFile(input)) {
            List<Person> people = iterator.read();
            System.out.println(people);
        } finally {
            input.delete();
        }
    }
    
    /**
     * Illustrates reading strings from a file with {@code StringIterator}.
     * The iterator will be configured to skip two header lines in the file.
     */
    @Test
    public void skipHeaderLines() throws IOException {
        File input = stringInputFileWithTwoLineHeader();
        try (ObjectIterator<String> iterator = StringIterator
                .fromFile(input, false)
                .with(numHeaderLines, 2)
                .locked()) {
            List<String> list = iterator.read();
            System.out.println(list);
        } finally {
            input.delete();
        }
    }
    
    /**
     * Illustrates writing a list of objects to a JSON file.
     */
    @Test
    public void writeJsonFile() throws IOException {
        File output = jsonOutputFile();
        try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
            writer.write(Person.peopleDB());
        } finally {
            output.delete();
        }
        
        // or, if you have an iterator instead of a collection
        
        output = jsonOutputFile();
        try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
            writer.write(Person.peopleDB().iterator());
        } finally {
            output.delete();
        }
    }
    
    /**
     * Illustrates collecting a Java 8 stream with ObjectWriter.
     */
    @Test
    public void collectToFile() throws IOException {
        File output = jsonOutputFile();
        try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
            Person.peopleDB().stream().collect(writer.asCollector());
        } finally {
            output.delete();
        }
        
        // or
        
        output = jsonOutputFile();
        try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
            writer.write(Person.peopleDB().stream());
        } finally {
            output.delete();
        }
    }
    
    /**
     * This test shows how to read a file which contains objects serialized with Java serialization mechanism,
     *  and write the data to another file using JSON serialization. It uses {@code Serializer.copy()} method.
     */
    @Test
    public void convertJavaFileToJsonFile() throws IOException {
        File input  = javaInputFile();
        File output = jsonOutputFile();
        
        try (Serializer<Person, Person> serializer = serializer(
                input,
                output,
                SerializationType.JAVA,
                SerializationType.JSON,
                Optional.empty(),
                Optional.of(Configurable.empty()))) {
            serializer.copy();
            displayFile(output);
        } finally {
            input.delete();
            output.delete();
        }
    }
    
    @Test
    public void convertObjectToTheirStrings() throws IOException {
        File input  = javaInputFile();
        File output = stringOutputFile();
        
        try (Serializer<Person, String> serializer = serializer(
                input,
                output,
                SerializationType.JAVA,
                SerializationType.STRING)) {
            serializer.map(Objects::toString);
            displayFile(output);
        } finally {
            input.delete();
            output.delete();
        }
    }
    
    private static File javaInputFile() throws IOException {
        File f = File.createTempFile("temp", ".ser");
        try(ObjectWriter<Person> writer = JavaWriter.<Person>toFile(f)) {
            writer.write(Person.peopleDB());
        }
        return f;
    }
    
    private static File stringInputFileWithTwoLineHeader() throws IOException {
        File f = File.createTempFile("temp", ".txt");
        try(ObjectWriter<String> writer = StringWriter.toFile(f)) {
            writer.write("header1");
            writer.write("header2");
            writer.write(Person.peopleDBStrings());
        }
        return f;
    }
    
    private static File jsonOutputFile() throws IOException {
        File f = File.createTempFile("temp", ".json");
        return f;
    }
    
    private static File stringOutputFile() throws IOException {
        File f = File.createTempFile("temp", ".txt");
        return f;
    }
    
    private static void displayFile(File file) throws IOException {
        Files.readAllLines(file.toPath()).forEach(System.out::println);
    }
}
