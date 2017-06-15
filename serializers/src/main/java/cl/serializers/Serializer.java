package cl.serializers;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static cl.serializers.SerializerConfiguration.*;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cl.core.configurable.Configurable;
import cl.serializers.SerializerImpl.SerializerBuilder;
import cl.serializers.iterators.ObjectIterator;
import cl.serializers.writers.ObjectWriter;

/**
 * A serializer is an object which wraps a pair of an {@link ObjectIterator} and an {@link ObjectWriter},
 * which share common configuration attributes.
 * 
 * <p>The idea is to support work flows where one needs to read objects from some source (file, or input stream),
 * process them, and then write them to some destination (file, or output stream).  The important part
 * here is that a consistent set of configuration settings is used to read and write the objects.
 * 
 * <p>Examples of using serializers may include sorting files, converting a file of one type (for example JAVA) to
 * another type (for example JSON), removing objects from a file (filtering), converting objects of one type
 * to objects of a different type (mapping), etc.  Serializers should be used when reading objects goes along
 * with writing them. In cases when only reading or writing is needed, one should use {@link ObjectIterator}s and
 * {@link ObjectWriter}s directly.
 * 
 * <p>Serializers implement {@code Closeable} interface, and if used in 'try-with-resource' construct will
 * close both their object iterator and object writer automatically.
 * 
 * <p>Serializer objects may be created by using one of the numerous static factory methods defined in this interface.
 *  
 * @param <T> type of objects which are iterated over by the serializer's object iterator
 * @param <R> type of objects to which objects will be converted when writing them by the serializer's object writer
 */
public interface Serializer<T,R> extends Closeable {

    /**
     * Get object iterator.
     */
    ObjectIterator<T> getIterator();
    
    /**
     * Get object writer.
     */
    ObjectWriter<R> getWriter();
    
    /**
     * Get object iterator as Java 8 stream.
     */
    default Stream<T> stream() {
        return getIterator().stream();
    }

    /**
     * Turn this serializer's object writer into a Java 8 stream collector.
     */
    default Collector<R, ObjectWriter<R>, ObjectWriter<R>> toWriter() {
        return getWriter().asCollector();
    }
    
    /**
     * Copy all objects from source to destination.
     * This method assumes, that object iterator and object writer operate on objects of the same type.
     */
    @SuppressWarnings("unchecked")
    default void copy() {
        uncheck(() -> {
            try (Serializer<T,T> s = (Serializer<T,T>)this) {
                s.stream().collect(s.toWriter());
            }
        });
    }
    
    /**
     * Copy all objects which satisfy the given predicate from source to destination.
     * This method assumes, that object iterator and object writer operate on objects of the same type. 
     */
    @SuppressWarnings("unchecked")
    default void filter(Predicate<T> p) {
        uncheck(() -> {
            try (Serializer<T,T> s = (Serializer<T,T>)this) {
                s.stream().filter(p).collect(s.toWriter());
            }
        });
    }

    /**
     * Apply given function to all objects in the source and write the results to destination.
     */
    default void map(Function<T,R> f) {
        uncheck(() -> {
            try (Serializer<T,R> s = this) {
                s.stream().map(f).collect(s.toWriter());
            }
        });
    }
    
    /**
     * Filter objects with given predicate, apply a mapping function to the
     * filtered objects, and then write the results to the destination.
     */
    default void filterAndMap(Predicate<T> p, Function<T,R> f) {
        uncheck(() -> {
            try (Serializer<T,R> s = this) {
                s.stream().filter(p).map(f).collect(s.toWriter());
            }
        });
    }
    
    /**
     * Apply the given mapping function to all objects in the source, filter the results with the 
     * given predicate, and then write the results to the destination.
     */
    default void mapAndFilter(Function<T,R> f, Predicate<R> p) {
        uncheck(() -> {
            try (Serializer<T,R> s = this) {
                s.stream().map(f).filter(p).collect(s.toWriter());
            }
        });
    }
    
    /**
     * Read objects, run a given consumer on each object, and write them to the destination.
     * This method assumes, that object iterator and object writer operate on objects of the same type. 
     */
    @SuppressWarnings("unchecked")
    default void forEach(Consumer<T> f) {
        uncheck(() -> {
            try (Serializer<T,T> s = (Serializer<T,T>)this;
                ObjectIterator<T> iter = s.getIterator();
                ObjectWriter<T> writer = s.getWriter()) {
                while (iter.hasNext()) {
                    T next = iter.next();
                    f.accept(next);
                    writer.write(next);
                }
            }
        });        
    }
    
