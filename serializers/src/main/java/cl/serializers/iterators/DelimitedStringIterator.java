package cl.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import cl.core.configurable.ConfigurableException;
import cl.core.util.Reflections;
import cl.serializers.SerializerConfiguration;
import cl.serializers.delimited.DelimitedStringParser;
import cl.serializers.delimited.DelimitedStringSplitter;

/**
 * This iterator can read and parse delimited lines.
 * 
 * <p>The iterator makes use of the following configuration settings:
 * <ul>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#delimitedStringSplitter} specifies
 *      the splitter object, that is, the object which splits a line into an array of
 *      values.  By default, the CSV splitter is used.
 *  </li>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#columnToProperty} specifies
 *      a strategy of converting column names into object property names. It will not 
 *      override mappings generated by {@link cl.serializers.SerializerConfiguration#columnIndexToProperty}.
 *      
 *      <p>A "spaced" string to camel case is the default strategy. This strategy
 *      will convert strings like "First name", "first name", "First Name" into
 *      a "firstName" property. This strategy is case-insensitive and it ignores
 *      redundant spaces.
 *  </li>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#columnIndexToProperty} specifies
 *      mapping between column indexes and object property names. An empty map is
 *      the default value.
 *      
 *      <p>This configuration setting is optional. For missing columns, 
 *      the iterator will try to figure out object properties by parsing 
 *      the header in the file (the first line).
 *  </li>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#valueParsers} specifies
 *      the mapping between property names and custom parsers. It is used
 *      to set object properties of custom types.
 *      
 *      <p>The default delimited string parser used by the iterator can figure
 *      out how to parse string values for most commonly used types by asking the
 *      global {@link cl.core.function.stringparser.StringParsers} object. Use this
 *      property to override the default behavior and/or to provide parsers
 *      for custom types.
 *  </li>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#numHeaderLines} defines 
 *      how many header lines exist in a file. For example a CSV file may have
 *      from zero to many header lines. 
 *      
 *      <p>The factory methods for this iterator will set this value to 1 by default.
 *  </li>
 *  <li>
 *      {@link cl.serializers.SerializerConfiguration#onHeader} of type 
 *      {@code BiConsumer<Integer, String>}. 
 *      This consumer function will be called for each header line, and the parameters to it 
 *      are the line number (starting with zero), and the line itself.
 *   </li>
 * </ul>
 *  
 * <p>
 * Other then the settings listed above, this iterator also
 * makes use of all configuration settings exposed by its super class 
 * {@link cl.serializers.iterators.TextIterator}.
 *  
 * @param <T> type of objects which this iterator generates
 */
public class DelimitedStringIterator<T> extends TextIterator<T> {
    
    private final Class<T> klass;
    private DelimitedStringSplitter splitter;
    private DelimitedStringParser<T> parser;
    
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
        @SuppressWarnings("resource")
        ObjectIterator<T> iter = new DelimitedStringIterator<>(file, klass)
                .with(SerializerConfiguration.numHeaderLines, 1);
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
        @SuppressWarnings("resource")
        ObjectIterator<T> iter = new DelimitedStringIterator<>(inputStream, klass)
                .with(SerializerConfiguration.numHeaderLines, 1);
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
    
    /**
     * Parse a line into an object.
     */
    @Override
    protected T parseLine(String line) {
        return parser.parse(splitter.split(line));
    }
    
    @Override
    protected void build() {
        super.build();
        skipEmptyLines = get(SerializerConfiguration.skipEmptyLines);
        
        Map<Integer, String> columnIndexToProperty = 
                new HashMap<>(get(SerializerConfiguration.columnIndexToProperty));
        
        boolean useSetters = get(SerializerConfiguration.useSetters);
        
        splitter = get(SerializerConfiguration.delimitedStringSplitter);
        
        Function<String, String> columnToProperty = get(SerializerConfiguration.columnToProperty);
        
        boolean emptyFile = false;
        
        BiConsumer<Integer, String> onHeader = get(SerializerConfiguration.onHeader);
        for (int i = 0; i < get(SerializerConfiguration.numHeaderLines); i++) {
            String line = uncheck(() -> reader.readLine());
            if (i == 0 && line == null) {
                emptyFile = true;
                break;
            };
            
            onHeader.accept(i, line);
            
            if (i == 0) {
                Predicate<String> propertyChecker = useSetters ?
                            p -> Reflections.setterExists(p, klass) :
                            p -> Reflections.fieldExists(p, klass);
                
                String[] columnNames = splitter.split(line);
                for (int j = 0; j < columnNames.length; j++) {
                    if (!columnIndexToProperty.containsKey(j)) {
                        String property = columnToProperty.apply(columnNames[j]);
                        if (propertyChecker.test(property)) {
                            columnIndexToProperty.put(j, property);
                        }
                    }
                }
            }
        }
        
        if (!emptyFile && columnIndexToProperty.isEmpty()) {
            throw new ConfigurableException("Cannot figure out column index to property mappings. "
                    + "Either a header should exist in the file or 'columnIndexToProperty' should be set.");
        }
        
        parser = DelimitedStringParser.get(
                klass, columnIndexToProperty, get(SerializerConfiguration.valueParsers), false)
                    .with(DelimitedStringParser.useSetters, useSetters)
                    .with(DelimitedStringParser.onPropertySetError, get(SerializerConfiguration.onPropertySetError))
                    .locked();
    }

}
