package cl.core.decorator.retry;

/**
 *  Retry policy establishes when a function needs to be re-executed when an exception happens.
 *  
 *   @see RetryDecorator
 *   @see RetryDecorators
 */
public interface RetryPolicy {
    
    /**
     * Return a number of milliseconds which code should be suspended for before the next retry
     * takes places.  Returning a zero forces a retry decorator to give up and re-throw the 
     * exception.
     */
    long nextRetryIn();
}
