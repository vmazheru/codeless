package cl.core.configurable;

import java.util.Collection;
import java.util.Set;

/**
 * Interface to which configurable objects should adhere to. 
 */
public interface Configurable<C extends Configurable<C>> {
    /**
     * Set a property value and return itself.
     * @param key    A parameterized strongly-typed property key
     * @param value  Property value
     * @return       A reference to itself (this).
     * @throws       ConfigurableException if object is locked or there is some other problem setting the value
     */
    <T> C with(Key<T> key, T value);
    
    /**
     * Get a property value.  It might return a value set by {@code with}, a default value, or null
     * depending on the implementation.
     * 
     * @param key A parameterized strongly-typed property key
     * @return    Property value
     */
    <T> T get(Key<T> key);
    
    /**
     * Return a set of keys, currently configured.  The object doesn't have to be locked for this
     * method to return successfully.
     */
    Set<Key<?>> keys();
    
    /**
     * Set all configuration values from a different object.  This may be useful when objects
     * of different types share the same set of configuration keys and values.<br/>
     * If the target object is locked, the {@code ConfigurableException} will be thrown.  The
     * other object doesn't have to be locked.
     * 
     *  @return a reference to itself
     */
    C withConfigurationFrom(Configurable<?> other);
    
    /**
     * Prevent the object from further modifications.  Any attempt to use {@code with()} on a locked
     * object should throw {@code ConfigurableException}.  Also, it is recommended that the methods,
     * defined by the object, throw exceptions if used before {@code locked()} is called.
     * 
     * @return a reference to the object itself
     */
     C locked();
     
     /**
      * <p>
      * Convert a set of configuration keys with their default values into a {@code Configurable} object.
      * This method is useful whenever a collection of configuration keys has to be passed around as one
      * object.
      * </p>
      * <p>
      * The returned {@code Configurable} object is locked.
      * </p>
      */
     static Configurable<?> defaultConfiguration(Collection<Key<?>> keys) {
         Configurable<?> configurable = empty();
         keys.forEach(key -> configurable.with(key, null));
         return configurable.locked();
     }
     
     /**
      * Return an empty unlocked configuration.
      */
     static Configurable<?> empty() {
         return new ConfigurationHolder();
     }
     
}
