package cl.json;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;

/**
 * <p>This class helps serialize / deserialize objects to / from JSON strings.
 * 
 * <p>The behavior of this class depends completely on Java reflection, so a default constructor
 * must be present in a class, instances of which we read from JSON. Also, to set
 * object's properties, this class may access either object's private fields directly (default) or 
 * use setters/getters (see {@code visibility} configuration key description below).
 */
public interface JsonMapper extends Configurable<JsonMapper> {

    /**
     * Serialize an object to JSON string.
     */
    String toJson(Object o) throws JsonMapperException;
    
    /**
     * Read an object from a JSON string. 
     */
    <T> T fromJson(String json, Class<T> klass) throws JsonMapperException;
    
    /**
     * Create a mapper with configuration still unlocked, so the client can adjust it.
     */
    static JsonMapper getJsonMapper(boolean lockConfiguration) {
        JsonMapper m  = new JsonMapperImpl();
        if (lockConfiguration) m.locked();
        return m;
    }

    /**
     * Create a mapper with locked default configuration.
     */
    static JsonMapper getJsonMapper() {
        return getJsonMapper(true);
    }

    /**
     * The mapper can access object data by either accessing its fields directly or
     * calling accessors/mutators.  The default behavior is to use fields.
     */
    public enum Visibility { FIELD, METHOD }
    
    /**
     * Configuration key which defines the way reflection is used to access object's data.
     * Possible values are {@code Visibility.FIELD} and {@code Visibility.METHOD}.
     * Default value is {@code Visibility.FIELD}.
     */
    static Key<Visibility> visibility                = new Key<Visibility>(){};
    
    /**
     * Configuration key which instructs the mapper to fail/not fail when it finds unknown properties
     * in a JSON string.  Default value is {@code false} (don't fail).
     */
    static Key<Boolean>    failOnUnknownProperties   = new Key<Boolean>(){};
    
    /**
     * Configuration key which enables/disables JSON indentation. Default value is {@code false} (no pretty printing).
     */
    static Key<Boolean>    prettyPrinting            = new Key<Boolean>(){};
    
    /**
     * Configuration key which instructs the mapper to wrap JSON string into a root element, the name
     * of which is usually an object's class name.  Default value is {@code false} (don't wrap).
     */
    static Key<Boolean>    wrapRootValue             = new Key<Boolean>(){};
    
    /**
     * Configuration key which instructs the mapper to unwrap single-element arrays into standalone values.
     * Default value is {@code false} (don't unwrap).
     */
    static Key<Boolean>    unwrapSingleElementArrays = new Key<Boolean>(){};
    
    /**
     * Unchecked exception which might be thrown by the class operations, unless these operations
     * don't come from {@code Configurable} interface, in which case {@code ConfigurableException} is thrown.
     */
    @SuppressWarnings("serial")
    public static class JsonMapperException extends RuntimeException {
        public JsonMapperException(Throwable cause) {
            super(cause);
        }
    }
    
}