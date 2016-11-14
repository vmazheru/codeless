package cl.core.function;

import java.io.IOException;

/**
 * Represents an operation which accepts one argument, returns no result, and may throw an IO exception. 
 *  
 * @param <T> the type of the argument to the operation
 */
@FunctionalInterface
public interface ConsumerWithIOException<T> {
    void accept(T t) throws IOException;
}
