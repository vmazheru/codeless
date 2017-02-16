package cl.logging;

import java.util.function.Supplier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A Log4j implementation of {@link Log} interface.
 */
final class Log4jLog implements Log {
    
    private final Logger logger;
    
    public Log4jLog(String name) {
        logger = Logger.getLogger(name);
    }
    
    public Log4jLog(Class<?> klass) {
        logger = Logger.getLogger(klass);
    }    

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void trace(Supplier<String> message) {
        if(logger.isTraceEnabled()) logger.trace(message.get());
    }

    @Override
    public void debug(Supplier<String> message) {
        if(logger.isDebugEnabled()) logger.debug(message.get());
        
    }

    @Override
    public void info(Supplier<String> message) {
        if(logger.isInfoEnabled()) logger.info(message.get());
    }

    @Override
    public void warn(Supplier<String> message) {
        if(logger.isEnabledFor(Level.WARN)) logger.warn(message.get());  
    }

    @Override
    public void error(Supplier<String> message) {
        if(logger.isEnabledFor(Level.ERROR)) logger.error(message.get());
    }
    
    @Override
    public void error(Supplier<String> message, Throwable error) {
        if(logger.isEnabledFor(Level.ERROR)) logger.error(message.get(), error);
    }

}
