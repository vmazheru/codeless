/**
 * <p>This package contains classes for writing (serializing) objects to files and streams, using different formats (a.k.a serialization types).
 * 
 * <p>The primary class of interest is {@link ObjectWriter}, which defines a series of overloaded {@code write()} methods.
 * It also contains important {@code asCollector() } method which allows to convert an object writer into
 * a Java 8 stream collector.
 * 
 * <p>Here is a code example, which illustrates how to write a list of objects into a JSON file:
 * <pre>{@code
     File output = getMyFile();
     List<Person> people = getPeople();
     try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
         writer.write(people);
     }
 * }</pre>
 * 
 * <p>Another example illustrates collecting a Java 8 stream with {@link ObjectWriter}. Note, that in this
 * particular example enclosing the writer into a 'try-with-resource' construct is not strictly necessary,
 * since the {@code Collector.finisher()} method implementation in {@link ObjectWriter} class closes the file.
 * 
 * <pre>{@code
     File output = getMyFile();
     List<Person> people = getPeople();
     try (ObjectWriter<Person> writer = JsonWriter.toFile(output)) {
         people.stream().collect(writer.asCollector());
         // or
         // writer.write(people.stream());
     }
 * }</pre>
 * 
 */
package cl.serializers.writers;
