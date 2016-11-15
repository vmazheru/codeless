package cl.core.configurable;

import java.util.function.Supplier;

/**
 * A strongly-typed key for {@link Configurable} objects.
 * 
 * <p> The implementations of this interface are usually small enumeration-like anonymous classes
 * (see an example in {@link Configurable} description), but may be more involved if necessary.
 * 
 * @param <T> type of the key
 * 
 * @see Configurable
 */
public class Key<T> {
    
    private final Supplier<T> defaultValue;
    
    /**
     * The constructor of the key accepts a {@code Supplier} instead of the actual value as its 
     * default value.  This facilitates lazy evaluation of the default value whenever the key is used
     * and no other value has been set.
     */
    public Key(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * Get the default value.  This method will evaluate it.
     */
    public T getDefaultValue() {
        return defaultValue.get();
    }
}