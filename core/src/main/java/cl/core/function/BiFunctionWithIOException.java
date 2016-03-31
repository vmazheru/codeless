package cl.core.function;

import java.io.IOException;

@FunctionalInterface
public interface BiFunctionWithIOException<T, U, R> {
    R apply(T t, U u) throws IOException;
}
