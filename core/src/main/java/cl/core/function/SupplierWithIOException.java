package cl.core.function;

import java.io.IOException;

@FunctionalInterface
public interface SupplierWithIOException<R> {
    R get() throws IOException;
}
