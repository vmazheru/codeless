package cl.serializers.writers;

import java.io.File;
import java.io.OutputStream;

import cl.serializers.SerializerConfiguration;
import cl.serializers.delimited.DelimitedStringJoiner;

public class DelimitedStringWriter extends TextWriter<String> {
    
    private DelimitedStringJoiner delimitedStringJoiner;
    
    private DelimitedStringWriter(File file) {
        super(file);
    }
    
    private DelimitedStringWriter(OutputStream outputStream) {
        super(outputStream);
    }
    
    public static ObjectWriter<String> toFile(File file, boolean lockConfiguration) {
        DelimitedStringWriter w = new DelimitedStringWriter(file);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    public static ObjectWriter<String> toFile(File file) {
        return toFile(file, true);
    }
    
    public static ObjectWriter<String> toOutputStream(OutputStream outputStream, boolean lockConfiguration) {
        DelimitedStringWriter w = new DelimitedStringWriter(outputStream);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    public static ObjectWriter<String> toOutputStream(OutputStream outputStream) {
        return toOutputStream(outputStream, true);
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.File)
     */
    @Override
    public ObjectWriter<String> clone(File file) {
        return DelimitedStringWriter.toFile(file, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.OutputStream)
     */
    @Override
    public ObjectWriter<String> clone(OutputStream outputStream) {
        return StringWriter.toOutputStream(outputStream, false).withConfigurationFrom(this).locked();
    }

    @Override
    protected void build() {
        super.build();
        
        delimitedStringJoiner = get(SerializerConfiguration.delimitedStringJoiner);
        
        for (String header : get(SerializerConfiguration.headerLines)) {
            writer.println(header);
        }
    }
    
    @Override
    protected String toString(String t) {
        // TODO Auto-generated method stub
        return null;
    }

}
