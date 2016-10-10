package cl.files.serializers.iterators;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Instances of this class know how to iterate over objects stored with Java serialization mechanism. 
 */
public class JavaIterator<T extends Serializable> extends IOBoundObjectIterator<T> {
    
    private ObjectInputStream in;
    
    private JavaIterator(File file) {
        super(file);
    }
    
    private JavaIterator(InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Return an object iterator which will operate on a file.
     * 
     * @param file  input file
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static <T extends Serializable> ObjectIterator<T> fromFile(File file, boolean lockConfiguration) {
        JavaIterator<T> iter = new JavaIterator<>(file);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return an object iterator which will operate on a file. The iterator's configuration will be locked.
     */
    public static <T extends Serializable> ObjectIterator<T> fromFile(File file) {
        return fromFile(file, true);
    }
    
    /**
     * Return an object iterator which will operate on an input stream.
     * 
     * @param inputStream  input stream
     * @param lockConfiguration if true, the returned iterator configuration will be locked.
     */
    public static <T extends Serializable> ObjectIterator<T> fromInputStream(InputStream inputStream, boolean lockConfiguration) {
        JavaIterator<T> iter = new JavaIterator<>(inputStream);
        if (lockConfiguration) iter.locked();
        return iter;
    }
    
    /**
     * Return an object iterator which will operate on an input stream. The iterator's configuration will be locked.
     */
    public static <T extends Serializable> ObjectIterator<T> fromInputStream(InputStream inputStream) {
        return fromInputStream(inputStream, true);
    }

    /**
     * Close the input stream.
     */
    @Override
    public void close() throws IOException {
        if (in != null) in.close();
    }

    /**
     * @see cl.files.serializers.iterators.ObjectIterator#clone(java.io.File)
     */
    @Override
    public ObjectIterator<T> clone(File file) {
        return JavaIterator.<T>fromFile(file, false).withConfigurationFrom(this).locked();
    }

    /**
     * @see cl.files.serializers.iterators.ObjectIterator#clone(java.io.InputStream)
     */
    @Override
    public ObjectIterator<T> clone(InputStream inputStream) {
        return JavaIterator.<T>fromInputStream(inputStream, false).withConfigurationFrom(this).locked();
    }

    /**
     * Deserialize the next object.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T readNext() throws ClassNotFoundException, IOException {
        try {
            return (T)in.readObject();
        } catch (EOFException e) {
            return null;
        } catch (NullPointerException e) {
            // input stream is null because the file is empty
            return null;
        }
    }
    
    /**
     * Initialize the given input stream as Java object input stream.
     */
    @Override
    protected void init(InputStream inputStream) {
        try {
            in = new ObjectInputStream(new BufferedInputStream(inputStream));   
        } catch (IOException e) {
            in = null;
        }
    }
    
}
