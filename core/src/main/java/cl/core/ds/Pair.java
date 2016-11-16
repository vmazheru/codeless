package cl.core.ds;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a tuple of two elements. The objects of this class are immutable.
 *
 * @param <T> type of the first element
 * @param <U> type of the second element
 */
public final class Pair<T,U> {
    
    private final T first;
    private final U second;
    
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Return the first element of the pair.
     */
    public T _1() {
        return first;
    }

    /**
     * Return the second element of the pair.
     */
    public U _2() {
        return second;
    }

    /**
     * Convert this pair to an array of {@code Object}s of size two.
     */
    public Object[] asArray() {
        return new Object[] {first, second};
    }
    
    /**
     * Convert a pair to a list, assuming that both elements are of the same type.
     */
    public static <T> List<T> asList(Pair<T,T> pair) {
        return Arrays.<T>asList(pair._1(), pair._2());
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[").append(Objects.toString(first)).append(",")
                .append(Objects.toString(second)).append("]").toString();
    }

}
