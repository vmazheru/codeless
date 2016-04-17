package cl.logging;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * A {@code Log} interface implementation which prints message to {@code System.out}
 */
final class Console implements Log {
    
    private final String name;
    
    public Console(Class<?> klass) {
        name = klass.getName();
    }
    
    public Console(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void trace(Supplier<String> message) {
        log("TRACE", message.get());
    }

    @Override
    public void debug(Supplier<String> message) {
        log("DEBUG", message.get());        
    }

    @Override
    public void info(Supplier<String> message) {
        log("INFO", message.get());
    }

    @Override
    public void warn(Supplier<String> message) {
        log("WARN", message.get());        
    }
    
    @Override
    public void error(Supplier<String> message) {
        log("ERROR", message.get());        
    }

    private void log(String level, String message) {
        System.out.println(level + "\t" + LocalDateTime.now() + " " + name + ": " + message);
    }
}
