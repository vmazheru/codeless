package cl.files.serializers.iterators;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Extends {@code ObjectIterator} with addition of either file or input stream handler.  Most of
 * implemented iterators operate on file or input stream, so they should extend this class. 
 */
abstract class IOBoundObjectIterator<T> extends ObjectIterator<T> {

    private final File file;
    private final InputStream inputStream;
    
    private IOBoundObjectIterator(File file, InputStream inputStream) {
        this.file = file;
        this.inputStream = inputStream;
    }
    
    /**
     * Initialize object iterator with a file as input.
     */
    protected IOBoundObjectIterator(File file) {
        this(file, null);
    }

    /**
     * Initialize object iterator with the input stream as input.
     */
    protected IOBoundObjectIterator(InputStream inputStream) {
        this(null, inputStream);
    }

    /**
     * Overrides {@code ConfigurableObject.build()} in order to initialize the iterator's
     * input.
     * 
     * @throws IllegalStateException if the input is null
     */
    @Override
    protected void build() {
        InputStream in = (file != null) ? uncheck(() -> new FileInputStream(file)) : inputStream;
        if (in == null) {
            throw new IllegalStateException("either file or input stream must be set in the iterator");
        }
        init(in);
    }
    
    /**
     * Subclasses must override this method in order to initialize a more specific version of the given input stream.
     * For example, Java object iterator may require {@code ObjectInputStream}, while text-processing 
     * object iterators require {@code BufferedReader}.
     */
    protected abstract void init(InputStream inputStream);
}
