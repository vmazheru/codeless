package cl.files.serializers.writers;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * A specialization of {@code ObjectWriter} which stores objects by using Java serialization mechanism.
 *
 * @param <T> object type which must extend {@code java.io.Serializable} interface.
 */
public class JavaWriter<T extends Serializable> extends IOBoundObjectWriter<T> {
    
    /*
     * ObjectOutputStream.reset() method will be called after that many objects have been written
     * to prevent from OutOfMemeoryError to occur.
     */
    private static final int RESET_AFTER = 10_000;
    
    private ObjectOutputStream out;
    // how many objects have been written so far toward the reset limit.
    private int count;
    
    private JavaWriter(File file) {
        super(file);
    }
    
    private JavaWriter(OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Create an object writer which will write to a file.
     * 
     * @param file output file
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static <T extends Serializable> ObjectWriter<T> toFile(File file, boolean lockConfiguration) {
        JavaWriter<T> w = new JavaWriter<>(file);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    /**
     * Create an object writer which will write to a file.  The configuration of this object writer will be locked.
     */
    public static <T extends Serializable> ObjectWriter<T> toFile(File file) {
        return toFile(file, true);
    }
    
    /**
     * Create an object writer which will write to an output stream.
     * 
     * @param outputStream output stream
     * @param lockConfiguration if true, the created object writer's configuration will be locked.
     */
    public static <T extends Serializable> ObjectWriter<T> toOutputStream(OutputStream outputStream, boolean lockConfiguration) {
        JavaWriter<T> w = new JavaWriter<>(outputStream);
        if (lockConfiguration) w.locked();
        return w;
    }

    /**
     * Create an object writer which will write to an output stream.  The configuration of this object writer will be locked.
     */
    public static <T extends Serializable> ObjectWriter<T> toOutputStream(OutputStream outputStream) {
        return toOutputStream(outputStream, true);
    }
    
    /**
     * Close the output stream
     */
    @Override
    public void close() throws IOException {
        if (out != null) out.close();
    }

    /**
     * @see cl.files.serializers.writers.ObjectWriter#clone(java.io.File)
     */
    @Override
    public ObjectWriter<T> clone(File file) {
        return JavaWriter.<T>toFile(file, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * @see cl.files.serializers.writers.ObjectWriter#clone(java.io.OutputStream)
     */
    @Override
    public ObjectWriter<T> clone(OutputStream outputStream) {
        return JavaWriter.<T>toOutputStream(outputStream, false).withConfigurationFrom(this).locked();
    }

    /**
     * Serialize an object
     */
    @Override
    public void write(T t) {
        uncheck(() -> {
            out.writeObject(t);
            // when writing multiple objects to a single file using one ObjectOutputStream object,
            // OutOfMemeoryError may occur, since ObjectOutputStream holds references to all objects written
            // in memory till it is closed or reset() is called
            if(count++ > RESET_AFTER) {
                count = 0;
                out.reset();
            }
        });
    }

    /**
     * Wrap the given output stream in {@code ObjectOutputStream}
     */
    @Override
    protected void init(OutputStream outputStream) {
        out = uncheck(() -> new ObjectOutputStream(new BufferedOutputStream(outputStream)));
    }

}
