package cl.core.decorator.retry;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cl.core.decorator.Decorator;
import cl.core.util.Threads;

/**
 * Different overloaded static methods which decorate a function call with retry logic. 
 * 
 * <p> The methods in this class could be divided into two groups.
 * 
 * <p> If a method name is "retried", it simply transforms a function into a similar
 * function with retry logic applied. These methods could be used whenever a result function needs
 * be further decorated with some other decorators.
 * 
 * <p> If a method name is "retry", it will execute the given function with retry logic applied.
 * 
 * @see RetryDecorator
 * @see RetryPolicy
 */
public interface RetryDecorators {
    
    ///////////////// decorators //////////////////////

    // -------------- Runnable ----------------- //
    static Runnable retried(int numRetries, long sleep, Runnable f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static Runnable retried(
            RetryPolicy p,
            Runnable f) {
        return retried(p, null, null, f, null);
    }

    static Runnable retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Runnable f) {
        return retried(p, exceptionClasses , null, f, null);
    }
    
    static Runnable retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Runnable f) {
        return retried(p, null, beforeSleep, f, null);
    }
    
    static Runnable retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Runnable f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }
    
    static Runnable retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Runnable f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static Runnable retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Runnable f,
            Runnable afterSleep) {
        return new RetryDecorator<>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    // -------------- Supplier ----------------- //
    static <R> Supplier<R> retried(int numRetries, long sleep, Supplier<R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static <R> Supplier<R> retried(
            RetryPolicy p,
            Supplier<R> f) {
        return retried(p, null, null, f, null);
    }

    static <R> Supplier<R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Supplier<R> f) {
        return retried(p, exceptionClasses , null, f, null);
    }

    static <R> Supplier<R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Supplier<R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    static <R> Supplier<R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Supplier<R> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    static <R> Supplier<R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Supplier<R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static <R> Supplier<R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Supplier<R> f,
            Runnable afterSleep) {
        return new RetryDecorator<Object, Object, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    
    // -------------- Consumer ----------------- //
    static <T> Consumer<T> retried(int numRetries, long sleep, Consumer<T> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static <T> Consumer<T> retried(
            RetryPolicy p,
            Consumer<T> f) {
        return retried(p, null, null, f, null);
    }

    static <T> Consumer<T> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<T> f) {
        return retried(p, exceptionClasses , null, f, null);
    }

    static <T> Consumer<T> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Consumer<T> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    static <T> Consumer<T> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Consumer<T> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    static <T> Consumer<T> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Consumer<T> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static <T> Consumer<T> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Consumer<T> f,
            Runnable afterSleep) {
        return new RetryDecorator<T, Object, Object>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    // -------------- BiConsumer ----------------- //
    static <T,U> BiConsumer<T,U> retried(int numRetries, long sleep, BiConsumer<T,U> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            BiConsumer<T,U> f) {
        return retried(p, null, null, f, null);
    }

    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            BiConsumer<T,U> f) {
        return retried(p, exceptionClasses , null, f, null);
    }

    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            BiConsumer<T,U> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            BiConsumer<T,U> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            BiConsumer<T,U> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static <T,U> BiConsumer<T,U> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            BiConsumer<T,U> f,
            Runnable afterSleep) {
        return new RetryDecorator<T, U, Object>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    
    // -------------- Function ----------------- //
    static <T,R> Function<T,R> retried(int numRetries, long sleep, Function<T,R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Function<T,R> f) {
        return retried(p, null, null, f, null);
    }

    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Function<T,R> f) {
        return retried(p, exceptionClasses , null, f, null);
    }

    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Function<T,R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Function<T,R> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Function<T,R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static <T,R> Function<T,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Function<T,R> f,
            Runnable afterSleep) {
        return new RetryDecorator<T, Object, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    
    // -------------- BiFunction ----------------- //
    static <T,U,R> BiFunction<T,U,R> retried(int numRetries, long sleep, BiFunction<T,U,R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            BiFunction<T,U,R> f) {
        return retried(p, null, null, f, null);
    }

    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            BiFunction<T,U,R> f) {
        return retried(p, exceptionClasses , null, f, null);
    }

    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            BiFunction<T,U,R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            BiFunction<T,U,R> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            BiFunction<T,U,R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    static <T,U,R> BiFunction<T,U,R> retried(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            BiFunction<T,U,R> f,
            Runnable afterSleep) {
        return new RetryDecorator<T, U, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    
    
    ///////////////// decorator applications //////////////////////
    
    // -------------- Runnable ----------------- //
    static void retry(int numRetries, long sleep, Runnable f) {
        retried(numRetries, sleep, f).run();
    }
    
    static void retry(
            RetryPolicy p,
            Runnable f) {
        retried(p, f).run();
    }    
    
    static void retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Runnable f) {
        retried(p, exceptionClasses, f).run();
    }    

    static void retry(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Runnable f) {
        retried(p, beforeSleep, f).run();
    }    

    static void retry(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Runnable f,
            Runnable afterSleep) {
        retried(p, beforeSleep, f, afterSleep).run();
    }
    
    static void retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Runnable f) {
        retried(p, exceptionClasses, beforeSleep, f).run();
    }    
    
    static void retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Runnable f,
            Runnable afterSleep) {
        retried(p, exceptionClasses, beforeSleep, f, afterSleep).run();
    }

    // -------------- Supplier ----------------- //
    static <R> R retry(int numRetries, long sleep, Supplier<R> f) {
        return retried(numRetries, sleep, f).get();
    }    

    static <R> R retry(
            RetryPolicy p,
            Supplier<R> f) {
        return retried(p, f).get();
    }
    
    static <R> R retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Supplier<R> f) {
        return retried(p, exceptionClasses, f).get();
    }    

    static <R> R retry(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Supplier<R> f) {
        return retried(p, beforeSleep, f).get();
    }    
    
    static <R> R retry(
            RetryPolicy p,
            Consumer<Exception> beforeSleep,
            Supplier<R> f,
            Runnable afterSleep) {
        return retried(p, beforeSleep, f, afterSleep).get();
    }
    
    static <R> R retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Supplier<R> f) {
        return retried(p, exceptionClasses, beforeSleep, f).get();
    }    
    
    static <R> R retry(
            RetryPolicy p,
            Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep,
            Supplier<R> f,
            Runnable afterSleep) {
        return retried(p, exceptionClasses, beforeSleep, f, afterSleep).get();
    }   

}

