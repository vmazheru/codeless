package cl.core.function;

@FunctionalInterface
public interface BiFunctionWithException<T, U, R> {
    R apply(T t, U u) throws Exception;
}
