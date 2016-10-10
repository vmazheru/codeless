package cl.files.serializers.writers;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Extends {@code ObjectWriter} with addition of either file or output stream handler.  Most of
 * implemented writers operate on file or input stream, so they should extend this class. 
 */
abstract class IOBoundObjectWriter<T> extends ObjectWriter<T> {

    private final File file;
    private final OutputStream outputStream;
    
    private IOBoundObjectWriter(File file, OutputStream outputStream) {
        this.file = file;
        this.outputStream = outputStream;
    }
    
    /**
     * Create a new object writer which writes to a file.
     */
    protected IOBoundObjectWriter(File file) {
        this(file, null);
    }

    /**
     * Create a new object writer which writes to an output stream.
     */
    protected IOBoundObjectWriter(OutputStream outputStream) {
        this(null, outputStream);
    }
    
    /**
     * Overrides {@code ConfigurableObject.build()} in order to initialize the writers's
     * destination.
     * 
     * @throws IllegalStateException if the destination (file or output stream) is null
     */
    @Override
    protected void build() {
        OutputStream out = (file != null) ? uncheck(() -> new FileOutputStream(file)) : outputStream; 
        if (out == null) {
            throw new IllegalStateException("either file or output stream must be set in the writer");
        }
        init(out);
    }
    
    /**
     * Subclasses must override this method in order to initialize a more specific version of the given output stream.
     * For example, Java object writer may require {@code ObjectOutputStream}, while text-processing 
     * object writers require {@code PrintWriter}.
     */
    protected abstract void init(OutputStream out);
}
