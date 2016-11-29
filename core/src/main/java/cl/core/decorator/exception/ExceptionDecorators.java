package cl.core.decorator.exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cl.core.decorator.DecoratorWithException;
import cl.core.function.BiConsumerWithException;
import cl.core.function.BiFunctionWithException;
import cl.core.function.ConsumerWithException;
import cl.core.function.FunctionWithException;
import cl.core.function.RunnableWithException;
import cl.core.function.SupplierWithException;

/**
 * Exception decorators take functions which throw checked exceptions and return functions which
 * throw run-time (unchecked) exceptions.
 * 
 * <p>Behind the scenes, all functions in this interface use an actual implementation of
 * {@link DecoratorWithException} interface which does the following:
 * 
 * <ol>
 *  <li>Call the underlying function.</li>
 *  <li>
 *      If the exception is thrown and the specific exception class is passed as an argument,
 *      try to throw an instance of that class.  If creating it by the means of reflection fails, just
 *      throw a {@code RutimeException}.
 *  </li>
 *  <li>
 *      If no specific exception class is requested, then if the exception caught is {@code IOException},
 *      throw {@code UncheckedIOException}.
 *  </li>
 *  <li>Else, just throw {@RuntimeException}.</li>
 * </ol>
 * 
 * <p>The methods in this interface can be broken down into two groups:
 * <ul>
 *   <li>
 *      Methods which accepts AND return functions of the same type (the actual "decorators").
 *      These are all overloaded versions of {@code unchecked()} method
 *   </li>
 *   <li>
 *     Methods which accept functions, decorate them by applying one of the methods from the first group,
 *     and then execute the resulting functions returning the actual value (if any).  These are all
 *     overloaded versions of {@code uncheck()} method. These methods are only implemented for those types
 *     of functions which don't take arguments (that is for {@code Runnable} and {@code Supplier}).
 *   </li>
 * </ul>
 * 
 * <p>The following snippet illustrates how to use methods from the first group:
 * 
 * <pre>{@code
 *  Function<File, List<String> readAllLinesF = unchecked(file -> Files.readAllLines(file.toPath()));
 *  
 *  // that you have a new function which reads all lines from a file, but throws a run-time exception
 *  // instead of IOException
 *  
 *  readAllLinesF.apply(myFile);
 * }</pre>
 * 
 * <p>For functions which don't accept arguments (Runnable and Supplier),
 * it's easier to combine decorating a function with calling it in one line by
 * using one of the methods from the second group, like in the following snippet:
 * 
 * <pre>{@code
 *   File f = getMyFile();
 *   Fist<String> lines = uncheck(() -> Files.readAllLines(f.toPath()));
 * }</pre>
 */
public interface ExceptionDecorators {

    /**
     * Convert {@link RunnableWithException} into {@code Runnable}.
     */
    static Runnable unchecked(RunnableWithException f) {
        return new DecoratorWithExceptionImpl<>().decorate(f);
    }

    /**
     * Convert {@link RunnableWithException} into {@code Runnable}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the runnable
     *        whenever the underlying runnable throws a checked exception
     */
    static Runnable unchecked(Class<? extends RuntimeException> exceptionClass, RunnableWithException f) {
        return new DecoratorWithExceptionImpl<>(exceptionClass).decorate(f);
    }
    
    /**
     * Convert {@link SupplierWithException} into {@code Supplier}.
     */
    static <R> Supplier<R> unchecked(SupplierWithException<R> f) {
        return new DecoratorWithExceptionImpl<Object, Object, R>().decorate(f);
    }
    
    /**
     * Convert {@link SupplierWithException} into {@code Supplier}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the supplier
     *        whenever the underlying supplier throws a checked exception
     */
    static <R> Supplier<R> unchecked(Class<? extends RuntimeException> exceptionClass, SupplierWithException<R> f) {
        return new DecoratorWithExceptionImpl<Object, Object, R>(exceptionClass).decorate(f);
    }
    
    /**
     * Convert {@link ConsumerWithException} into {@code Consumer}.
     */
    static <T> Consumer<T> unchecked(ConsumerWithException<T> f) {
        return new DecoratorWithExceptionImpl<T, Object, Object>().decorate(f);
    }

    /**
     * Convert {@link ConsumerWithException} into {@code Consumer}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the consumer
     *        whenever the underlying consumer throws a checked exception
     */
    static <T> Consumer<T> unchecked(Class<? extends RuntimeException> exceptionClass, ConsumerWithException<T> f) {
        return new DecoratorWithExceptionImpl<T, Object, Object>(exceptionClass).decorate(f);
    }
    
