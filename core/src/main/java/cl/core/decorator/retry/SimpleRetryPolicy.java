package cl.core.decorator.retry;

/**
 * The simplest possible implementation of {@code RetryPolicy}, where retries execute for specified
 * number of times with equal pauses between them.
 * 
 * An instance of this class maintains an internal retry counter and it hence the instance cannot be
 * reused for a different method execution. 
 */
public class SimpleRetryPolicy implements RetryPolicy {
    
    private final int numRetries;
    private final long sleepTime;
    private int counter;

    /**
     * Constructor.
     * @param numRetries How many times to retry?
     * @param sleepTime  For how long to sleep between retries?
     */
    public SimpleRetryPolicy(int numRetries, long sleepTime) {
        this.sleepTime = sleepTime;
        this.numRetries = numRetries;
    }
    
    @Override
    public long nextRetryIn() {
        return (counter++ >= numRetries) ? 0 : sleepTime;
    }
    
    public int getNumRetries() {
        return numRetries;
    }
    
    public long getSleepTime() {
        return sleepTime;
    }
    
}
