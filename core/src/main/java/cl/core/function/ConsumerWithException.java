package cl.core.function;

/**
 * Represents an operation which accepts one argument, returns no result, and may throw a checked exception. 
 *  
 * @param <T> the type of the argument to the operation
 */
@FunctionalInterface
public interface ConsumerWithException<T> {
    void accept(T t) throws Exception;
}
