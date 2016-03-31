package cl.core.configurable;

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
public interface Key<T> {}