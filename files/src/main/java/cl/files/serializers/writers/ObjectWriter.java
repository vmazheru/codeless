package cl.files.serializers.writers;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.Closeable;
import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cl.core.configurable.ConfigurableObject;

/**
 * Implementations of this class know how to write objects to files or output streams, while
 * converting them into some specific representations (for example, serialize objects with Java serialization
 * mechanism, or convert to JSON). 
 * 
 * <p>{@code ObjectWriter} is a {@code ConfigurableObject}, so the implementations can feel free
 * to implement any configuration logic they desire.  Overloaded {@code write()} methods
 * when called on a writer with unlocked configuration will result in {@code ConfigurableException}.
 * 
 * <p>{@code ObjectWriter} also implements {@code Closeable} interface and may be used in Java 'try-with-resource' construct.
 * 
 * @param <T> Type of objects which an object writer writes to its destination. This may be some custom
 * class for {@code JavaWriter} or {@JsonWriter}, or {@String} for {@code StringWriter}.
 */
public abstract class ObjectWriter<T> extends ConfigurableObject<ObjectWriter<T>> implements Closeable {
    
    /**
     * Write an object to the iterator's destination.
     * The implementation of this method is subclass-specific.
     */
    public abstract void write(T x);

    /**
     * Write a collection of objects.
     */
    public void write(Collection<T> xs) {
        xs.forEach(this::write);
    }
    
    /**
     * Write all objects from the given stream.
     */
    public void write(Stream<T> xs) {
       xs.forEach(this::write);
    }
    
    /**
     * Write all objects which the given iterator has.
     */
    public void write(Iterator<T> xs) {
        xs.forEachRemaining(this::write);
    }
    
    /**
     * Create a new object writer for the given file, which is of the same type and with the same configuration settings as the
     * original object writer.
     * 
     * <p>The new object writer's configuration will be locked.
     */
    public abstract ObjectWriter<T> clone(File file);
    
    /**
     * Create a new object writer for the given output stream, which is of the same type and with the same configuration settings as the
     * original object writer.
     * 
     * <p>The new object writer's configuration will be locked.
     */
    public abstract ObjectWriter<T> clone(OutputStream outputStream);
    
    /**
     * Convert this object writer into a Java 8 stream collector.  This collector will
     * process the stream sequentially.
     */
    public Collector<T, ObjectWriter<T>, ObjectWriter<T>> asCollector() {
        return new Collector<T, ObjectWriter<T>, ObjectWriter<T>>() {

            /*
             * Return this object writer
             */
            @Override
            public Supplier<ObjectWriter<T>> supplier() {
                return () -> ObjectWriter.this;
            }

            /*
             * The accumulator will write objects to the writer's destination.
             */
            @Override
            public BiConsumer<ObjectWriter<T>, T> accumulator() {
                return (writer, object) -> writer.write(object);
            }

            /*
             * The combiner just returns the writer. This method will be never used
             * since the collector is sequential.
             */
            @Override
            public BinaryOperator<ObjectWriter<T>> combiner() {
                return (writer1, writer2) -> writer1;
            }

            /*
             * The finisher will close the iterator
             */
            @Override
            public Function<ObjectWriter<T>, ObjectWriter<T>> finisher() {
                return unchecked(writer -> {
                    writer.close();
                    return writer;
                });
            }

            /*
             * Return immutable empty set of characteristics. The set is empty because the
             * collector is sequential, ordered, and finisher cannot be elided.
             * 
             * @see java.util.stream.Collector.Characteristics
             */
            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
            
        };
    }

}
