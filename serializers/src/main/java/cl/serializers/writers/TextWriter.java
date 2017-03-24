package cl.serializers.writers;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import cl.serializers.SerializerConfiguration;

/**
 * This class is a specialization of {@link IOBoundObjectWriter} which operates on text outputs
 * (files or output streams).
 * 
 * <p>The class makes use of the following configuration keys:
 * <ul>
 *   <li>{@link cl.serializers.SerializerConfiguration#charset} specifies the output character set. The default value is UTF-8.</li>
 * </ul>
 * 
 * <p>Subclasses may override these configuration settings in their respective {@code build()} methods.
 */
abstract class TextWriter<T> extends IOBoundObjectWriter<T> {
    
    protected PrintWriter writer;
    
    /**
     * Create a writer which writes to a file.
     */
    protected TextWriter(File file) {
        super(file);
    }

    /**
     * Create a writer which writes to an output stream.
     */
    protected TextWriter(OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Close print writer
     */
    @Override
    public void close() throws IOException {
        if (writer != null) writer.close();
    }

    /**
     * Write an object to the destination. The object will be converted to a {@code String} by
     * calling a subclass's concrete implementation of {@code TextWriter.toString()} method.
     */
    @Override
    public void write(T t) {
        writer.println(toString(t));
    }
    
    /**
     * Implements the {@code IOBoundObjectWriter.init()} method in order to wrap a given output stream into a {@code PrintWriter}
     * object.
     * 
     * <p>The print writer will be initialized with the character set defined as a configuration setting.
     */
    @Override
    protected void init(OutputStream outputStream) {
        writer = uncheck(() -> new PrintWriter(new OutputStreamWriter(outputStream, get(SerializerConfiguration.charset))));
    }    
    
    /**
     * Subclasses must implement this method in order to be able to convert object to strings.
     */
    protected abstract String toString(T t);

}
