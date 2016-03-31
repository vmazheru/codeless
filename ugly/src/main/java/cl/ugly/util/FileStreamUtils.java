package cl.ugly.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FileStreamUtils {
    
    private FileStreamUtils(){}

    /**
     * Read a text file as a stream of lists of objects, where every object is a result of
     * parsing a line.
     * 
     * Note, that stream needs to be closed in order to dispose a file reader underneath.
     * 
     * @param path Text file path
     * @param size Size of the chunk (list)
     * @param parser Function which parses the row into some object of type R
     * @return Stream of lists of objects
     * @throws UncheckedIOException
     */
    public static <R> Stream<List<R>> sliceAndParse(Path path, int size, Function<String, R> parser) 
            throws UncheckedIOException {
        if(size <= 0) throw new IllegalArgumentException("size <= 0");
        try {
            BufferedReader in = Files.newBufferedReader(path);
            MultiLineIterator<R> iter = new MultiLineIterator<>(in, size, parser);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.ORDERED | Spliterator.NONNULL), false)
                    .onClose(uncheckedCloser(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    /**
     * Read a text file as a stream, every element of which is a list of rows
     * from that file of the specified size.
     * 
     * Note, that stream needs to be closed in order to dispose a file reader underneath.
     */
    public static Stream<List<String>> slice(Path path, int size) throws UncheckedIOException {
        return sliceAndParse(path, size, Function.identity());
    }

    /**
     * Function to close a Closeable object and throw unchecked exception in case when
     * close() throws IOException.
     */
    private static Runnable uncheckedCloser(Closeable closeable) {
        return () -> {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}

/**
 * Package access iterator which iterate a text file in line batches, and converts every line
 * to some object by calling a supplied parser function.
 */
class MultiLineIterator<T> implements Iterator<List<T>> {
    final private int size;
    final private BufferedReader in;
    final Function<String, T> parser;
    private List<T> list;
    
    MultiLineIterator(BufferedReader in, int size, Function<String, T> parser) {
        this.size = size;
        this.in = in;
        this.parser = parser;
    }

    @Override
    public boolean hasNext() {
        readNext();
        return !list.isEmpty();
    }

    @Override
    public List<T> next() {
        if (list != null && !list.isEmpty()) {
            List<T> tmp = list;
            list = null;
            return tmp;
        }
        throw new NoSuchElementException();
    }
    
    private void readNext() {
        list = new ArrayList<>(size);
        try {
            for(int i = 0 ; i < size; i++) {
                String s = in.readLine();
                if(s != null) list.add(parser.apply(s)); 
                else break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
