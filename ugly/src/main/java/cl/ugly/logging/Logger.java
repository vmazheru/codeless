package cl.ugly.logging;

import java.util.function.Supplier;

public interface Logger {

    void trace (Supplier<String> message);
    void debug (Supplier<String> message);
    void info  (Supplier<String> message);
    void warn  (Supplier<String> message);
    void warn  (Supplier<String> message, Throwable error);
    void error (Supplier<String> message);
    void error (Supplier<String> message, Throwable error);
    
    static Logger getJavaLogger(String name) {
        return JavaLogger.getLogger(name);
    }
    
}
