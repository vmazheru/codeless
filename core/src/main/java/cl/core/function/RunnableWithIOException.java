package cl.core.function;

import java.io.IOException;

/**
 * Represents an operation which accepts no arguments, returns no result, and may throw an IO exception. 
 */
@FunctionalInterface
public interface RunnableWithIOException {
    void run() throws IOException;
}