    /**
     * Read objects, collect them in batches of the specified size, execute a function on each batch, 
     * and write objects to the destination.
     * This method assumes, that object iterator and object writer operate on objects of the same type.
     */
    @SuppressWarnings("unchecked")
    default void forEachBatch(int batchSize, Consumer<List<T>> onBatch) {
        uncheck(() -> {
            try (Serializer<T,T> s = (Serializer<T,T>)this;
                ObjectIterator<T> iter = s.getIterator();
                ObjectWriter<T> writer = s.getWriter()) {
                iter.forEachBatch(batchSize, batch -> {
                    onBatch.accept(batch);
                    writer.write(batch);
                });
            }
        });
    }
    
    /*
     * The rest of the class contains factory methods which create serializers from different
     * sources, of different types, and with different configuration settings.
     */
    
    /* The next four methods generate serializers in most common form (they accept every possible parameter) */
    
    /**
     * Construct a serializer which operates on two files.
     *  
     * @param inputFile                input file
     * @param outputFile               output file
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @param iteratorClass            class of objects which object iterator will iterate over
     * @param configuration            configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration                       
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType inputSerializationType, 
            SerializationType outputSerializationType,
            Optional<Class<T>> iteratorClass,
            Optional<Configurable<?>> configuration) {
        return SerializerBuilder.<T,R>basicBuilder(
                inputSerializationType, outputSerializationType, iteratorClass, configuration)
            .withInputFile(inputFile)
            .withOutputFile(outputFile)
            .build();
    }
    
    /**
     * Construct a serializer which reads from a file and writes to an output stream.
     *  
     * @param inputFile                input file
     * @param outputStream             output stream
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @param iteratorClass            class of objects which object iterator will iterate over
     * @param configuration            configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration                       
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType inputSerializationType, 
            SerializationType outputSerializationType,
            Optional<Class<T>> iteratorClass,
            Optional<Configurable<?>> configuration) {
        return SerializerBuilder.<T,R>basicBuilder(
                inputSerializationType, outputSerializationType, iteratorClass, configuration)
                .withInputFile(inputFile)
                .withOutputStream(outputStream)
                .build();        
    }
    
    /**
     * Construct a serializer which reads from an input stream and writes to a file
     *  
     * @param inputStream              input stream
     * @param outputFile               output file
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @param iteratorClass            class of objects which object iterator will iterate over
     * @param configuration            configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration                       
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType inputSerializationType, 
            SerializationType outputSerializationType,
            Optional<Class<T>> iteratorClass,
            Optional<Configurable<?>> configuration) {
        return SerializerBuilder.<T,R>basicBuilder(
                inputSerializationType, outputSerializationType, iteratorClass, configuration)
                .withInputStream(inputStream)
                .withOutputFile(outputFile)
                .build();        
    }
    
    /**
     * Construct a serializer which reads from an input stream and writes to an output stream.
     *  
     * @param inputStream              input stream
     * @param outputStream             output stream
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @param iteratorClass            class of objects which object iterator will iterate over
     * @param configuration            configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration                       
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType inputSerializationType, 
            SerializationType outputSerializationType,
            Optional<Class<T>> iteratorClass,
            Optional<Configurable<?>> configuration) {
        return SerializerBuilder.<T,R>basicBuilder(
                inputSerializationType, outputSerializationType, iteratorClass, configuration)
                .withInputStream(inputStream)
                .withOutputStream(outputStream)
                .build();        
    }
    
    
    /* The next four methods create serializers where the iterator and the writer are of the same serialization type */
    
    /**
     * Create a serializer with the same serialization type for input and output, which operates on files.
     * 
     * @param inputFile         input file         
     * @param outputFile        output file
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @param configuration     configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType serializationType,
            Class<T> iteratorClass, 
            Configurable<?> configuration) {
        return serializer(inputFile, outputFile, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from a file and writes to an output stream.
     * 
     * @param inputFile         input file         
     * @param outputStream      output stream
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @param configuration     configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType serializationType,
            Class<T> iteratorClass,
            Configurable<?> configuration) {
        return serializer(inputFile, outputStream, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from an input stream and writes to a file.
     * 
     * @param inputStream       input stream
     * @param outputFile        output file
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @param configuration     configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType serializationType,
            Class<T> iteratorClass,
            Configurable<?> configuration) {
        return serializer(inputStream, outputFile, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from an input stream and writes to an output stream.
     * 
     * @param inputStream       input stream         
     * @param outputStream      output stream
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @param configuration     configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType serializationType,
            Class<T> iteratorClass,
            Configurable<?> configuration) {
        return serializer(inputStream, outputStream, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.of(configuration));
    }    
    
    
    /* The next four methods generate serializers with the same serialization type and default configuration */
    
