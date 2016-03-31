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
 * @param <O> The type of an actual configurable object (self-bounded).
 * @see Configurable
 */
public abstract class ConfigurableObject<O> implements Configurable<O> {
    
    private Map<Key<?>, Object> configuration = new HashMap<>();
    private boolean locked;

    /**
     * {@inheritDoc}
     */
    //TODO: see if inheritDoc actually works and add to the rest of the methods
    @Override
    @SuppressWarnings("unchecked")
    public <T> O with(Key<T> key, T value) {
        if (locked) {
            throw new ConfigurableException("configurable object locked");
        }
        configuration.put(key, value);
        return (O)this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T)configuration.get(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public O locked() {
        locked = true;
        return (O)this;
    }
}
