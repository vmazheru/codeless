package cl.core.function;

/**
 * Represents an operation which accepts two arguments, returns no result, and may throw a checked exception. 
 *  
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 */
@FunctionalInterface
public interface BiConsumerWithException<T, U> {
    void accept(T t, U u) throws Exception;
}
