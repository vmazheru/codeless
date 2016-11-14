package cl.core.function;

import java.io.IOException;

/**
 * Represents an operation which accepts two arguments, returns no result, and may throw an IO exception. 
 *  
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 */
@FunctionalInterface
public interface BiConsumerWithIOException<T, U> {
    void accept(T t, U u) throws IOException;
}
