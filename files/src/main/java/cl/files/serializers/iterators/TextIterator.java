package cl.files.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cl.files.serializers.SerializerConfiguration;

/**
 * This class is a specialization of {@link IOBoundObjectIterator} which operates on text inputs
 * (files or input streams).
 * 
 * <p>The class makes use of the following configuration keys:
 * <ul>
 *   <li>
 *      {@link cl.files.serializers.SerializerConfiguration#skipEmptyLines} of boolean type 
 *      instructs the iterator to skip or not skip empty lines in the text input.
 *      The default value is {@code true}.
 *   </li>
 *   <li>
 *      {@link cl.files.serializers.SerializerConfiguration#charset} specifies the input character set. 
 *      The default value is UTF-8.
 *   </li>
 * </ul>
 * 
 * <p>Subclasses may override these configuration settings in their respective {@code build()} methods.
 */
abstract class TextIterator<T> extends IOBoundObjectIterator<T> {
    
    protected BufferedReader reader;
    protected boolean skipEmptyLines = true;

    /**
     * Initialize object iterator with file as input.
     */
    protected TextIterator(File file) {
        super(file);
    }

    /**
     * Initialize object iterator with input stream as input.
     */
    protected TextIterator(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Close the input stream.
     */
    @Override
    public void close() throws IOException {
        if (reader != null) reader.close();
    }
    
    /**
     * Implements the {@code IOBoundObjectIterator.init()} method in order to initialize a text reader
     * with specific character set given a low-level Java input stream.
     * 
     * <p>The buffered reader will be created with the character set specified as a configuration setting.
     */
    @Override
    protected void init(InputStream inputStream) {
        reader = uncheck(() -> new BufferedReader(
                new InputStreamReader(inputStream, get(SerializerConfiguration.charset))));
    }
    
    /**
     * Implements the {@code ObjectIterator.readNext()} method.
     * The method reads one line from the iterator's text input and then delegates to {@code parseLine()}
     * method, which converts this line into an actual object.
     */
    @Override
    protected final T readNext() throws IOException {
        String line = null;
        do {
            line = reader.readLine();
        } while (line != null && skipEmptyLines && (line.isEmpty() || line.trim().isEmpty()));
        return parseLine(line);
    }
    
    /**
     * Given a line of text, parse this line into an actual object of some specific type.
     */
    protected abstract T parseLine(String line);
}
