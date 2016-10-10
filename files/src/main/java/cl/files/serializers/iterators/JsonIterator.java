package cl.files.serializers.iterators;

import java.io.File;
import java.io.InputStream;

import cl.files.serializers.SerializerConfiguration;
import cl.json.JsonMapper;

/**
 * Instances of this class know how to iterate over objects stored in JSON format.
 * 
 * <p>The class makes use of the following configuration keys (besides those available for its superclass {@code TextIterator}):
 * <ul>
 *   <li>{@code SerializerConfiguration.jsonMapper} sets a custom JSON mapper object. The default instance of JSON mapper is the
 *   instance with all JSON mapper default configuration settings.</li>
 * </ul>
 * 
 * <p>Note, that reading objects from a JSON file requires run-time knowledge of the target object type, hence
 * passing an object class to an iterator is necessary.
 */
public class JsonIterator<T> extends TextIterator<T> {
    
    private final Class<T> klass;
    private JsonMapper jsonMapper;
    
    private JsonIterator(File file, Class<T> klass) {
        super(file);
        this.klass = klass;
    }
    
    private JsonIterator(InputStream inputStream, Class<T> klass) {
        super(inputStream);
        this.klass = klass;
    }
    
    /**
     * Return an object iterator which will operate on a file.
     * 
     * @param file  input file
     * @param klass target objects' class
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass, boolean lockConfiguration) {
        JsonIterator<T> iter = new JsonIterator<>(file, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return an object iterator which will operate on a file. The iterator's configuration will be locked.
     */
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass) {
        return fromFile(file, klass, true);
    }
    
    /**
     * Return an object iterator which will operate on an input stream.
     * 
     * @param file  input file
     * @param klass target objects' class
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass, boolean lockConfiguration) {
        JsonIterator<T> iter = new JsonIterator<>(inputStream, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return an object iterator which will operate on an input stream. The iterator's configuration will be locked.
     */
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass) {
        return fromInputStream(inputStream, klass, true);
    }

    /**
     * @see cl.files.serializers.iterators.ObjectIterator#clone(java.io.File)
     */
    @Override
    public ObjectIterator<T> clone(File file) {
        return JsonIterator.<T>fromFile(file, klass, false).withConfigurationFrom(this).locked();
    }

    /**
     * @see cl.files.serializers.iterators.ObjectIterator#clone(java.io.InputStream)
     */
    @Override
    public ObjectIterator<T> clone(InputStream inputStream) {
        return JsonIterator.<T>fromInputStream(inputStream, klass, false).withConfigurationFrom(this).locked();
    }

    @Override
    protected void build() {
        super.build();
        jsonMapper = get(SerializerConfiguration.jsonMapper);
    }
    
    @Override
    protected T parseLine(String line) {
        return line != null ? jsonMapper.fromJson(line, klass) : null;
    }
    
}
