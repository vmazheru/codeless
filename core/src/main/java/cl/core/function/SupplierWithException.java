package cl.core.function;

/**
 * Represents an operation which accepts no arguments, returns a result, and may throw a checked exception. 
 *  
 * @param <R> the type of the operation result
 */
@FunctionalInterface
public interface SupplierWithException<R> {
    R get() throws Exception;
}
