package cl.core.function;

import java.io.IOException;

/**
 * Represents an operation which accepts one argument, returns a result, and may throw an IO exception. 
 *  
 * @param <T> the type of the first argument to the operation
 * @param <R> the type of the operation result
 */
@FunctionalInterface
public interface FunctionWithIOException<T, R> {
    R apply(T t) throws IOException;
}
