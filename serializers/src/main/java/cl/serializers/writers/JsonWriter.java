package cl.serializers.writers;

import java.io.File;
import java.io.OutputStream;

import cl.serializers.SerializerConfiguration;
import cl.json.JsonMapper;

/**
 * A specialization of {@link ObjectWriter} which stores objects in JSON format.
 * 
 * <p>The class makes use of the following configuration keys (besides those available in {@link TextWriter}):
 * <ul>
 *   <li>{@link cl.serializers.SerializerConfiguration#jsonMapper} sets a custom JSON mapper object. 
 *   The default instance of JSON mapper is the instance with all JSON mapper default configuration settings.
 *   </li>
 * </ul>
 */
public class JsonWriter<T> extends TextWriter<T> {

    private JsonMapper jsonMapper;
    
    private JsonWriter(File file) {
        super(file);
    }
    
    private JsonWriter(OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Create an object writer which will write to a file.
     * 
     * @param file output file
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static <T> ObjectWriter<T> toFile(File file, boolean lockConfiguration) {
        JsonWriter<T> w = new JsonWriter<>(file);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    /**
     * Create an object writer which will write to a file.  The configuration of this object writer will be locked.
     */
    public static <T> ObjectWriter<T> toFile(File file) {
        return toFile(file, true);
    }
    
    /**
     * Create an object writer which will write to an output stream.
     * 
     * @param outputStream output stream
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static <T> ObjectWriter<T> toOutputStream(OutputStream outputStream, boolean lockConfiguration) {
        JsonWriter<T> w = new JsonWriter<>(outputStream);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    /**
     * Create an object writer which will write to an output stream.  The configuration of this object writer will be locked.
     */
    public static <T> ObjectWriter<T> toOutputStream(OutputStream outputStream) {
        return toOutputStream(outputStream, true);
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.File)
     */
    @Override
    public ObjectWriter<T> clone(File file) {
        return JsonWriter.<T>toFile(file, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.OutputStream)
     */
    @Override
    public ObjectWriter<T> clone(OutputStream outputStream) {
        return JsonWriter.<T>toOutputStream(outputStream, false).withConfigurationFrom(this).locked();
    }
    
    @Override
    protected void build() {
        super.build();
        jsonMapper = get(SerializerConfiguration.jsonMapper);
    }
    
    /**
     * Convert an object to its JSON representation
     */
    @Override
    protected String toString(Object t) {
        return jsonMapper.toJson(t);
    }

}
