package cl.logging;

import static cl.core.util.Exceptions.*;

import java.util.function.Supplier;

/**
 * A simple wrapper around logging implementations (including System.out).
 * The default implementation is Log4j.
 */
public interface Log {

    /**
     * Available Log implementations
     */
    enum Implementation { CONSOLE, JAVA, LOG4J }
    
    /**
     * A Log object's name
     */
    String getName();

    /**
     * Print a trace-level message.
     */
    void trace(Supplier<String> message);
    
    /**
     * Print a debug-level message.
     */
    void debug(Supplier<String> message);

    /**
     * Print an info-level message.
     */
    void info(Supplier<String> message);
    
    /**
     * Print a warning.
     */
    void warn(Supplier<String> message);
    
    /**
     * Print an error.
     */
    void error(Supplier<String> message);
    
    /**
     * Print an error. <br/>
     * This method will evaluate the message before sending to the logging system.
     */
    default void error(String message) {
        error(() -> message);
    }

    /**
     * Print an error message and an exception stack trace.
     */
    default void error(Supplier<String> message, Throwable error) {
        error(message.get(), error);
    }
    
    /**
     * Print an error message and an exception stack trace.
     * <br/> This method will evaluate the message before sending to the logging system.
     */
    default void error(String message, Throwable error) {
        error(new StringBuilder(message).append(System.lineSeparator()).append(getStackTrace(error)).toString());
    }
    
    /**
     * Print an exception message and a stack trace.
     */
    default void error(Throwable error) {
        error(() -> error.toString(), error);
    }
    
    /**
     * Get a default logger for a given class. If the default logger cannot be
     * instantiated, Java logger will be instantiated.
     */
    static Log getLog(Class<?> klass) {
        try {
            return (Log)Class.forName("cl.logging.Log4jLog")
                .getConstructor(Class.class).newInstance(klass);
        } catch (Exception e) {
            System.out.println("Log4j log implementation class 'cl.logging.Log4jLog' is not found in the classpath. "
                    + "Will default to Java logging implementation.");
            return new JavaLog(klass);
        }
    }
    
    /**
     * Get a default logger for a given name. If the default logger cannot be
     * instantiated, Java logger will be instantiated.
     */
    static Log getLog(String name) {
        try {
            return (Log)Class.forName("cl.logging.Log4jLog")
                .getConstructor(String.class).newInstance(name);
        } catch (Exception e) {
            System.out.println("Log4j log implementation class 'cl.logging.Log4jLog' is not found in the classpath. "
                    + "Will default to Java logging implementation.");
            return new JavaLog(name);
        }
    }

    /**
     * Get an instance of a logger of the given implementation for the given class.
     */
    static Log getLog(Class<?> klass, Implementation impl) {
        switch (impl) {
            case LOG4J   : return getLog(klass);
            case JAVA    : return new JavaLog(klass);
            case CONSOLE :
            default      : return getConsole(klass);
        }
    }    

    /**
     * Get an instance of a logger of the given implementation for the given name.
     */
    static Log getLog(String name, Implementation impl) {
        switch (impl) {
            case LOG4J   : return getLog(name);
            case JAVA    : return new JavaLog(name);
            case CONSOLE :
            default      : return getConsole(name);
        }
    }
    
    /**
     * Get a console for the given name.
     */
    static Log getConsole(String name) {
        return new Console(name);
    }

    /**
     * Get a console for the given class.
     */
    static Log getConsole(Class<?> klass) {
        return new Console(klass);
    }
    
}