    /**
     * Convert {@link BiConsumerWithException} into {@code BiConsumer}.
     */
    static <T,U> BiConsumer<T,U> unchecked(BiConsumerWithException<T,U> f) {
        return new DecoratorWithExceptionImpl<T, U, Object>().decorate(f);
    }

    /**
     * Convert {@link BiConsumerWithException} into {@code BiConsumer}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the consumer
     *        whenever the underlying consumer throws a checked exception
     */
    static <T,U> BiConsumer<T,U> unchecked(Class<? extends RuntimeException> exceptionClass, BiConsumerWithException<T,U> f) {
        return new DecoratorWithExceptionImpl<T, U, Object>(exceptionClass).decorate(f);
    }    

    /**
     * Convert {@link FunctionWithException} into {@code Function}.
     */
    static <T,R> Function<T,R> unchecked(FunctionWithException<T,R> f) {
        return new DecoratorWithExceptionImpl<T, Object, R>().decorate(f);
    }    

    /**
     * Convert {@link FunctionWithException} into {@code Function}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the function
     *        whenever the underlying function throws a checked exception
     */
    static <T,R> Function<T,R> unchecked(Class<? extends RuntimeException> exceptionClass, FunctionWithException<T,R> f) {
        return new DecoratorWithExceptionImpl<T, Object, R>(exceptionClass).decorate(f);
    }
    
    /**
     * Convert {@link BiFunctionWithException} into {@code BiFunction}.
     */
    static <T,U,R> BiFunction<T,U,R> unchecked(BiFunctionWithException<T,U,R> f) {
        return new DecoratorWithExceptionImpl<T, U, R>().decorate(f);
    }    

    /**
     * Convert {@link BiFunctionWithException} into {@code BiFunction}.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown by the function
     *        whenever the underlying function throws a checked exception
     */
    static <T,U,R> BiFunction<T,U,R> unchecked(Class<? extends RuntimeException> exceptionClass, BiFunctionWithException<T,U,R> f) {
        return new DecoratorWithExceptionImpl<T, U, R>(exceptionClass).decorate(f);
    }
    

    /**
     * Convert {@code RunnableWithException} into a {@code Runnable} and execute it.
     */
    static void uncheck(RunnableWithException f) {
        unchecked(f).run();
    }
    
    /**
     * Convert {@code RunnableWithException} into a {@code Runnable} and execute it.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown
     *        whenever the underlying runnable throws a checked exception
     */
    static void uncheck(Class<? extends RuntimeException> exceptionClass, RunnableWithException f) {
        unchecked(exceptionClass, f).run();
    }
    
    /**
     * Convert {@code SupplierWithException} into a {@code Supplier} and execute it.
     */
    static <R> R uncheck(SupplierWithException<R> f) {
        return unchecked(f).get();
    }
    
    /**
     * Convert {@code SupplierWithException} into a {@code Supplier} and execute it.
     * 
     * @param exceptionClass run-time exception class, instances of which will be thrown
     *        whenever the underlying runnable throws a checked exception
     */
    static <R> R uncheck(Class<? extends RuntimeException> exceptionClass, SupplierWithException<R> f) {
        return unchecked(exceptionClass, f).get();
    }
    
}

/**
 * Implementation of {@code DecoratorWithException}
 * which "decorates" code which may throw checked exceptions into code which may throw
 * run-time exceptions.
 */
final class DecoratorWithExceptionImpl<T,U,R> implements DecoratorWithException<T,U,R> {
    
    private Class<? extends RuntimeException> exceptionClass;

    DecoratorWithExceptionImpl(){}
    
    DecoratorWithExceptionImpl (Class<? extends RuntimeException> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    /**
     * Execute the given function.  In case of exception, catch it and wrap into a specified 
     * exception, if any, or a general run-time exception.
     */
    @Override
    public BiFunction<T,U,R> decorate(BiFunctionWithException<T,U,R> f) {
        return (t, u) -> {
            try {
                return f.apply(t, u);
            } catch (Exception e) {
                if (exceptionClass != null) {
                    try {
                        RuntimeException ex = exceptionClass.getConstructor(Throwable.class).newInstance(e);
                        throw ex;
                    } catch (NoSuchMethodException  | InvocationTargetException | 
                             IllegalAccessException | InstantiationException ex) {
                        // let's not do anything here and just continue with throwing a runtime exception
                        ex.printStackTrace();
                    }
                }
                if (e instanceof IOException) {
                    throw new UncheckedIOException((IOException)e);
                }
                throw new RuntimeException(e);
            }
        };
    }

}
