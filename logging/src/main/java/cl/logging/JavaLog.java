package cl.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class is a Java logging's implementation of the {@code Log} interface.<br/>
 * The class will attempt to load Java logging configuration from "logging.properties" file first, 
 * and if not found will use default Java logging configuration.
 */
final class JavaLog implements Log {
    
    private static final String JAVA_LOGGER_CONFIG_FILE = "logging.properties";
    private static boolean javaLoggingInitialized;

    private final Logger logger;
    
    public JavaLog(String name) {
        synchronized (JavaLog.class) {
            if (!javaLoggingInitialized) {
                initializeJavaLogging();
                javaLoggingInitialized = true;
            }
        }
        logger = Logger.getLogger(name);
    }
    
    public JavaLog(Class<?> klass) {
        this(klass.getName());
    }
    
    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void trace(Supplier<String> message) {
        logger.finest(message);
    }

    @Override
    public void debug(Supplier<String> message) {
        logger.fine(message);
    }

    @Override
    public void info(Supplier<String> message) {
        logger.info(message);
    }

    @Override
    public void warn(Supplier<String> message) {
        logger.warning(message);
    }

    @Override
    public void error(Supplier<String> message) {
        logger.severe(message);
    }

    private static void initializeJavaLogging() {
        try(InputStream in = JavaLog.class.getResourceAsStream(JAVA_LOGGER_CONFIG_FILE)) {
            if(in == null) {
                String msg = 
                        "No " + JAVA_LOGGER_CONFIG_FILE + " is found in the classpath. " + 
                        "The default java logging properties file will be used"; 
                java.util.logging.Logger.getLogger(JavaLog.class.getName()).warning(msg);
                System.out.println(msg);
            } else {
                LogManager logManager = LogManager.getLogManager();
                logManager.readConfiguration(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
