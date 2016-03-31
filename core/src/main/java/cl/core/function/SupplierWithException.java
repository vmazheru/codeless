package cl.core.function;

@FunctionalInterface
public interface SupplierWithException<R> {
    R get() throws Exception;
}
