package cl.core.configurable;

/**
 * Interface to which configurable objects should adhere to. 
 *
 * @param <OBJECT> should be of the class which implements {@code Configurable} (put another way,
 * configurable objects are self-bounded).  This is necessary for {@code with()} method to return
 * the reference to itself of exact type.
 */
public interface Configurable<OBJECT> {
    /**
     * Set a property value and return itself.
     * @param key    A parameterized strongly-typed property key
     * @param value  Property value
     * @return       A reference to itself (this).
     * @throws       ConfigurableException if object is locked or there is some other problem setting the value
     */
    <T> OBJECT with(Key<T> key, T value) throws ConfigurableException;
    
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
     * object should throw {@code ConfigurableException}
     * 
     * @return a reference to object itself
     */
    OBJECT locked();
}
