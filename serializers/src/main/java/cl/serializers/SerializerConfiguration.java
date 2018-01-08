package cl.serializers;

import static cl.core.configurable.Configurable.configurationWith;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;
import cl.core.util.Strings;
import cl.json.JsonMapper;
import cl.serializers.delimited.DelimitedStringJoiner;
import cl.serializers.delimited.DelimitedStringParser;
import cl.serializers.delimited.DelimitedStringSerializer;
import cl.serializers.delimited.DelimitedStringSplitter;
import cl.serializers.delimited.DelimitedStringParser.PropertySetException;
import cl.serializers.iterators.DelimitedStringIterator;


/**
 * This class contains all configuration keys (and their default values), which can be used with serializers.
 */
public class SerializerConfiguration {

    /**
     * Instruct a text-based serializer to skip empty lines. The default value is {@code false}.
     * <p>With most of iterators which use this key (JSON, delimited, etc), there is
     * no need to set this key to TRUE, if the files are well-formed.
     */
    public final static Key<Boolean> skipEmptyLines = new Key<>(() -> false);
    
    /**
     * Set a character set on the text- based serializer. The default value is UTF-8.
     */
    public final static Key<Charset> charset = new Key<>(() -> StandardCharsets.UTF_8);
    
    /**
     * Instruct a text-based serializer on how many lines in the file belongs to the file header.
     * This setting should be consistent with {@link SerializerConfiguration#headerLines} value.
     * The setting informs {@link cl.serializers.iterators.ObjectIterator} on how many lines 
     * to skip before processing the data. The default value is 0;
     */
    public final static Key<Integer> numHeaderLines = new Key<>(() -> 0);
    
    /**
     * Set a list of strings which represent header lines in the text file. These lines will be
     * written by a text-based {@link cl.serializers.writers.ObjectWriter} before any data.
     * The default value is an empty list.
     */
    public final static Key<List<String>> headerLines = new Key<>(() -> Collections.emptyList());
    
    /**
     * Set a callback which will be executed on every line in the file header when reading
     * data by a text-based {@link cl.serializers.iterators.ObjectIterator}. 
     * The default value is a function which does nothing.
     */
    public final static Key<BiConsumer<Integer, String>> onHeader = new Key<>(() -> (i, s) -> {});
    
    /**
     * Set a custom version of JSON mapper.  This value is used by JSON serializers.  The default
     * value is a JSON mapper object with its default configuration settings.
     * 
     * @see cl.json.JsonMapper
     */
    public final static Key<JsonMapper> jsonMapper = new Key<>(() -> JsonMapper.getJsonMapper());
    
    /**
     * Used by {@link DelimitedStringIterator}. This key sets an object
     * responsible for splitting lines by delimiters. CSV splitter is the 
     * default value.
     * 
     * @see cl.serializers.delimited.DelimitedStringSplitter
     */
    public final static Key<DelimitedStringSplitter> delimitedStringSplitter = 
            new Key<>(() -> DelimitedStringSplitter.csv());
    
    /**
     * Used by {@link DelimitedStringIterator} and {@DelimitedStringWriter}. 
     * This key sets a strategy for parsing file header in order to establish mappings between
     * columns and object properties.
     * 
     * <p>Converting strings with spaces to camel case is the default strategy.
     * 
     * <p>This key will not override explicit mappings between column
     * indexes and object properties given as {@code columnIndexToProperty} key value.
     * 
     * @see SerializerConfiguration#columnIndexToProperty
     */
    public final static Key<Function<String, String>> columnToProperty =
            new Key<>(() -> s -> Strings.spacedToCamel(s));
    
    /**
     * Used by {@link DelimitedStringIterator} and {@DelimitedStringWriter}.
     * This key sets a mapping between columns and object properties. 
     * It does not have to cover all columns in the delimited file. 
     * The missing mappings will be filled by applying the 'columnToProperty' setting. 
     * It is zero based.
     * 
     * <p>An empty map is the default value.
     * 
     * @see SerializerConfiguration#columnToProperty
     */
    public final static Key<Map<Integer, String>> columnIndexToProperty =
            new Key<>(() -> Collections.emptyMap());
    
    /**
     * Used by {@link DelimitedStringIterator}. This key sets custom
     * parsers for certain object properties.  
     * 
     * <p>By default, a delimited
     * string parser is able to parse strings, all primitive types, dates, 
     * big integers, and big decimals. But if a property is of some other type,
     * a custom parser is required.
     * 
     * <p>The property maps object property names to custom parsers.  If no
     * parser for a property is given by this mapping, a default parser will
     * be used.
     * 
     * @see cl.serializers.delimited.DelimitedStringParser
     */
    public final static Key<Map<String, Function<String, Object>>> valueParsers =
            new Key<>(() -> Collections.emptyMap());
    
    /**
     * Used by {@link DelimitedStringWriter}. This key sets custom serializers for
     * certain object properties.
     * 
     * <p>By default, a delimited string serializer calls object's {$code toString()} method
     * to convert an object to a string, which may not be sufficient in all cases.  For such
     * cases, use custom value serializers.
     */
    public final static Key<Map<String, Function<Object, String>>> valueSerializers = 
            new Key<>(() -> Collections.emptyMap());
    
    /**
     * Used by {@link DelimitedStringIterator}. This key indicates the use of
     * setters versus fields when parsing a delimited string into an object.
     * 
     * @see cl.serializers.delimited.DelimitedStringParser#useSetters
     */
    public final static Key<Boolean> useSetters = DelimitedStringParser.useSetters;
    
