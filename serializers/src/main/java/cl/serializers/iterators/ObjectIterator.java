package cl.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static java.util.Spliterator.*;
import static java.util.stream.Collectors.*;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cl.core.configurable.ConfigurableException;
import cl.core.configurable.ConfigurableObject;

/**
 * This class defines common functionality available in all object iterator implementations.  See 
 * package description for usage examples.
 * 
 * <p>The actual {@code Iterator} methods {@code hasNext()} and {@code next()} are made final, and
 * concrete implementations must implement {@code readNext()} method.  (There are also two 
 * {@code clone()} methods, which concrete implementations must implement, but implementing these methods
 * is trivial).
 * 
 * <p>{@link ObjectIterator} is a {@link ConfigurableObject}, so the implementations can feel free
 * to implement any configuration logic they desire.  Final methods {@code hasNext()} and {@code next()}
 * make sure that any operation on an unlocked configurable object iterator will result in {@link ConfigurableException}.
 * 
 *  <p>{@link ObjectIterator} also implements {@code Closeable} interface and may be used in Java 'try-with-resource' construct.

 * @param <T> Type of objects which an object iterator reads from its source. This may be some custom
 * class for {@link JavaIterator} or {@link JsonIterator}, or {@code String} for {@link StringIterator}.
 */
public abstract class ObjectIterator<T> extends ConfigurableObject<ObjectIterator<T>> implements Iterator<T>, Closeable {

    private T next;
    private boolean justOpen = true;
    
    /**
     * Check if more items are available in this iterator.
     * 
     * @throws ConfigurableException whenever configuration for this object is not locked.
     */
    @Override
    public final boolean hasNext() {
        requireLock();
        tryNextOnJustOpen();
        return next != null;
    }
    
    /**
     * Return the next element in this iterator.
     * 
     * @throws ConfigurableException whenever configuration for this object is not locked.
     * @throws NoSuchElementException when called on an iterator with no more elements available.
     */
    @Override
    public final T next() {
        requireLock();
        tryNextOnJustOpen();
        if (next == null) throw new NoSuchElementException();
        T n = next;
        tryNext();
        return n;
    }
    
    /**
     * Return the next {@code numObjects} elements from the source.  If the iterator has less elements, than
     * specified by the parameter, it will return as many items as are available.
     * 
     * @throws ConfigurableException whenever the configuration for this object is not locked.
     * @param numObjects How many elements to read
     * @return A list of at most {@code numObjects} elements read from the source.
     */
    public final List<T> next(int numObjects) {
        List<T> result = new ArrayList<>(numObjects);
        int count = 0;
        while (hasNext() && count++ < numObjects) {
            result.add(next());
        }
        return result;
    }
    
    /**
     * Read objects, collect them to lists of the given size, and execute a function on each list.
     */
    public final void forEachBatch(int batchSize, Consumer<List<T>> f) {
        List<T> batch = null;
        while (!(batch = next(batchSize)).isEmpty()) {
            f.accept(batch);
        }
    }
    
    /**
     * Convert this iterator to a stream.  The stream obtained by calling this method is ordered and sequential
     * (can't be parallelized).
     * 
     * @throws ConfigurableException whenever configuration for this object is not locked.
     */
    public final Stream<T> stream() {
        requireLock();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, ORDERED | NONNULL | IMMUTABLE), false);
    }
    
    /**
     * Read all objects and return them in a list.
     * 
     * @throws ConfigurableException whenever configuration for this object is not locked.
     */
    public final List<T> read() {
        return stream().collect(toList());
    }

    /**
     * Create a new object iterator of the same type with the same configuration settings for 
     * the given file. The return object's configuration must be locked, so it is ready to use.
     * 
     * <p>The implementation of this method should be as simple as:
     * 
     * <pre>{@code
     *    return JavaIterator.<T>fromFile(file, false).withConfigurationFrom(this).locked();
     * }</pre>
     * 
     */
    public abstract ObjectIterator<T> clone(File file);
    
    /**
     * Create a new object iterator of the same type with the same configuration settings for 
     * the given input stream.  The return object's configuration must be locked, so it is ready to use.
     * 
     * <p>The implementation of this method should be as simple as:
     * 
     * <pre>{@code
     *    return JavaIterator.<T>fromInputStream(inputStream, false).withConfigurationFrom(this).locked();
     * }</pre>
     * 
     */
    public abstract ObjectIterator<T> clone(InputStream inputStream);
    
    /**
     * Read the next element from the iterator source.  The implementation of this method is iterator 
     * format specific.
     */
    protected abstract T readNext() throws Exception;
    
    private void tryNextOnJustOpen() {
        if (justOpen) {
            justOpen = false;
            tryNext();
        }
    }
    
    private void tryNext() {
        uncheck(() -> {
           next = readNext(); 
        });
    }
}
