package cl.serializers.delimited;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;
import cl.core.util.Reflections;

/**
 * Instances of this interface know how to convert (parse) arrays of strings
 * (possible generated by splitting delimited strings) into objects.
 * 
 * <p>The default parser implementation uses reflection in order to set object
 * properties, and it exposes the following configuration settings:
 * 
 * <ul>
 *  <li>
 *      {@code useSetters} of type {@code Boolean} instructs the parser to use
 *      setters instead of accessing object's fields directly. The default value
 *      is {@code FALSE}.
 *  </li>
 *  <li>
 *      {@code onPropertySetError} of type {@code Consumer<PropertySetException>} is a
 *      function which will be executed when setting a specific property results in
 *      exception. The default behavior is to re-throw that exception.
 *      
 *      @see DelimitedStringParser.PropertySetException
 *  </li>
 * </ul>
 * 
 * <p>In order to operate, the parser requires two things:
 * <ul>
 *  <li>
 *      A method to instantiate the object. This object supplier may be passed
 *      to the parser directly, or, alternatively, the object's class object
 *      may be passed.  In that case, the parser will instantiate the object by
 *      executing {@code Class.newInstance()} method.
 *  </li>
 *  <li>
 *      A map pointing string array indexes to object's properties. Properties
 *      which are not present in this map will not be set to any value.
 *  </li>
 * </ul>
 *
 * <p>Optionally, the parser may be given a map pointing object's properties
 * to functions, which will instruct the parser on how to parse values
 * for specific properties. This map does not have to cover all properties, and if
 * a property function is missing, the parser will try to set it by using reflection.
 * 
 * @param <T> Type of the object produced by parsing the given string array.
 */
public interface DelimitedStringParser<T> extends Configurable<DelimitedStringParser<T>> {
    
    /**
     * Instruct the parser to use setters instead of accessing object's fields directly.
     * The default value is {@code FALSE}
     */
    public static Key<Boolean> useSetters = new Key<>(() -> Boolean.FALSE);
    
    /**
     * A function which will be executed when setting a specific property results in
     * exception. The default behavior is to re-throw that exception.
     */
    public static Key<Consumer<PropertySetException>> onPropertySetError = 
            new Key<>(() -> (ex -> { throw ex; }));
    
    /**
     * Parse the given array of string values into an object of a specific type.
     */
    T parse(String[] values);
    
    /**
     * Get a parser instance.
     * 
     * @param objectFactory      A method to instantiate an object returned by the parser
     * @param indexToProperty    Index to property mappings (zero based)
     * @param valueParsers       Property to value parser mappings
     * @param lockConfiguration  Flag to lock or not lock the parser's configuration
     * @return                   An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty,
            Map<String, Function<String, Object>> valueParsers,
            boolean lockConfiguration) {
        DelimitedStringParser<T> p = new DelimitedStringParserImpl<>(
                objectFactory, indexToProperty, valueParsers);
        if (lockConfiguration) p.locked();
        return p;
    }
    
    /**
     * Get a parser instance.
     * 
     * @param klass             A class, which represents the type of an object returned by the parser.
     *                          It must have a default constructor.
     * @param indexToProperty   Index to property mappings (zero based)
     * @param valueParsers      Property to value parser mappings
     * @param lockConfiguration Flag to lock or not lock the parser's configuration
     * @return                  An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Class<T> klass,
            Map<Integer, String> indexToProperty,
            Map<String, Function<String, Object>> valueParsers,
            boolean lockConfiguration) {
        return get(() -> Reflections.newInstance(klass),
                indexToProperty, valueParsers, lockConfiguration);
    }
    
    /**
     * Get a parser instance with its configuration locked.
     * 
     * @param objectFactory      A method to instantiate an object returned by the parser
     * @param indexToProperty    Index to property mappings (zero based)
     * @param valueParsers       Property to value parser mappings
     * @return                   An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty,
            Map<String, Function<String, Object>> valueParsers) {
        return get(objectFactory, indexToProperty, valueParsers, true);
    }
    
    /**
     * Get a parser instance with its configuration locked.
     * 
     * @param klass             A class, which represents the type of an object returned by the parser.
     *                          It must have a default constructor.
     * @param indexToProperty   Index to property mappings (zero based)
     * @param valueParsers      Property to value parser mappings
     * @return                  An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Class<T> klass,
            Map<Integer, String> indexToProperty,
            Map<String, Function<String, Object>> valueParsers) {
        return get(klass, indexToProperty, valueParsers, true);
    }
    
    /**
     * Get a parser instance.
     * 
     * @param objectFactory      A method to instantiate an object returned by the parser
     * @param indexToProperty    Index to property mappings (zero based)
     * @param lockConfiguration  Flag to lock or not lock the parser's configuration
     * @return                   An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty,
            boolean lockConfiguration) {
        return get(objectFactory, indexToProperty, emptyMap(), lockConfiguration);
    }
    
    /**
     * Get a parser instance.
     * 
     * @param klass             A class, which represents the type of an object returned by the parser.
     *                          It must have a default constructor.
     * @param indexToProperty   Index to property mappings (zero based)
     * @param lockConfiguration Flag to lock or not lock the parser's configuration
     * @return                  An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Class<T> klass,
            Map<Integer, String> indexToProperty,
            boolean lockConfiguration) {
        return get(klass, indexToProperty, emptyMap(), lockConfiguration);
    }
    
    /**
     * Get a parser instance with its configuration locked.
     * 
     * @param objectFactory      A method to instantiate an object returned by the parser
     * @param indexToProperty    Index to property mappings (zero based)
     * @return                   An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty) {
        return get(objectFactory, indexToProperty, emptyMap(), true);
    }
    
    /**
     * Get a parser instance with its configuration locked.
     * 
     * @param klass             A class, which represents the type of an object returned by the parser.
     *                          It must have a default constructor.
     * @param indexToProperty   Index to property mappings (zero based)
     * @return                  An object, instantiated and populated by the parser
     */
    static <T> DelimitedStringParser<T> get(
            Class<T> klass,
            Map<Integer, String> indexToProperty) {
        return get(klass, indexToProperty, emptyMap(), true);
    }
    
    /**
     * Represents a run-time exception which conveys the problem of setting
     * a specific value to a specific property of an object. 
     */
    @SuppressWarnings("serial")
    public static class PropertySetException extends RuntimeException {
        
        private final String property;
        private final String value;
        private final Object object;
        
        public PropertySetException(String property, String value, Object object, Exception ex) {
            super(ex);
            this.property = property;
            this.value = value;
            this.object = object;
        }

        /**
         * A name of the property which failed to be set a value to.
         */
        public String getProperty() {
            return property;
        }

        /**
         * A string value which the parser could not parse in order to set the property.
         */
        public String getValue() {
            return value;
        }

        /**
         * An object on which we are setting the property.
         */
        @SuppressWarnings("unchecked")
        public <T> T getObject() {
            return (T)object;
        }
        
    }
}
