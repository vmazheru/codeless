package cl.serializers.iterators;

import java.io.File;
import java.io.InputStream;

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
    
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass, boolean lockConfiguration) {
        DelimitedStringIterator<T> iter = new DelimitedStringIterator<>(file, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    public static <T> ObjectIterator<T> fromFile(File file, Class<T> klass) {
        return fromFile(file, klass, true);
    }
    
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass, boolean lockConfiguration) {
        DelimitedStringIterator<T> iter = new DelimitedStringIterator<>(inputStream, klass);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    public static <T> ObjectIterator<T> fromInputStream(InputStream inputStream, Class<T> klass) {
        return fromInputStream(inputStream, klass, true);
    }
    
    @Override
    public ObjectIterator<T> clone(File file) {
        return DelimitedStringIterator.<T>fromFile(file, klass, false).withConfigurationFrom(this).locked();
    }

    @Override
    public ObjectIterator<T> clone(InputStream inputStream) {
        return DelimitedStringIterator.<T>fromInputStream(inputStream, klass, false).withConfigurationFrom(this).locked();        
    }
    
    @Override
    protected T parseLine(String line) {
        return parser.parse(splitter.split(line));
    }

}
