package cl.core.configurable;

import java.util.function.Supplier;

/**
 * A strongly-typed key for {@code Configurable} objects.
 * 
 * <p> The implementations of this interface are usually small enumeration-like anonymous classes
 * (see an example in the package description), but may be more involved if necessary.
 * 
 * @param <T> The type of the key.
 * 
 * @see Configurable
 */
public class Key<T> {
    
    private final Supplier<T> defaultValue;
    
    public Key(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public T getDefaultValue() {
        return defaultValue.get();
    }
}