package cl.serializers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;
import cl.json.JsonMapper;


/**
 * This class contains all configuration keys (and their default values), which can be used with serializers.
 */
public class SerializerConfiguration {

    /**
     * Instruct a text-based serializer to skip empty lines. The default value is {@code true}.
     */
    public final static Key<Boolean> skipEmptyLines = new Key<>(() -> true);
    
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
     * Return default configuration settings for java serializers. For java serializers, the
     * configuration is empty.
     */
    public static Configurable<?> javaSerializerDefaultConfiguration() {
        return Configurable.defaultConfiguration(Collections.emptyList());
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
        return Configurable.defaultConfiguration(Arrays.asList(skipEmptyLines, charset, jsonMapper));
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
        return Configurable.defaultConfiguration(Arrays.asList(skipEmptyLines, charset, numHeaderLines, headerLines, onHeader));
    }
    
}
