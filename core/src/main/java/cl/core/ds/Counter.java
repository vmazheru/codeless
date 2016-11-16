package cl.core.ds;

/**
 * A mutable thread-unsafe counter, which can be used in places where mutating a primitive integer
 * is not an option, for example in lambda expressions.
 */
public final class Counter {

    private int i;
    private final int initValue;
    
    /**
     * Create a counter which starts with zero.
     */
    public Counter() {
        this(0);
    }
    
    /**
     * Create a counter which starts with some initial value.
     */
    public Counter(int initValue) {
        this.initValue = initValue;
        i = initValue;
    }

    /**
     * Add some value to the counter.
     */
    public void add(int increment) {
        i += increment;
    }

    /**
     * Increment the counter.
     */
    public void increment() {
        i++;
    }
    
    /**
     * Set the counter to its initial value.
     */
    public void reset() {
        i = initValue;
    }
    
    /**
     * Get counter value.
     */
    public int get() {
        return i;
    }
    
    /**
     * Get the value and then increment.
     */
    public int getAndIncrement() {
        int n = i;
        i++;
        return n;
    }

    /**
     * Get the value and then reset.
     */
    public int getAndReset() {
        int n = i;
        reset();
        return n;
    }
    
    /**
     * Increment and then get the value.
     */
    public int incrementAndGet() {
        return ++i;
    }
    
    @Override
    public String toString() {
        return Integer.toString(i);
    }
}