    /**
     * Create a serializer with the same serialization type for input and output and default
     * configuration settings, which operates on files.
     * 
     * @param inputFile         input file
     * @param outputFile        output file
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType serializationType,
            Class<T> iteratorClass) {
        return serializer(inputFile, outputFile, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default
     * configuration settings, which reads from a file and writes to an output stream.
     * 
     * @param inputFile         input file
     * @param outputStream      output stream
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType serializationType,
            Class<T> iteratorClass) {
        return serializer(inputFile, outputStream, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default
     * configuration settings, which reads from an input stream and writes to a file.
     * 
     * @param inputStream       input stream
     * @param outputFile        output file
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType serializationType,
            Class<T> iteratorClass) {
        return serializer(inputStream, outputFile, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default
     * configuration settings, which reads from an input stream and writes to an output stream.
     * 
     * @param inputStream       input stream
     * @param outputStream      output stream
     * @param serializationType serialization type
     * @param iteratorClass     class of objects which object iterator will iterate over
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType serializationType,
            Class<T> iteratorClass) {
        return serializer(inputStream, outputStream, serializationType, serializationType, 
                Optional.of(iteratorClass), Optional.empty());
    }
    
    
    /*  The next four methods create serializers with the same serialization type for input and output and
     *  no iterator class.  These can be used only to create serializers for which run-time type information is 
     *  not necessary for object iterator to operate (for example, string or java serializers). 
     */
    
    /**
     * Create a serializer with the same serialization type for input and output, which operates on files.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile          input file
     * @param outputFile         output file
     * @param serializationType  serialization type
     * @param configuration      configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType serializationType,
            Configurable<?> configuration) {
        return serializer(inputFile, outputFile, serializationType, serializationType, 
                Optional.empty(), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from a file and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile          input file
     * @param outputStream       output stream
     * @param serializationType  serialization type
     * @param configuration      configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType serializationType,
            Configurable<?> configuration) {
        return serializer(inputFile, outputStream, serializationType, serializationType, 
                Optional.empty(), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from an input stream and writes to a file.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream        input stream
     * @param outputFile         output file
     * @param serializationType  serialization type
     * @param configuration      configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType serializationType,
            Configurable<?> configuration) {
        return serializer(inputStream, outputFile, serializationType, serializationType, 
                Optional.empty(), Optional.of(configuration));
    }
    
    /**
     * Create a serializer with the same serialization type for input and output,
     * which reads from an input stream and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream        input stream
     * @param outputStream       output stream
     * @param serializationType  serialization type
     * @param configuration      configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType serializationType,
            Configurable<?> configuration) {
        return serializer(inputStream, outputStream, serializationType, serializationType, 
                Optional.empty(), Optional.of(configuration));
    }

    /*
     * The next for methods create serializers with default configuration settings and no
     * iterator class.
     */
    
