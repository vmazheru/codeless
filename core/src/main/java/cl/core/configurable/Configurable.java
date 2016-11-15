package cl.core.configurable;

import java.util.Collection;
import java.util.Set;

/**
 * A {@code Configurable} object is an object who's behavior may be altered prior to its use by changing
 * the object's configuration parameters.
 * 
 * <p>A configurable object exposes its parameters via strongly typed {@link Key}s.  Every key has 
 * a name, a type, and a default value.  The default values are mandatory, so that configurable objects
 * behave sensibly even when created without altering configuration.  A key default value
 * is evaluated lazily to avoid unnecessary computations prior to actual configuration setting usage.
 * 
 * <p>Once the configuration settings are set on a configurable object, the object should be locked
 * before calling methods on it.  This behavior prevents changes in configuration while the object is being
 * operational.  The default {@code Configurable} implementation class {@link ConfigurableObject} will 
 * throw a {@link ConfigurableException} when an attempt is made to alter the object's configuration
 * after calling {@code Configurable.locked()}.  The implementations are also encouraged to throw
 * whenever an object is being used prior to calling {@code Configurable.locked()}.
 * 
 * <p>The easiest way to implement a {@code Configurable} object is to extend {@link ConfigurableObject} class,
 * and override its {@code build() } method.  The following snippet illustrates the pattern.
 * 
 * <pre>{@code
 * 
 * public class HTTPClient extends ConfigurableObject<HTTPClient> {
 *   
 *   // expose HTTPClient configuration keys
 *   public static Key<Integer> socketTimeout = new Key<>(() -> 10_000){}
 *   public static Key<Boolean> logging       = new Key<>(() -> false){}
 * 
 *   //Override
 *   protected void build() {
 *    // initialize HTTPClient according to the configuration settings available via calling
 *    // get(key) method.  Assume, that all configuration values has been set at this point.
 *   }
 *   
 *   public HTTPResponse executeGet(String url) {
 *     // execute GET request, and return some HTTPResponse object
 *   }
 *   
 *   // more methods ...
 * }
 * 
 * // create and use your object
 * 
 * HTTPClient c = new HTTPClient()
 *                  .with(socketTimeout, 60_000) // change default socket timeout
 *                  .with(logging, true)         // change default logging flag
 *                  .locked();                   // lock configuration (calls build())
 * 
 * c.executeGet(myUrl);
 * 
 * }</pre>
 
 * @param <C> the type of the object itself.  This is necessary in order to make methods like locked() and 
 *            with() return the reference to the object itself.
 * 
 * @see ConfigurableObject
 * @see Key
 * @see ConfigurableException
 */

public interface Configurable<C extends Configurable<C>> {
    
    /**
     * Set a value for the given configuration key.
     * 
     * @param key    configuration key
     * @param value  configuration value
     * @return       a reference to object itself
     * @throws       ConfigurableException if the object is locked or there is some other problem setting the value
     */
    <T> C with(Key<T> key, T value);
    
    /**
     * Get a configuration value.  It might return a value set by {@code with} or the default value (which
     * may be null).
     * 
     * @param key configuration key
     * @return    configuration value
     */
    <T> T get(Key<T> key);
    
    /**
     * Return a set of keys for this configurable object.  Calling this method does not depend on weather
     * the object is locked or unlocked.
     */
    Set<Key<?>> keys();
    
    /**
     * Set all configuration values from a different configurable object.  This may be useful when objects
     * of different or the same type share the same set of configuration keys and values.
     * 
     * <p>If the target object is locked, the {@code ConfigurableException} will be thrown.  The
     * other object doesn't have to be locked.
     * 
     *  @return a reference to itself
     *  @throws ConfigurableException if the object is locked or there is some other problem setting the values
     */
    C withConfigurationFrom(Configurable<?> other);
    
    /**
     * Prevent the object's configuration from further modifications.  Any attempt to use {@code with()} on a locked
     * object should throw {@code ConfigurableException}.  Also, it is recommended that the methods,
     * defined by the object, throw exceptions if used before {@code locked()} is called.
     * 
     * @return a reference to the object itself
     */
     C locked();
     
     /**
      * Convert a set of configuration keys with their default values into a {@code Configurable} object.
      * This method is useful when a collection of configuration keys has to be passed around as one
      * object. The returned {@code Configurable} object is locked.
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
