package cl.core.configurable;

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
     * Prevent the object from further modifications.  Any attempt to use {@code with()} on a locked
     * object should throw {@code ConfigurableException}.  Also, it is recommended that the methods,
     * defined by the object, throw exceptions if used before {@code locked()} is called.
     * 
     * @return a reference to the object itself
     */
    C locked();
}
