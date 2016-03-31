package cl.core.function;

import java.io.IOException;

@FunctionalInterface
public interface BiConsumerWithIOException<T, U> {
    void accept(T t, U u) throws IOException;
}