    /**
     * Used by {@link DelimitedStringSerializer}. This key indicates the use of
     * getters versus fields when serializing an object
     * 
     * @see cl.serializers.delimited.DelimitedStringSerializer#useGetters
     */
    public final static Key<Boolean> useGetters = DelimitedStringSerializer.useGetters;
    
    /**
     * Used by {@link DelimitedStringSerializer}. Whenever it is set to TRUE, only
     * properties defined in the 'index to property map' will be serialized. Otherwise
     * all properties of the objects will be serialized. That includes also properties
     * which are set to null, in which case the serializer will produce empty strings.
     * 
     * @see cl.serializers.delimited.DelimitedStringSerializer#exactProperties
     */
    public final static Key<Boolean> exactProperties = DelimitedStringSerializer.exactProperties;
    
    /**
     * Used by {@link DelimitedStringWriter}. If during delimited file creation a header
     * is not given, the writer will generate a header from object properties, if
     * this key is set to TRUE (which is default).
     * The strategy of generating column names is defined by {@code propertyToColumn}
     * configuration key.
     * 
     * @see SerializerConfiguration#propertyToColumn
     */
    public final static Key<Boolean> generateHeaderIfAbsent = new Key<>(() -> Boolean.TRUE);
    
    /**
     * Used by {@Link DelimitedStringWriter}. It defines a function which is used
     * to generate the file header if no explicit header is given. This configuration
     * key is used only when {@code generateHeaderIfAbsent} key is set to TRUE.
     * 
     * @see SerializerConfiguration#generateHeaderIfAbsent
     */
    public final static Key<Function<String, String>> propertyToColumn =
            new Key<>(() -> s -> Strings.camelToSpaced(s));
    
    /**
     * Used by {@link DelimitedStringIterator}. This key supplies a callback
     * which is executed whenever setting an object property (by setting a field
     * or using a setter) results in exception.
     * 
     * @see cl.serializers.delimited.DelimitedStringParser#onPropertySetError
     */
    public static Key<Consumer<PropertySetException>> onPropertySetError = 
            DelimitedStringParser.onPropertySetError;

    /**
     * Used by {@link DelimitedStringSerializer}. The joiner will concatenate
     * array of strings produced by it.
     * 
     * @see cl.serializers.delimited.DelimitedStringSerializer
     */
    public final static Key<DelimitedStringJoiner> delimitedStringJoiner = 
            new Key<>(() -> DelimitedStringJoiner.csv());
    
    /**
     * Return default configuration settings for java serializers. For java serializers, the
     * configuration is empty.
     */
    public static Configurable<?> javaSerializerDefaultConfiguration() {
        return configurationWith();
    }

    /**
     * Return default configuration settings for JSON serializers.
     * <p>JSON serializers make use of the following configuration settings:
     * <ul>
     *   <li>{@link SerializerConfiguration#skipEmptyLines}</li>
     *   <li>{@link SerializerConfiguration#charset}</li>
     *   <li>{@link SerializerConfiguration#jsonMapper}</li>
     * </ul>
     */
    public static Configurable<?> jsonSerializerDefaultConfiguration() {
        return configurationWith(skipEmptyLines, charset, jsonMapper);
    }
    
    /**
     * Return default configuration settings for string serializers.
     * <p>String serializers make use of the following configuration settings:
     * <ul>
     *   <li>{@link SerializerConfiguration#skipEmptyLines}</li>
     *   <li>{@link SerializerConfiguration#charset}</li>
     *   <li>{@link SerializerConfiguration#numHeaderLines}</li>
     *   <li>{@link SerializerConfiguration#headerLines}</li>
     *   <li>{@link SerializerConfiguration#onHeader}</li>
     * </ul>
     */
    public static Configurable<?> stringSerializerDefaultConfiguration() {
        return configurationWith(skipEmptyLines, charset, numHeaderLines, headerLines, onHeader);
    }
    
    /**
     * Return default configuration settings for delimited serializers.
     * <p>The following configuration settings are available:
     * <ul>
     *   <li>{@link SerializerConfiguration#skipEmptyLines}</li>
     *   <li>{@link SerializerConfiguration#charset}</li>
     *   <li>{@link SerializerConfiguration#numHeaderLines}</li>
     *   <li>{@link SerializerConfiguration#headerLines}</li>
     *   <li>{@link SerializerConfiguration#onHeader}</li>
     *   <li>{@link SerializerConfiguration#generateHeaderIfAbsent}</li>
     *   <li>{@link SerializerConfiguration#delimitedStringSplitter}</li>
     *   <li>{@link SerializerConfiguration#delimitedStringJoiner}</li>
     *   <li>{@link SerializerConfiguration#valueParsers}</li>
     *   <li>{@link SerializerConfiguration#valueSerializers}</li>
     *   <li>{@link SerializerConfiguration#columnToProperty}</li>
     *   <li>{@link SerializerConfiguration#columnIndexToProperty}</li>
     *   <li>{@link SerializerConfiguration#propertyToColumn}</li>
     *   <li>{@link SerializerConfiguration#useGetters}</li>
     *   <li>{@link SerializerConfiguration#useSetters}</li>
     *   <li>{@link SerializerConfiguration#exactProperties}</li>
     *   <li>{@link SerializerConfiguration#onPropertySetError}</li>
     * </ul>
     */
    public static Configurable<?> delimitedSerializerDefaultConfiguration() {
        return configurationWith(
                skipEmptyLines, charset,
                new Key<>(() -> 1),
                headerLines, onHeader, generateHeaderIfAbsent, 
                delimitedStringSplitter, delimitedStringJoiner,
                valueParsers, valueSerializers,
                columnToProperty, columnIndexToProperty, propertyToColumn,
                useGetters, useSetters, exactProperties, onPropertySetError);
    }

}
