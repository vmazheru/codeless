package cl.serializers.writers;

import java.io.File;
import java.io.OutputStream;

import cl.serializers.SerializerConfiguration;

/**
 * A specialization of {@link ObjectWriter} which operate on {@code String} objects.
 * 
 * <p>The class makes use of the following configuration keys (besides those available in {@link TextWriter}):
 * <ul>
 *   <li>{@link cl.serializers.SerializerConfiguration#headerLines} defines a list of header lines (zero or more).  
 *   These lines will be written in the output before any data lines.  The default value is empty list.</li>
 * </ul>
 */
public class StringWriter extends TextWriter<String> {
    
    private StringWriter(File file) {
        super(file);
    }
    
    private StringWriter(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Create a string writer which will write to a file.
     * 
     * @param file output file
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static ObjectWriter<String> toFile(File file, boolean lockConfiguration) {
        StringWriter w = new StringWriter(file);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    /**
     * Create a string writer which will write to a file.  The configuration of this string writer will be locked.
     */
    public static ObjectWriter<String> toFile(File file) {
        return toFile(file, true);
    }
    
    /**
     * Create a string writer which will write to an output stream.
     * 
     * @param outputStream output stream
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static ObjectWriter<String> toOutputStream(OutputStream outputStream, boolean lockConfiguration) {
        StringWriter w = new StringWriter(outputStream);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    /**
     * Create a string writer which will write to an output stream.  The configuration of this string writer will be locked.
     */
    public static ObjectWriter<String> toOutputStream(OutputStream outputStream) {
        return toOutputStream(outputStream, true);
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.File)
     */
    @Override
    public ObjectWriter<String> clone(File file) {
        return StringWriter.toFile(file, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.OutputStream)
     */
    @Override
    public ObjectWriter<String> clone(OutputStream outputStream) {
        return StringWriter.toOutputStream(outputStream, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * Initialize this string writer. This method will write header lines to the writer's output if
     * the header lines are set in the configuration.
     */
    @Override
    protected void build() {
        super.build();
        for (String header : get(SerializerConfiguration.headerLines)) {
            writer.println(header);
        }
    }    

    /**
     * Return the same string as the input string.
     */
    @Override
    protected String toString(String t) {
        return t;
    }

}
