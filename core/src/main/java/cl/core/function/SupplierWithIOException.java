package cl.core.function;

import java.io.IOException;

/**
 * Represents an operation which accepts no arguments, returns a result, and may throw an IO exception. 
 *  
 * @param <R> the type of the operation result
 */
@FunctionalInterface
public interface SupplierWithIOException<R> {
    R get() throws IOException;
}
