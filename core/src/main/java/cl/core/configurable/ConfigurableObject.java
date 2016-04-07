package cl.core.configurable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the default implementation of {@code Configurable} interface, which should be 
 * extended by any configurable object implementation if possible.
 * 
 * <p> If a configurable object implementation needs to extend another class and still needs to
 * implement {@code Configurable}, the code of this class may serve as an implementation reference.
 * 
 * @see Configurable
 */
public abstract class ConfigurableObject<C extends Configurable<C>> implements Configurable<C> {
    
    private Map<Key<?>, Object> configuration = new HashMap<>();
    private boolean locked;

    /**
     * {@inheritDoc}
     */
    //TODO: see if inheritDoc actually works and add to the rest of the methods
    @Override
    @SuppressWarnings("unchecked")
    public <T> C with(Key<T> key, T value) {
        if (locked) {
            throw new ConfigurableException("configurable object locked");
        }
        configuration.put(key, value);
        return (C)this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T)configuration.get(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public C locked() {
        locked = true;
        return (C)this;
    }
    
    /**
     * Use this method in subclasses to make sure the configuration is locked before an object's method is in use. 
     */
    protected void requireLock() {
        if (!locked) throw new ConfigurableException("configurable object not locked");
    }
    
    /**
     * Get value from configuration, or some default value if the value in configuration is NULL
     */
    @SuppressWarnings("unchecked")
    protected <T> T get(Key<T> key, T defaultValue) {
        return (T)configuration.getOrDefault(key, defaultValue);
    }
}
