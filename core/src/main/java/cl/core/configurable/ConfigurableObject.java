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
        T value = (T)configuration.get(key);
        return value != null ? value : key.getDefaultValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public C locked() {
        build();
        locked = true;
        return (C)this;
    }
    
    /**
     * This method is called from {@code locked()} in order to make sure the object is
     * properly built before being locked.  The default implementation does nothing.
     */
    protected void build() {}
    
    /**
     * Use this method in subclasses to make sure the configuration is locked before an object's method is in use. 
     */
    protected void requireLock() {
        if (!locked) throw new ConfigurableException("configurable object not locked");
    }
}
