package cl.core.configurable;

/**
 * Instances of this exception are thrown by methods of {@link Configurable} interface.
 * 
 * @see Configurable
 */
@SuppressWarnings("serial")
public class ConfigurableException extends RuntimeException {
    public ConfigurableException(String msg) {
        super(msg);
    }
}
