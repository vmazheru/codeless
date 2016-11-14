package cl.core.function;

/**
 * Represents an operation which accepts no arguments, returns no result, and may throw a checked exception. 
 */
@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