/**
 * This decorator facilitates wrapping a method call with retries whenever exception is thrown by
 * that method.  The number of retries and sleep time between them is defined in the given
 * {@code RetryPolicy} object.
 * 
 * <p>The decorator may be given an array of exception types on
 * which decorator should catch and retry.  If no array of exception classes is given, the decorator
 * will execute on any type of exception caught.
 * Exceptions types are checked with {@code Class.isInstance} method, so the decorator will
 * run on the given exceptions' subclasses as well.
 * 
 * <p>Also, the decorator may be given call-backs which it calls right after an exception is caught,
 * and after it is done sleeping.
 * 
 *  @see RetryPolicy
 */
final class RetryDecorator<T,U,R> implements Decorator<T,U,R> {
    
    private final RetryPolicy retryPolicy;
    private final Class<? extends Exception>[] exceptionClasses;
    private final Optional<Consumer<Exception>> before;
    private final Optional<Runnable> after;

    /**
     * Package access constructor.
     * 
     * @param retryPolicy        Retry policy
     * @param exceptionClasses   Array of exception classes on which the decorator should execute
     * @param before             A lambda to run on exception thrown (before the decorator goes to sleep)
     * @param after              A lambda to run after the decorator wakes up from sleeping
     */
    RetryDecorator(RetryPolicy retryPolicy, Class<? extends Exception>[] exceptionClasses, 
            Consumer<Exception> before, Runnable after) {
        this.retryPolicy = retryPolicy;
        this.exceptionClasses = exceptionClasses;
        this.before = Optional.ofNullable(before);
        this.after  = Optional.ofNullable(after);
    }
    
    /**
     * Contains decorator logic. The decorator will:
     * 1) Wrap a function in try/catch and call it
     * 2) If exception happens, check if exception is of one the target types (if given)
     * 3) If yes, consult the retry policy on how long it should sleep before the next retry.  If
     *    the retry policy returns 0 as sleep time, stop retrying and re-throw the
     *    exception.
     * 4) If the retry policy returns a result greater than zero, execute "before" 
     *    call back (if given), sleep for specified time, and execute an "after" callback (if present)
     */
    @Override
    public BiFunction<T, U, R> decorate(BiFunction<T, U, R> f) {
        return (t, u) -> {
            while (true) {
                try {
                    return f.apply(t, u);
                } catch (Exception e) {
                    if (ofTargetClass(e)) {
                        long sleepTime = retryPolicy.nextRetryIn();
                        if (sleepTime > 0) {
                            before.ifPresent(before -> before.accept(e));
                            Threads.sleep(sleepTime);
                            after.ifPresent(after -> after.run());
                            continue;
                        }
                    }
                    throw e;
                }
            }
        };
    }
    
    private boolean ofTargetClass(Exception e) {
        if (exceptionClasses == null) {
            return true;
        }
        for (Class<? extends Exception> klass : exceptionClasses) {
            if (klass.isInstance(e)) {
                return true;
            }
        }
        return false;
    }
    
}

