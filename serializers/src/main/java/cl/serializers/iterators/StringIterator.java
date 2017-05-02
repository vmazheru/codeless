package cl.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.io.InputStream;
import java.util.function.BiConsumer;

import cl.serializers.SerializerConfiguration;

/**
 * A specialized version of text iterator which operates on raw strings.
 * 
 * <p>The class makes use of the following configuration keys (besides those available in {@link TextIterator}):
 * <ul>
 *   <li>
 *      {@link cl.serializers.SerializerConfiguration#numHeaderLines} defines how many header lines exist in a file.
 *      For example, a CSV file can have one line on top, which contain column names.
 *   </li>
 *   <li>
 *      {@link cl.serializers.SerializerConfiguration#onHeader} of type {@code BiConsumer<Integer, String>}. 
 *      This consumer function will be called for each header line in order, and the parameters to it 
 *      are the line number (starting with zero), and the line itself.
 *   </li>
 * </ul>
 */
public class StringIterator extends TextIterator<String> {
    
    private StringIterator(File file) {
        super(file);
    }
    
    private StringIterator(InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Return a string iterator which will operate on a file.
     * 
     * @param file  input file
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static ObjectIterator<String> fromFile(File file, boolean lockConfiguration) {
        StringIterator iter = new StringIterator(file);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return a string iterator which will operate on a file. The iterator's configuration will be locked.
     */
    public static ObjectIterator<String> fromFile(File file) {
        return fromFile(file, true);
    }
    
    /**
     * Return a string iterator which will operate on an input stream.
     * 
     * @param inputStream input stream
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static ObjectIterator<String> fromInputStream(InputStream inputStream, boolean lockConfiguration) {
        StringIterator iter = new StringIterator(inputStream);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return a string iterator which will operate on an input stream. The iterator's configuration will be locked.
     */
    public static ObjectIterator<String> fromInputStream(InputStream inputStream) {
        return fromInputStream(inputStream, true);
    }

    /**
     * @see cl.serializers.iterators.ObjectIterator#clone(java.io.File)
     */
    @Override
    public ObjectIterator<String> clone(File file) {
        return StringIterator.fromFile(file, false).withConfigurationFrom(this).locked();
    }

    /**
     * @see cl.serializers.iterators.ObjectIterator#clone(java.io.InputStream)
     */
    @Override
    public ObjectIterator<String> clone(InputStream inputStream) {
        return StringIterator.fromInputStream(inputStream, false).withConfigurationFrom(this).locked();
    }
    
    @Override
    protected void build() {
        super.build();
        skipEmptyLines = get(SerializerConfiguration.skipEmptyLines);
        BiConsumer<Integer, String> onHeader = get(SerializerConfiguration.onHeader);
        for (int i = 0; i < get(SerializerConfiguration.numHeaderLines); i++) {
            onHeader.accept(i, uncheck(() -> reader.readLine()));
        }
    }
    
    @Override
    protected String parseLine(String line) {
        return line;
    }
    
}
