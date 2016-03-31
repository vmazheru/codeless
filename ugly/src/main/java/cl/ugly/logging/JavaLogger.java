package cl.ugly.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;

final class JavaLogger implements Logger {
    
    private final java.util.logging.Logger javaLogger;
    
    private JavaLogger(java.util.logging.Logger javaLogger) {
        this.javaLogger = javaLogger;
    }

    @Override
    public void trace(Supplier<String> message) {
        javaLogger.finest(message);
    }

    @Override
    public void debug(Supplier<String> message) {
        javaLogger.fine(message);
    }

    @Override
    public void info(Supplier<String> message) {
        javaLogger.info(message);
    }

    @Override
    public void warn(Supplier<String> message) {
        javaLogger.warning(message);
    }

    @Override
    public void error(Supplier<String> message) {
        javaLogger.severe(message);
    }

    @Override
    public void warn(Supplier<String> message, Throwable error) {
        javaLogger.log(Level.WARNING, error, message);
    }

    @Override
    public void error(Supplier<String> message, Throwable error) {
        javaLogger.log(Level.SEVERE, error, message);
    }
    
    private static final String JAVA_LOGGER_CONFIG_FILE = "logging.properties";
    private static boolean javaLoggingInitialized;
    
    static Logger getLogger(String name) {
        synchronized(JavaLogger.class) {
            if(!javaLoggingInitialized) {
                initializeJavaLogging();
            }
        }
        java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger(name);
        return new JavaLogger(javaLogger);
    }
    
    private static void initializeJavaLogging() {
        try(InputStream in = JavaLogger.class.getResourceAsStream(JAVA_LOGGER_CONFIG_FILE)) {
            if(in == null) {
                String msg = "No " + JAVA_LOGGER_CONFIG_FILE + " is found in class path. " + 
                        "The default java logging properties file will be used"; 
                java.util.logging.Logger.getLogger(JavaLogger.class.getName()).warning(msg);
                System.out.println(msg);
            } else {
                LogManager logManager = LogManager.getLogManager();
                logManager.readConfiguration(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        javaLoggingInitialized = true;
    }

}
