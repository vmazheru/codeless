package cl.core.ds;

/**
 * A simple counter as an object as opposite to a primitive integer.
 * Its primary use is incrementing a value from inside a lambda expression.
 * This class is not thread safe.
 */
public final class Counter {

    private int i;
    
    public Counter() {}
    
    public Counter(int initValue) {
        i = initValue;
    }
    
    public void add(int increment) {
        i += increment;
    }
    
    public void increment() {
        i++;
    }
    
    public void reset() {
        i = 0;
    }
    
    public int getValue() {
        return i;
    }
    
    public int getValueAndIncrement() {
        int n = i;
        i++;
        return n;
    }
    
    public int getValueAndReset() {
        int result = getValue();
        reset();
        return result;
    }
}
