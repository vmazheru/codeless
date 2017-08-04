package cl.util.file.sorter;

import static cl.util.file.sorter.FileSorterUtils.withTempFile;
import static cl.serializers.Serializer.*;

import java.io.File;
import java.util.Comparator;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;
import cl.serializers.SerializationType;
import cl.serializers.Serializer;
import cl.serializers.SerializerConfiguration;

/**
 * A file sorter is an object which sorts files, from small to large possibly without loading them
 * fully into the memory.
 * 
 * <p>Implementations of {@code FileSorter} rely on {@code Collections.sort()} method; hence the sorting
 * algorithm is stable.
 * 
 * <p>Because file sorter relies on 'serializers' framework, it is able to sort objects in files of
 * different format (text files, JSON files, or Java-serialized objects), not just strings. That's why
 * the interface is made generic.
 * 
 * <p>File sorter is also a {@link Configurable} object, which accepts the following configuration keys:
 * 
 * <ul>
 *   <li>{@link FileSorter#comparator}. Defines a comparator which operates on the object of the same type as file sorter.
 *       The default value is {@code Comparator.naturalOrder()}.
 *   </li>
 *   <li>{@link FileSorter#inMemorySizeThreshold}. Specifies a value on which the default implementation will switch from
 *       in-memory sorting to 'external merge sort' algorithm, which could be useful when sorting very large files.
 *       The value is measured in bytes, and the default value is 100 * 1024 * 1024 bytes (100MB). This setting is very
 *       rough since it is being compared with the input file size, not some "actual" memory size taken by the all 
 *       objects in the file.
 *   </li>
 *   <li>{@link FileSorter#numObjectsPerFile}. This is another setting which helps to conserve memory.
 *       It is used by the 'external merge sort' algorithm to define the number of objects which go to a single chunk (temporary file).
 *       (External merge sort splits an input file into smaller temporary files, and sort each of them independently in memory.)
 *       The default value is 100,000. 
 *   </li>
 *   <li>{@link FileSorter#removeDuplicates}. Instruct file sorter to remove duplicates from the result.
 *       The default value is {@code false}, which retains duplicates. Since the sorting algorithm is stable,
 *       the duplicates will be put in the result in the same order as they are in the source file. 
 *   </li>
 * </ul>
 * 
 * <p>Note, that the underlying serializer's configuration keys cannot be passed via file sorter
 * configuration.  If the underlying serializers needs to accept come custom configuration, create
 * the serializer explicitly and passed to a factory method when creating file sorter.
 * 
 * <p>This interface defines a number of factory methods returning file sorter instance, as well as
 * number of static {@code sort()} methods which could be used to sort files more easily in typical
 * cases (for example, sorting a text file).
 * 
 * @param T type of objects which file sorter operates on
 */
public interface FileSorter<T> extends Configurable<FileSorter<T>> {
    
    /**
     * Comparator which will be used to compare objects. If not passed, objects must implement
     * {@code Comparable} interface.  The default value is {@code Comparator.naturalOrder()}
     */
    static Key<Comparator<?>> comparator = new Key<>(() -> Comparator.naturalOrder());
    
    /**
     * Value in bytes on which the implementation switches between in-memory sorting to external merge sorting
     * algorithms. The default value is 100 * 1024 * 1024 bytes (100MB). This value is compared with
     * the input file size.
     */
    static Key<Long> inMemorySizeThreshold = new Key<>(() -> 100 * 1024 * 1024L); //in bytes
    
    /**
     * Number of object to put in one temporary file, created by external merge file sorting algorithm.
     * The default value is 100,000.
     */
    static Key<Integer> numObjectsPerFile = new Key<>(() -> 100_000);
    
    /**
     * Instructs file sorter to remove or keep duplicates in the result. The default value is
     * false, which means 'keep duplicates'.
     */
    static Key<Boolean> removeDuplicates = new Key<>(() -> false);
    
    /**
     * Sort the input file.
     */
    void sort();
    
    /**
     * Create a file sorter object. This method takes a {@link Serializer} and passes it to the
     * default implementation class. This is the main factory method upon which the other factory 
     * methods depend.
     * 
     * @param serializer         serializer object
     * @param inputSize          input size, which should be the size of the input file
     *                           This value has to be passed separately, because
     *                           there is no an easy way to get the underlying file size from the given
     *                           serializer object.
     * @param lockConfiguration  specifies whether the configuration of the file sorter should be
     *                           locked  or not. Use {@code false} when you need to pass more configuration
     *                           settings to the file sorter after it has been created.
     * @return a file sorter instance
     */
    static <T> FileSorter<T> getFileSorter(Serializer<T,T> serializer, long inputSize, boolean lockConfiguration) {
        FileSorter<T> fs = new DefaultFileSorter<>(serializer, inputSize);
        if (lockConfiguration) {
            fs.locked();
        }
        return fs;
    }
    
    /**
     * Create a file sorter object with its configuration locked.
     * 
     * @param serializer serializer object
     * @param inputSize  input size, which should be the size of the input file
     * @return a file sorter instance
     */
    static <T> FileSorter<T> getFileSorter(Serializer<T,T> serializer, long inputSize) {
        return getFileSorter(serializer, inputSize, true);
    }    
    
