package cl.serializers;

import static cl.core.configurable.Configurable.configurationWith;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;
import cl.core.util.Strings;
import cl.json.JsonMapper;
import cl.serializers.delimited.DelimitedStringSplitter;
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
     * Used by {@link DelimitedStringIterator}. This key sets a strategy
     * for parsing file header in order to establish mappings between
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
     * Used by {@link DelimitedStringIterator}. This key sets a mapping
     * between columns and object properties. It does not have to cover all
     * columns in the delimited file. The missing mappings will be filled by
     * applying the 'columnToProperty' setting.
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
     * string parsers is able to parse strings, all primitive types, dates, 
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
    
}
