package cl.serializers;

/**
 * Enumeration of supported serialization types.
 */
public enum SerializationType {
    
    /**
     * Serialization type which makes use of Java serialization mechanism
     */
    JAVA,
    
    /**
     * Serialization type which converts objects to and from JSON reflectively. Serializers
     * of this type use {@link cl.json.JsonMapper} implementations internally.
     */
    JSON,
    
    /**
     * Serialization type which uses delimited string format (CSV etc) to
     * serialize objects.
     */
    DELIMITED,
    
    /**
     * Serializers of this type operate on strings, and can be used to read/write plain
     * text files. 
     */
    STRING
}
