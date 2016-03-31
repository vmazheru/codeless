package cl.core.function;

@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
