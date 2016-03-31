package cl.core.configurable;

/**
 * Instances of this exception might thrown by methods of {@code Configurable} interface.
 * 
 * @see Configurable
 */
@SuppressWarnings("serial")
public class ConfigurableException extends RuntimeException {
    public ConfigurableException(String msg) {
        super(msg);
    }
}
