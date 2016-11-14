package cl.core.function;

/**
 * Represents an operation which accepts one argument, returns a result, and may throw a checked exception. 
 *  
 * @param <T> the type of the first argument to the operation
 * @param <R> the type of the operation result
 */
@FunctionalInterface
public interface FunctionWithException<T, R> {
    R apply(T t) throws Exception;
}
