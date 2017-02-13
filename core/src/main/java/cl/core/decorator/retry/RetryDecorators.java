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
 * function with retry logic applied. These methods could be used whenever a resulting function needs
 * to be further decorated with some other decorators.
 * 
 * <p> If a method name is "retry", it will execute the given function with retry logic applied.
 * 
 * @see RetryDecorator
 * @see RetryPolicy
 */
public interface RetryDecorators {
    
    ///////////////// decorators //////////////////////

    // -------------- Runnable ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Runnable}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(int numRetries, long sleep, Runnable f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Runnable f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Runnable f) {
        return retried(p, exceptionClasses, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     *  
     * @param p            retry policy
     * @param beforeSleep  code to execute before going to sleep
     * @param f            code to retry
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Consumer<Exception> beforeSleep, Runnable f) {
        return retried(p, null, beforeSleep, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Consumer<Exception> beforeSleep, Runnable f, Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Runnable f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     * @return {@code Runnable} which will retry in case of an error
     */
    static Runnable retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Runnable f, Runnable afterSleep) {
        return new RetryDecorator<>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    // -------------- Supplier ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Supplier}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(int numRetries, long sleep, Supplier<R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Supplier<R> f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute retries
     * @param f                 code to retry
     * @return                  {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Supplier<R> f) {
        return retried(p, exceptionClasses, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     *  
     * @param p           retry policy
     * @param beforeSleep code to execute before going to sleep
     * @param f           code to retry
     * @return            {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Supplier<R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after sleeping
     * @return              {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Supplier<R> f, Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     *  
     * @param p                retry policy
     * @param exceptionClasses exception types on which to execute the retries
     * @param beforeSleep      code to execute before sleeping
     * @param f                code to retry
     * @return                 {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Supplier<R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier}.
     * 
     * @param p                  retry policy
     * @param exceptionClasses   exception types on which to execute the retries
     * @param beforeSleep        code to execute before sleeping
     * @param f                  code to retry
     * @param afterSleep         code to execute after sleeping
     * @return                   {@code Supplier} which will retry in case of an error
     */
    static <R> Supplier<R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Supplier<R> f, Runnable afterSleep) {
        return new RetryDecorator<Object, Object, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    
    
    // -------------- Consumer ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Consumer}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(int numRetries, long sleep, Consumer<T> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Consumer<T> f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Consumer<T> f) {
        return retried(p, exceptionClasses, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     *  
     * @param p            retry policy
     * @param beforeSleep  code to execute before going to sleep
     * @param f            code to retry
     * @return {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Consumer<T> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     * @return {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Consumer<T> f, Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @return {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Consumer<T> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Consumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     * @return {@code Consumer} which will retry in case of an error
     */
    static <T> Consumer<T> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Consumer<T> f, Runnable afterSleep) {
        return new RetryDecorator<T, Object, Object>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    
    // -------------- BiConsumer ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code BiConsumer}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code BiConsumer} which will retry in case of an error
     */
    static <T,U> BiConsumer<T,U> retried(int numRetries, long sleep, BiConsumer<T,U> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, BiConsumer<T, U> f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            BiConsumer<T, U> f) {
        return retried(p, exceptionClasses, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     *  
     * @param p            retry policy
     * @param beforeSleep  code to execute before going to sleep
     * @param f            code to retry
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, Consumer<Exception> beforeSleep, BiConsumer<T, U> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, Consumer<Exception> beforeSleep, BiConsumer<T, U> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, BiConsumer<T, U> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiConsumer}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     * @return {@code BiConsumer} which will retry in case of an error
     */
    static <T, U> BiConsumer<T, U> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, BiConsumer<T, U> f, Runnable afterSleep) {
        return new RetryDecorator<T, U, Object>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }

    
    // -------------- Function ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Function}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code Function} which will retry in case of an error
     */
    static <T,R> Function<T,R> retried(int numRetries, long sleep, Function<T,R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Function<T, R> f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Function<T, R> f) {
        return retried(p, exceptionClasses, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     *  
     * @param p            retry policy
     * @param beforeSleep  code to execute before going to sleep
     * @param f            code to retry
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Function<T, R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Consumer<Exception> beforeSleep, Function<T, R> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Function<T, R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Function}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     * @return {@code Function} which will retry in case of an error
     */
    static <T, R> Function<T, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, Function<T, R> f, Runnable afterSleep) {
        return new RetryDecorator<T, Object, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    

    // -------------- BiFunction ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code BiFunction}.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     * @return           {@code BiFunction} which will retry in case of an error
     */
    static <T,U,R> BiFunction<T,U,R> retried(int numRetries, long sleep, BiFunction<T,U,R> f) {
        return retried(new SimpleRetryPolicy(numRetries, sleep), null, null, f, null);
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     * 
     * @param p retry policy
     * @param f code to retry
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, BiFunction<T, U, R> f) {
        return retried(p, null, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            BiFunction<T, U, R> f) {
        return retried(p, exceptionClasses, null, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     *  
     * @param p            retry policy
     * @param beforeSleep  code to execute before going to sleep
     * @param f            code to retry
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, Consumer<Exception> beforeSleep,
            BiFunction<T, U, R> f) {
        return retried(p, null, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, Consumer<Exception> beforeSleep, BiFunction<T, U, R> f,
            Runnable afterSleep) {
        return retried(p, null, beforeSleep, f, afterSleep);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, BiFunction<T, U, R> f) {
        return retried(p, exceptionClasses, beforeSleep, f, null);
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code BiFunction}.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     * @return {@code BiFunction} which will retry in case of an error
     */
    static <T, U, R> BiFunction<T, U, R> retried(RetryPolicy p, Class<? extends Exception>[] exceptionClasses,
            Consumer<Exception> beforeSleep, BiFunction<T, U, R> f, Runnable afterSleep) {
        return new RetryDecorator<T, U, R>(p, exceptionClasses, beforeSleep, afterSleep).decorate(f);
    }
    
    
    ///////////////// decorator applications //////////////////////
    
    // -------------- Runnable ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     */
    static void retry(int numRetries, long sleep, Runnable f) {
        retried(numRetries, sleep, f).run();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p retry policy
     * @param f code to retry
     */
    static void retry(RetryPolicy p, Runnable f) {
        retried(p, f).run();
    }    
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  classes, which represent type of exceptions on which the retry policy will be applied
     * @param f                 code to retry                  
     */
    static void retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Runnable f) {
        retried(p, exceptionClasses, f).run();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     */
    static void retry(RetryPolicy p, Consumer<Exception> beforeSleep, Runnable f) {
        retried(p, beforeSleep, f).run();
    }    

    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after going to sleep
     */
    static void retry(RetryPolicy p, Consumer<Exception> beforeSleep, Runnable f, Runnable afterSleep) {
        retried(p, beforeSleep, f, afterSleep).run();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     */
    static void retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Consumer<Exception> beforeSleep,
            Runnable f) {
        retried(p, exceptionClasses, beforeSleep, f).run();
    }    
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Runnable} and execute it.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute the retries
     * @param beforeSleep       code to execute before going to sleep
     * @param f                 code to retry
     * @param afterSleep        code to execute after sleeping
     */
    static void retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Consumer<Exception> beforeSleep,
            Runnable f, Runnable afterSleep) {
        retried(p, exceptionClasses, beforeSleep, f, afterSleep).run();
    }

    
    // -------------- Supplier ----------------- //
    
    /**
     * Apply {@link SimpleRetryPolicy} to a {@code Supplier} and execute it.
     * 
     * @param numRetries how many times to retry
     * @param sleep      for how long to sleep between retries
     * @param f          code to retry
     */
    static <R> R retry(int numRetries, long sleep, Supplier<R> f) {
        return retried(numRetries, sleep, f).get();
    }    

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     * 
     * @param p retry policy
     * @param f code to retry
     */
    static <R> R retry(RetryPolicy p, Supplier<R> f) {
        return retried(p, f).get();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     * 
     * @param p                 retry policy
     * @param exceptionClasses  exception types on which execute retries
     * @param f                 code to retry
     */
    static <R> R retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Supplier<R> f) {
        return retried(p, exceptionClasses, f).get();
    }

    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     *  
     * @param p           retry policy
     * @param beforeSleep code to execute before going to sleep
     * @param f           code to retry
     */
    static <R> R retry(RetryPolicy p, Consumer<Exception> beforeSleep, Supplier<R> f) {
        return retried(p, beforeSleep, f).get();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     * 
     * @param p             retry policy
     * @param beforeSleep   code to execute before going to sleep
     * @param f             code to retry
     * @param afterSleep    code to execute after sleeping
     */
    static <R> R retry(RetryPolicy p, Consumer<Exception> beforeSleep, Supplier<R> f, Runnable afterSleep) {
        return retried(p, beforeSleep, f, afterSleep).get();
    }
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     *  
     * @param p                retry policy
     * @param exceptionClasses exception types on which to execute the retries
     * @param beforeSleep      code to execute before sleeping
     * @param f                code to retry
     */
    static <R> R retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Consumer<Exception> beforeSleep,
            Supplier<R> f) {
        return retried(p, exceptionClasses, beforeSleep, f).get();
    }    
    
    /**
     * Apply given {@link RetryPolicy} to a {@code Supplier} and execute it.
     * 
     * @param p                  retry policy
     * @param exceptionClasses   exception types on which to execute the retries
     * @param beforeSleep        code to execute before sleeping
     * @param f                  code to retry
     * @param afterSleep         code to execute after sleeping
     */
    static <R> R retry(RetryPolicy p, Class<? extends Exception>[] exceptionClasses, Consumer<Exception> beforeSleep,
            Supplier<R> f, Runnable afterSleep) {
        return retried(p, exceptionClasses, beforeSleep, f, afterSleep).get();
    }

}

/**
 * This decorator applies retry logic as defined in the given {@link RetryPoliy}.
 * 
 * <p>The decorator may be given an array of exception types which it should intercept.
 * If no array of exception classes is given, the decorator will retry on any exception type.
 *  
 * <p>Exception types are checked with {@code Class.isInstance} method, so the decorator will
 * intercept exception subclasses as well.
 * 
 * <p>Also, the decorator may be given callbacks which it executes right after an exception is caught,
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
     * @param exceptionClasses   Array of exception classes on which the decorator should retry
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
     * Implements the logic of this decorator.  This decorator will:
     * 
     * <ol>
     * <li>Wrap a function in try/catch and call it.</li>
     * <li>If exception happens, check if it is of one of the target types (if any).</li>
     * <li>Consult the retry policy on how long to sleep before the next retry. If
     *    the retry policy returns 0, stop retrying and re-throw the
     *    exception.</li>
     * <li>Else if the retry policy returns a result greater than zero, execute "before" 
     *    call back (if given), sleep for specified time, and execute an "after" callback (if given)</li>
     * </ol>
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

