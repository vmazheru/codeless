package cl.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.io.File;
import java.io.InputStream;
import java.util.function.BiConsumer;

import cl.serializers.SerializerConfiguration;
import cl.serializers.delimited.DelimitedStringParser;
import cl.serializers.delimited.DelimitedStringSplitter;

public class DelimitedStringIterator<T> extends TextIterator<T> {
    
    private final Class<T> klass;
    private final DelimitedStringSplitter splitter = null;
    private final DelimitedStringParser<T> parser = null;
    
    private DelimitedStringIterator(File file, Class<T> klass) {
        super(file);
        this.klass = klass;
        
        
        
    }
    
    private DelimitedStringIterator(InputStream inputStream, Class<T> klass) {
        super(inputStream);
        this.klass = klass;
    }
    
    /**
     * Get an iterator which reads from a file.
     * 
     * @param file               file to read data from
     * @param klass              target object class
     * @param lockConfiguration  lock or not to lock the configuration
     */
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass, boolean lockConfiguration) {
        DelimitedStringIterator<T> iter = new DelimitedStringIterator<>(file, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Get an iterator which reads from a file.  The iterator's configuration will be locked.
     * 
     * @param file               file to read data from
     * @param klass              target object class
     */
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass) {
        return fromFile(file, klass, true);
    }
    
    /**
     * Get an iterator which reads from a stream.
     * 
     * @param inputStream        the stream to read data from
     * @param klass              target object class
     * @param lockConfiguration  lock or not to lock the configuration
     */
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass, boolean lockConfiguration) {
        DelimitedStringIterator<T> iter = new DelimitedStringIterator<>(inputStream, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Get an iterator which reads from a stream.  The iterator's configuration will be locked.
     * 
     * @param inputStream        the stream to read data from
     * @param klass              target object class
     */
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass) {
        return fromInputStream(inputStream, klass, true);
    }
    
    /**
     * @see cl.serializers.iterators.ObjectIterator#clone(java.io.File)
     */
    @Override
    public ObjectIterator<T> clone(File file) {
        return DelimitedStringIterator.<T>fromFile(file, klass, false).withConfigurationFrom(this).locked();
    }

    /**
     * @see cl.serializers.iterators.ObjectIterator#clone(java.io.InputStream)
     */
    @Override
    public ObjectIterator<T> clone(InputStream inputStream) {
        return DelimitedStringIterator.<T>fromInputStream(inputStream, klass, false).withConfigurationFrom(this).locked();        
    }
    
    @Override
    protected T parseLine(String line) {
        return parser.parse(splitter.split(line));
    }
    
    @Override
    protected void build() {
        super.build();
        skipEmptyLines = get(SerializerConfiguration.skipEmptyLines);
        BiConsumer<Integer, String> onHeader = get(SerializerConfiguration.onHeader);
        for (int i = 0; i < get(SerializerConfiguration.numHeaderLines); i++) {
            String line = uncheck(() -> reader.readLine());
            onHeader.accept(i, line);
            
            if (i == 0) {
                // TODO: parse CSV header 
            }
        }
    }

}
