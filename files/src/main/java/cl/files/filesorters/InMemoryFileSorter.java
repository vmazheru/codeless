package cl.files.filesorters;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import cl.files.serializers.Serializer;
import cl.files.serializers.iterators.ObjectIterator;
import cl.files.serializers.writers.ObjectWriter;

/**
 * Implementation of the {@link FileSorter} interface which loads a file fully into the memory and sorts it in memory.
 */
public class InMemoryFileSorter<T> extends FileSorterSupport<T> {
    
    public InMemoryFileSorter(Serializer<T,T> serializer) {
        super(serializer);
    }
    
    /**
     * Sort data.
     */
    @Override
    protected void sort(ObjectIterator<T> iterator, ObjectWriter<T> writer) {
        Collection<T> sorted = get(FileSorter.removeDuplicates) ?
                sortWithoutDuplicates(iterator) :
                sortWithDuplicates(iterator);
        writer.write(sorted);
    }
    
    /*
     * Sorting with duplicate removal is done via loading data into a tree set. We're not concerned 
     * about sort stability, because duplicates get removed anyways.
     */
    private Collection<T> sortWithoutDuplicates(ObjectIterator<T> iterator) {
        return iterator.stream().collect(toCollection(() -> new TreeSet<>(getComparator())));
    }
    
    /*
     * Sorting with retaining duplicates is done via Collections.sort(), which uses stable sorting algorithm.
     */
    private Collection<T> sortWithDuplicates(ObjectIterator<T> iterator) {
        List<T> objects = iterator.stream().collect(toList());
        Collections.sort(objects, getComparator());
        return objects;
    }
    
}
