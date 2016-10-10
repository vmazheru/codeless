/**
 * <p>This package contains classes for reading (iterating over) objects from sources of different formats.
 * 
 * <p>The object source may be a file, an IO input stream, or other source, access to which is
 * implemented by a class which extends {@code ObjectIterator}.
 * 
 * <p>Typically, using an iterator can be as easy as calling {@code iterator.read()} or 
 * converting it to a Java 8 stream by calling {@code iterator.stream()} method. For example,
 * 
 * <pre>{@code
     try (ObjectIterator<Person> iterator = JavaIterator.fromFile(input)) {
         List<Person> list = iterator.read();
         // do something with the list
     }
 * }</pre>
 * 
 * <p>Note that @{code ObjectIterator} class implements {@code java.io.Closeable} interface, and should be
 * used with the try-with-resource construct.
 * 
 * <p>{@code ObjectIterator} class also implements {@code Configurable} interface, so different concrete
 * implementations may make use of different configuration settings.  For example, a {@StringIterator} object
 * may be told to skip two header lines while reading a text file as:
 *  
 *  <pre>{@code
        try (ObjectIterator<String> iterator = StringIterator.<String>fromFile(input, false)
                .with(numHeaderLines, 2)
                .locked()) {
            List<String> list = iterator.read();
            // do something with the list
        }
 *  }</pre>
 *  
 * <p>Note that if alteration to the configuration is required, the iterator object need to be
 * created with "false" as a parameter to the factory method in order to get an unlocked configurable object.
 * 
 * <p>The list of all configuration keys for all implemented object iterator classes can be found in the
 * {@code SerializerConfiguration} class.
 *  
 */
package cl.files.serializers.iterators;
