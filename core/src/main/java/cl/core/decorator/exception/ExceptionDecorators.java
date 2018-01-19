package cl.core.decorator.exception;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cl.core.function.BiConsumerWithException;
import cl.core.function.BiFunctionWithException;
import cl.core.function.ConsumerWithException;
import cl.core.function.FunctionWithException;
import cl.core.function.RunnableWithException;
import cl.core.function.SupplierWithException;

/**
 * This interface contains methods which expose functionality of different 
 * {@link cl.core.decorator.DecoratorWithException} implementations.
 * 
 * <p>There are two groups of methods.
 * <ul>
 *   <li>
 *      Methods which help converting checked exceptions into unchecked exceptions.
 *      These are {@code unchecked()} and {@code uncheck()} methods.
 *   </li>
 *   <li>
 *      Methods which catch exceptions, and return NULL values whenever an
 *      exception happens. These are {@code safe()} and {@code safely()} methods.
 *   </li>
 * </ul>
 * 
 * <p>Within each group, there are also two types of methods.
 * <ul>
 *   <li>
 *      Methods which "decorate" functions without actually executing them, for
 *      example {@code unchecked()}, {@code safe()}.
 *   </li>
 *   <li>
 *      Methods which "decorate" functions and execute them with or without returning
 *      the result, for example {@code uncheck()}, {@code safely()}.  Methods in 
 *      this group, usually, only apply to functions which take not parameters, that is
 *      {@code Runnable} and {@code Supplier}.
 *   </li>
 * </ul>
 * 
 * <p>The following snippet illustrates how to use {@code unchecked()} methods.
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
 * using one of the {@code uncheck()} methods, like in the following snippet:
 * 
 * <pre>{@code
 *   File f = getMyFile();
 *   Fist<String> lines = uncheck(() -> Files.readAllLines(f.toPath()));
 * }</pre>
 * 
 * <p>{@code safe()} and {@code safely()} methods may be used similarly.
 */
public interface ExceptionDecorators {

    static Runnable unchecked(RunnableWithException f) {
        return new Uncheck<>().decorate(f);
    }

    static Runnable unchecked(Class<? extends RuntimeException> exceptionClass, RunnableWithException f) {
        return new Uncheck<>(exceptionClass).decorate(f);
    }
    
    static <R> Supplier<R> unchecked(SupplierWithException<R> f) {
        return new Uncheck<Object, Object, R>().decorate(f);
    }
    
    static <R> Supplier<R> unchecked(Class<? extends RuntimeException> exceptionClass, SupplierWithException<R> f) {
        return new Uncheck<Object, Object, R>(exceptionClass).decorate(f);
    }
    
    static <T> Consumer<T> unchecked(ConsumerWithException<T> f) {
        return new Uncheck<T, Object, Object>().decorate(f);
    }

    static <T> Consumer<T> unchecked(Class<? extends RuntimeException> exceptionClass, ConsumerWithException<T> f) {
        return new Uncheck<T, Object, Object>(exceptionClass).decorate(f);
    }
    
    static <T,U> BiConsumer<T,U> unchecked(BiConsumerWithException<T,U> f) {
        return new Uncheck<T, U, Object>().decorate(f);
    }

    static <T,U> BiConsumer<T,U> unchecked(Class<? extends RuntimeException> exceptionClass, BiConsumerWithException<T,U> f) {
        return new Uncheck<T, U, Object>(exceptionClass).decorate(f);
    }    

    static <T,R> Function<T,R> unchecked(FunctionWithException<T,R> f) {
        return new Uncheck<T, Object, R>().decorate(f);
    }    

    static <T,R> Function<T,R> unchecked(Class<? extends RuntimeException> exceptionClass, FunctionWithException<T,R> f) {
        return new Uncheck<T, Object, R>(exceptionClass).decorate(f);
    }
    
    static <T,U,R> BiFunction<T,U,R> unchecked(BiFunctionWithException<T,U,R> f) {
        return new Uncheck<T, U, R>().decorate(f);
    }    

    static <T,U,R> BiFunction<T,U,R> unchecked(Class<? extends RuntimeException> exceptionClass, BiFunctionWithException<T,U,R> f) {
        return new Uncheck<T, U, R>(exceptionClass).decorate(f);
    }
    
    static void uncheck(RunnableWithException f) {
        unchecked(f).run();
    }
    
    static void uncheck(Class<? extends RuntimeException> exceptionClass, RunnableWithException f) {
        unchecked(exceptionClass, f).run();
    }
    
    static <R> R uncheck(SupplierWithException<R> f) {
        return unchecked(f).get();
    }
    
    static <R> R uncheck(Class<? extends RuntimeException> exceptionClass, SupplierWithException<R> f) {
        return unchecked(exceptionClass, f).get();
    }
    
    static <R> Supplier<R> safe(SupplierWithException<R> f) {
        return new Safe<Object, Object, R>().decorate(f);
    }
    
    @SafeVarargs
    static <R> Supplier<R> safe(SupplierWithException<R> f, Class<? extends Exception> ... exceptions) {
        return new Safe<Object, Object, R>(exceptions).decorate(f);
    }
    
    static <T,R> Function<T,R> safe(FunctionWithException<T,R> f) {
        return new Safe<T, Object, R>().decorate(f);
    }
    
    @SafeVarargs
    static <T,R> Function<T,R> safe(FunctionWithException<T,R> f, Class<? extends Exception> ... exceptions) {
        return new Safe<T, Object, R>(exceptions).decorate(f);
    }
    
    static <T,U,R> BiFunction<T,U,R> safe(BiFunctionWithException<T,U,R> f) {
        return new Safe<T, U, R>().decorate(f);
    }
    
    @SafeVarargs
    static <T,U,R> BiFunction<T,U,R> safe(BiFunctionWithException<T,U,R> f, Class<? extends Exception> ... exceptions) {
        return new Safe<T, U, R>(exceptions).decorate(f);
    }
    
    
    static <R> R safely(SupplierWithException<R> f) {
        return safe(f).get();
    }
    
    @SafeVarargs
    static <R> R safely(SupplierWithException<R> f, Class<? extends Exception> ... exceptions) {
        return safe(f, exceptions).get();
    }
    
}