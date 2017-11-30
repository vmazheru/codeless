package cl.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Simple set operations
 */
public final class Sets {
    
    private Sets() {}

    /**
     * Return a union of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @param setFactory  Set constructor
     * @return            the union of two collections
     */
    public static <T> Set<T> union(Collection<T> a, Collection<T> b, Supplier<Set<T>> setFactory) {
        Set<T> result = createSet(a, setFactory);
        result.addAll(b);
        return result;
    }
    
    /**
     * Return a union of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @return            the union of two collections
     */
    public static <T> Set<T> union(Collection<T> a, Collection<T> b) {
        return union(a, b, defaultSetFactory());
    }
    
    /**
     * Return an intersection of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @param setFactory  Set constructor
     * @return            the intersection of two collections
     */
    public static <T> Set<T> intersection(Collection<T> a, Collection<T> b, Supplier<Set<T>> setFactory) {
        Set<T> result = createSet(a, setFactory);
        result.retainAll(b);
        return result;
    }
    
    /**
     * Return an intersection of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @return            the intersection of two collections
     */
    public static <T> Set<T> intersection(Collection<T> a, Collection<T> b) {
        return intersection(a,  b, defaultSetFactory());
    }

    /**
     * Return a difference of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @param setFactory  Set constructor
     * @return            the difference of two collections
     */
    public static <T> Set<T> difference(Collection<T> a, Collection<T> b, Supplier<Set<T>> setFactory) {
        Set<T> result = createSet(a, setFactory);
        result.removeAll(b);
        return result;
    }
    
    /**
     * Return a difference of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @return            the difference of two collections
     */
    public static <T> Set<T> difference(Collection<T> a, Collection<T> b) {
        return difference(a, b, defaultSetFactory());
    }

    /**
     * Return a complement of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @param setFactory  Set constructor
     * @return            the complement of two collections
     */
    public static <T> Set<T> complement(Collection<T> a, Collection<T> b, Supplier<Set<T>> setFactory) {
        return difference(union(a, b, setFactory), intersection(a, b, setFactory), setFactory);
    }
    
    /**
     * Return a complement of two collections as a set. The original collections will
     * not be modified.
     * 
     * @param a           Collection a
     * @param b           Collection b
     * @return            the complement of two collections
     */
    public static <T> Set<T> complement(Collection<T> a, Collection<T> b) {
        return complement(a, b, defaultSetFactory());
    }
    
    private static <T> Set<T> createSet(Collection<T> col, Supplier<Set<T>> setFactory) {
        Set<T> result = setFactory.get();
        result.addAll(col);
        return result;
    }
    
    private static <T> Supplier<Set<T>> defaultSetFactory() { 
        return HashSet::new;
    }

}
