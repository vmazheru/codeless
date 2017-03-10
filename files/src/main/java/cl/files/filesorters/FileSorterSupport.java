package cl.files.filesorters;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.IOException;
import java.util.Comparator;

import cl.core.configurable.ConfigurableObject;
import cl.files.serializers.Serializer;
import cl.files.serializers.iterators.ObjectIterator;
import cl.files.serializers.writers.ObjectWriter;

/**
 * Super class for {@link FileSorter} implementations, which contains some common logic.
 */
abstract class FileSorterSupport<T> extends ConfigurableObject<FileSorter<T>> implements FileSorter<T> {
    
    private final Serializer<T,T> serializer;
    
    protected FileSorterSupport(Serializer<T,T> serializer) {
        this.serializer = serializer;
    }
    
    /**
     * Make sure the object is locked, and then sort the file.
     */
    @Override
    public void sort() {
        requireLock();
        uncheck(() -> {
            try (ObjectIterator<T> iterator = serializer.getIterator();
                 ObjectWriter<T> writer = serializer.getWriter()) {
                sort(iterator, writer);
            }
        });
    }
    
    protected Serializer<T,T> getSerializer() {
        return serializer;
    }
    
    @SuppressWarnings("unchecked")
    protected Comparator<T> getComparator() {
        return (Comparator<T>)get(FileSorter.comparator);
    }    
    
    protected abstract void sort(ObjectIterator<T> iterator, ObjectWriter<T> writer) throws IOException;
    
}
