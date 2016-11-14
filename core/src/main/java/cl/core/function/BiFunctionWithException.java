package cl.core.function;

/**
 * Represents an operation which accepts two arguments, returns a result, and may throw a checked exception. 
 *  
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <R> the type of the operation result
 */
@FunctionalInterface
public interface BiFunctionWithException<T, U, R> {
    R apply(T t, U u) throws Exception;
}