    /**
     * Create a file sorter object.
     * 
     * @param original           original file
     * @param sorted             destination file (sorted)
     * @param serializationType  serialization type
     * @param klass              class of objects, on which file sorter operates
     * @param lockConfiguration  lock configuration or not
     * @return a file sorter instance
     */
    static <T> FileSorter<T> getFileSorter(
            File original,
            File sorted,
            SerializationType serializationType,
            Class<T> klass,
            boolean lockConfiguration) {
        Serializer<T,T> serializer = Serializer.serializer(original, sorted, serializationType, klass);
        return getFileSorter(serializer, original.length(), lockConfiguration);
    }
    
    /**
     * Create a file sorter object with locked configuration.
     * 
     * @param original           original file
     * @param sorted             destination file (sorted)
     * @param serializationType  serialization type
     * @param klass              class of objects, on which file sorter operates
     * @return a file sorter instance
     */
    static <T> FileSorter<T> getFileSorter(
            File original,
            File sorted,
            SerializationType serializationType,
            Class<T> klass) {
        return getFileSorter(original, sorted, serializationType, klass, true);
    }

    /**
     * Create a file sorter object with locked configuration.
     * 
     * @param original           original file
     * @param sorted             destination file (sorted)
     * @param serializationType  serialization type
     * @param klass              class of objects, on which file sorter operates
     * @param comparator         comparator
     * @param removeDuplicates   remove duplicates or not
     * @return a file sorter instance
     */
    static <T> FileSorter<T> getFileSorter(
            File original,
            File sorted,
            SerializationType serializationType,
            Class<T> klass,
            Comparator<T> comparator,
            boolean removeDuplicates) {
        return getFileSorter(original, sorted, serializationType, klass, false)
                .with(FileSorter.comparator, comparator)
                .with(FileSorter.removeDuplicates, removeDuplicates).locked();
    }

    /**
     * Convenient method to sort a text file.
     * 
     * @param original original file
     * @param sorted   destination file (sorted)
     */
    static void sort(File original, File sorted) {
        getFileSorter(stringSerializer(original, sorted), original.length()).sort();
    }    
    
    /**
     * Convenient method to sort a text file. The original file will be replaced with the sorted file.
     */
    static void sort(File file) {
        withTempFile(file, (orig, sorted) -> sort(file, sorted));
    }

    /**
     * Sort a text file.
     * 
     * @param original          original file
     * @param sorted            sorted file
     * @param removeDuplicates  remove duplicates or not
     * @param numHeaderLines    how many lines are in the header (the sorter will skip them and put them on top of the sorted file)
     * @param comparator        comparator
     */
    static void sort(
            File original,
            File sorted,
            boolean removeDuplicates,
            int numHeaderLines,
            Comparator<String> comparator) {
        Serializer<String, String> serializer = Serializer.<String, String>stringSerializer(
                original, sorted, Configurable.empty().with(SerializerConfiguration.numHeaderLines, numHeaderLines).locked());
        FileSorter<String> fs = getFileSorter(serializer, original.length(), false)
                .with(FileSorter.removeDuplicates, removeDuplicates)
                .with(FileSorter.comparator, comparator)
                .locked();
        fs.sort();
    }
    
    /**
     * Sort a text file.
     * 
     * @param original          original file
     * @param sorted            sorted file
     * @param removeDuplicates  remove duplicates or not
     * @param numHeaderLines    how many lines are in the header
     */
    static void sort(
            File original,
            File sorted,
            boolean removeDuplicates,
            int numHeaderLines) {
        sort(original, sorted, removeDuplicates, numHeaderLines, Comparator.naturalOrder());
    }
    
    /**
     * Sort a text file.
     * 
     * @param original          original file
     * @param sorted            sorted file
     * @param removeDuplicates  remove duplicates or not
     */
    static void sort(
            File original,
            File sorted,
            boolean removeDuplicates) {
        sort(original, sorted, removeDuplicates, 0, Comparator.naturalOrder());
    }
    
    /**
     * Sort a text file.
     * 
     * @param file              input file. It will be replaced by the sorted file.
     * @param removeDuplicates  remove duplicates or not
     * @param numHeaderLines    how many lines are in the header
     * @param comparator        comparator
     */
    static void sort(
            File file,
            boolean removeDuplicates,
            int numHeaderLines,
            Comparator<String> comparator) {
        withTempFile(file, (orig, sorted) -> sort(orig, sorted, removeDuplicates, numHeaderLines, comparator));
    }
    
    /**
     * Sort a text file.
     * 
     * @param file             input file. It will be replaced by the sorted file
     * @param removeDuplicates remove duplicates or not
     * @param numHeaderLines   how many lines are in the header
     */
    static void sort(
            File file,
            boolean removeDuplicates,
            int numHeaderLines) {
        withTempFile(file, (orig, sorted) -> sort(orig, sorted, removeDuplicates, numHeaderLines));
    }
    
    /**
     * Sort a text file.
     * 
     * @param file             input file. It will be replaced by the sorted file
     * @param removeDuplicates remove duplicates or not
     */
    static void sort(
            File file,
            boolean removeDuplicates) {
        withTempFile(file, (orig, sorted) -> sort(orig, sorted, removeDuplicates, 0));
    }
    
}
