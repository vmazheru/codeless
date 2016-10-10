package cl.core.configurable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public final <T> C with(Key<T> key, T value) {
        requireNotLocked();
        configuration.put(key, value);
        return (C)this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T get(Key<T> key) {
        T value = (T)configuration.get(key);
        return value != null ? value : key.getDefaultValue();
    }
    
    @Override
    public final Set<Key<?>> keys() {
        return Collections.unmodifiableSet(configuration.keySet());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final C withConfigurationFrom(Configurable<?> other) {
        requireNotLocked();
        for (Key<?> key : other.keys()) {
            configuration.put(key, other.get(key));
        }
        return (C)this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final C locked() {
        if (!locked) {
            build();
            locked = true;
        }
        return (C)this;
    }
    
    /**
     * This method is being called from {@code locked()}. Subclasses may override
     * this method in order to provide additional initialization logic before locking object's
     * configuration.  The default implementation does nothing.
     */
    protected void build() {}
    
    /**
     * Use this method in subclasses to make sure the configuration is locked before an object's method is in use. 
     */
    protected void requireLock() {
        if (!locked) throw new ConfigurableException("configurable object not locked");
    }
    
    private void requireNotLocked() {
        if (locked) throw new ConfigurableException("configurable object locked");
    }
}
