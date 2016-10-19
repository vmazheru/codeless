package cl.core.util;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Miscellaneous utilities related to files and file system.  
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * Move a file. If the destination exists, it will be overwritten.
     */
    public static void moveFile(Path source, Path target) {
        uncheck(() -> {
            Files.move(source, target, REPLACE_EXISTING, ATOMIC_MOVE);    
        });
    }
    
    /**
     * Move a file. If the destination exists, it will be overwritten.
     */
    public static void moveFile(File source, File target) {
        moveFile(source.toPath(), target.toPath());
    }
    
    /**
     * Close an array of {@code Closeable} objects.  Each object is closed in its own try-catch block, so
     * if there is a problem closing one object, the others should still be closed.  If {@code IOException}
     * is thrown while closing any object, the method will throw an {@code UncheckedIOException} (after
     * it tries to close the other objects). This exception object may contain other exception in it
     * available via {@code getSuppressed()} method.
     * 
     * @param closeables             array of objects to close
     * @throws UncheckedIOException  whenever one or more objects fail to close. The exception object
     *                               may contain other exceptions in it available via {@code getSuppressed()} method
     */
    public static void close(Closeable ... closeables) throws UncheckedIOException {
        UncheckedIOException e = null;
        for (Closeable c : closeables) {
            IOException ex = close(c);
            if (ex != null) {
                if (e == null) {
                    e = new UncheckedIOException(ex);
                } else {
                    e.addSuppressed(ex);
                }
            }
        }
        if (e != null) {
            throw e;
        }
    }
    
    /*
     * Close an object in its own try-catch block.
     */
    private static IOException close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                return e;
            }
        }
        return null;
    }
}
