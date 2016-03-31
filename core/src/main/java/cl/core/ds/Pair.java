package cl.core.ds;

import java.util.Objects;

/**
 * Represents a tuple, since Java doesn't have those.
 * It's primary use is when methods need to return two values.
 */
public final class Pair<T,U> {
    
    private final T first;
    private final U second;
    
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
    
    public T _1() {
        return first;
    }
    
    public U _2() {
        return second;
    }
    
    @Override
    public String toString() {
        Objects.toString(first);
        return new StringBuilder("[").append(Objects.toString(first)).append(",")
                .append(Objects.toString(second)).append("]").toString();
    }

}