    /**
     * Create a serializer with default configuration settings, which operates on files.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile                input file
     * @param outputFile               output file
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType inputSerializationType,
            SerializationType outputSerializationType) {
        return serializer(inputFile, outputFile, inputSerializationType, outputSerializationType, 
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with default configuration settings,
     * which reads from a file and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile                input file
     * @param outputStream             output stream
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType inputSerializationType, 
            SerializationType outputSerializationType) {
        return serializer(inputFile, outputStream, inputSerializationType, outputSerializationType, 
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with default configuration settings,
     * which reads from an input stream and writes to a file.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream              input stream
     * @param outputFile               output file
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType inputSerializationType,
            SerializationType outputSerializationType) {
        return serializer(inputStream, outputFile, inputSerializationType, outputSerializationType,
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with default configuration settings,
     * which reads from an input stream and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream              input stream
     * @param outputStream             output stream
     * @param inputSerializationType   serialization type of object iterator
     * @param outputSerializationType  serialization type of object writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType inputSerializationType,
            SerializationType outputSerializationType) {
        return serializer(inputStream, outputStream, inputSerializationType, outputSerializationType, 
                Optional.empty(), Optional.empty());
    }    
    
    /**
     * Create a serializer with the same serialization type for input and output and default configuration settings,
     * which operates on files.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile           input file
     * @param outputFile          output file
     * @param serializationType   serialization type
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            File outputFile,
            SerializationType serializationType) {
        return serializer(inputFile, outputFile, serializationType, serializationType,
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default configuration settings,
     * which reads from a file and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputFile           input file
     * @param outputStream        output stream
     * @param serializationType   serialization type
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            File inputFile,
            OutputStream outputStream,
            SerializationType serializationType) {
        return serializer(inputFile, outputStream, serializationType, serializationType, 
                
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default configuration settings,
     * which reads from an input stream and writes to a file.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream         input stream
     * @param outputFile          output file
     * @param serializationType   serialization type
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            File outputFile,
            SerializationType serializationType) {
        return serializer(inputStream, outputFile, serializationType, serializationType, 
                Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a serializer with the same serialization type for input and output and default configuration settings,
     * which reads from an input stream and writes to an output stream.
     * <p>This method can be used only to create serializers for which run-time type information is
     * not required for the object iterator to operate (for example, string or java serializers).
     * 
     * @param inputStream         input stream
     * @param outputStream        output stream
     * @param serializationType   serialization type
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> serializer(
            InputStream inputStream,
            OutputStream outputStream,
            SerializationType serializationType) {
        return serializer(inputStream, outputStream, serializationType, serializationType, 
                Optional.empty(), Optional.empty());
    }
    
    /* The rest of the methods generate serializers for specific serialization types which operates on files */
    
    /**
     * Generate {@code SerializationType.JAVA} serializer, which operates on files.
     * 
     * @param inputFile      input file
     * @param outputFile     output file
     * @param configuration  configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> javaSerializer(
            File inputFile,
            File outputFile,
            Configurable<?> configuration) {
        return serializer(inputFile, outputFile, SerializationType.JAVA, SerializationType.JAVA, 
                Optional.empty(), Optional.of(configuration));
    }
    
    /**
     * Generate {@code SerializationType.JAVA} serializer with default configuration settings, which operates on files.
     * 
     * @param inputFile   input file
     * @param outputFile  output file
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> javaSerializer(
            File inputFile,
            File outputFile) {
        return javaSerializer(inputFile, outputFile, javaSerializerDefaultConfiguration());
    }
    
    /**
     * Generate {@code SerializationType.JSON} serializer, which operates on files.
     * 
     * @param inputFile      input file
     * @param outputFile     output file
     * @param iteratorClass  class of objects which object iterator will iterate over
     * @param configuration  configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> jsonSerializer(
            File inputFile,
            File outputFile,
            Class<T> iteratorClass,
            Configurable<?> configuration) {
        return serializer(inputFile, outputFile, SerializationType.JSON, SerializationType.JSON, 
                Optional.of(iteratorClass), Optional.of(configuration));
    }
    
    /**
     * Generate {@code SerializationType.JSON} serializer with default configuration settings, which operates on files.
     * 
     * @param inputFile     input file
     * @param outputFile    output file
     * @param iteratorClass class of objects which object iterator will iterate over
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> jsonSerializer(
            File inputFile,
            File outputFile,
            Class<T> iteratorClass) {
        return jsonSerializer(inputFile, outputFile, iteratorClass, jsonSerializerDefaultConfiguration());
    }
    
    /**
     * Generate {@code SerializationType.STRING} serializer, which operates on files.
     * 
     * @param inputFile      input file
     * @param outputFile     output file
     * @param configuration  configuration object, which contains keys for both iterator and writer
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> stringSerializer(
            File inputFile,
            File outputFile,
            Configurable<?> configuration) {
        return serializer(inputFile, outputFile, SerializationType.STRING, SerializationType.STRING, 
                Optional.empty(), Optional.of(configuration));
    }
    
    /**
     * Generate {@code SerializationType.STRING} serializer with default configuration settings, which operates on files.
     * 
     * @param inputFile   input file
     * @param outputFile  output file
     * @return a serializer object with locked configuration
     */
    static <T,R> Serializer<T,R> stringSerializer(
            File inputFile,
            File outputFile) {
        return stringSerializer(inputFile, outputFile, stringSerializerDefaultConfiguration());
    }
    
    
    /**
     * An exception which may be thrown by any factory methods when building a serializer encounters a problem.
     */
    @SuppressWarnings("serial")
    public static class SerializerBuildException extends RuntimeException {
        public SerializerBuildException(String message) {
            super(message);
        }
    }
}
