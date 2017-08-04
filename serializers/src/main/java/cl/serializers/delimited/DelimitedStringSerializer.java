package cl.serializers.delimited;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.function.Function;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;

/**
 * Objects of this class convert objects into arrays of strings.
 * 
 * <p>The class makes use the following configuration settings:
 * 
 * <ul>
 *   <li>{@code useGetters} instructs the serializer to use getters instead of directly accessing fields</li>
 *   <li>
 *      {@code exactProperties} tells the serializer to use properties from the given
 *      index-to-property map and ignore other object's properties
 *   </li>
 * </ul>
 *
 * @param <T> type of the input object
 */
public interface DelimitedStringSerializer<T> extends Configurable<DelimitedStringSerializer<T>> {
    
    /**
     * Instructs the serializer to use getters instead of directly accessing fields
     */
    public static Key<Boolean> useGetters = new Key<>(() -> Boolean.FALSE);
    
    /**
     * Tells the serializer to use properties from the given
     * index-to-property map and ignore other object's properties.
     */
    public static Key<Boolean> exactProperties = new Key<>(() -> Boolean.FALSE);

    /**
     * Serialize the given object into array of strings. The order of the
     * strings in this array is defined by the given "indexToProperty" map
     * supplied to the constructor and/or the order of the fields (getters)
     * returned by Java reflection.
     */
    String[] serialize(T obj);
    
    /**
     * Get a serializer instance.
     * 
     * @param indexToProperty    a map where keys are indexes in the resulting string array (zero based),
     *                           and values are object properties
     * @param valueSerializers   a map where keys are object properties and values are
     *                           functions used to convert property values to strings
     * @param lockConfiguration  flag indicating whether the configuration should be locked or unlocked
     */
    static <T> DelimitedStringSerializer<T> get(
            Map<Integer, String> indexToProperty,
            Map<String, Function<Object, String>> valueSerializers,
            boolean lockConfiguration) {
        DelimitedStringSerializer<T> s = new DelimitedStringSerializerImpl<>(
                indexToProperty, valueSerializers);
        if (lockConfiguration) s.locked();
        return s;
    }
    
    /**
     * Get a serializer instance with its configuration locked.
     * 
     * @param indexToProperty    a map where keys are indexes in the resulting string array (zero based),
     *                           and values are object properties
     * @param valueSerializers   a map where keys are object properties and values are
     *                           functions used to convert property values to strings
     */
    static <T> DelimitedStringSerializer<T> get(
            Map<Integer, String> indexToProperty,
            Map<String, Function<Object, String>> valueSerializers) {
        return get(indexToProperty, valueSerializers, true);
    }
    
    /**
     * Get a serializer instance.
     * 
     * @param indexToProperty    a map where keys are indexes in the resulting string array (zero based),
     *                           and values are object properties
     */
    static <T> DelimitedStringSerializer<T> get(
            Map<Integer, String> indexToProperty,
            boolean lockConfiguration) {
        return get(indexToProperty, emptyMap(), lockConfiguration);
    }
    
    /**
     * Get a serializer instance with its configuration locked.
     * 
     * @param indexToProperty    a map where keys are indexes in the resulting string array (zero based),
     *                           and values are object properties
     */
    static <T> DelimitedStringSerializer<T> get(Map<Integer, String> indexToProperty) {
        return get(indexToProperty, emptyMap(), true);
    }
    
    /**
     * Get a serializer instance.
     */
    static <T> DelimitedStringSerializer<T> get(boolean lockConfiguration) {
        return get(emptyMap(), emptyMap(), lockConfiguration);
    }
    
    /**
     * Get a serializer instance with its configuration locked.
     */
    static <T> DelimitedStringSerializer<T> get() {
        return get(emptyMap(), emptyMap(), true);
    }
}
