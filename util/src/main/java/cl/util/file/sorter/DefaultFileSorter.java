package cl.util.file.sorter;

import cl.serializers.Serializer;
import cl.serializers.iterators.ObjectIterator;
import cl.serializers.writers.ObjectWriter;

/**
 * The default implementation of the {@link FileSorter} interface.
 * <p>This class make use of {@code FileSorter.inMemorySizeThreshold} configuration value
 * in order to switch between in-memory sorting for smaller files and external merge sorting
 * for large files. 
 */
final class DefaultFileSorter<T> extends FileSorterSupport<T> {
    
    private final long inputSize;
    
    public DefaultFileSorter(Serializer<T,T> serializer, long inputSize) {
        super(serializer);
        this.inputSize = inputSize;
    }

    @Override
    public void sort() {
        requireLock();
        if (inputSize <= get(FileSorter.inMemorySizeThreshold)) {
            new InMemoryFileSorter<>(getSerializer()).withConfigurationFrom(this).locked().sort();
        } else {
            new ExternalMergeFileSorter<>(getSerializer()).withConfigurationFrom(this).locked().sort();
        }
    }

    @Override
    protected void sort(ObjectIterator<T> iterator, ObjectWriter<T> writer) {
        throw new UnsupportedOperationException("default file sorter does not sort by itself, it delegates");
    }
    
}
