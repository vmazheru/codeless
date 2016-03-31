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
 * throw run-time exceptions.
 */
public interface ExceptionDecorators {
    
    //////////////////////// decorators ///////////////////////////
    
    static Runnable unchecked(RunnableWithException f) {
        return new ExceptionWrappingDecorator<>().decorate(f);
    }
    
    static Runnable unchecked(Class<? extends RuntimeException> exceptionClass, RunnableWithException f) {
        return new ExceptionWrappingDecorator<>(exceptionClass).decorate(f);
    }
    
    static <R> Supplier<R> unchecked(SupplierWithException<R> f) {
        return new ExceptionWrappingDecorator<Object, Object, R>().decorate(f);
    }
    
    static <R> Supplier<R> unchecked(Class<? extends RuntimeException> exceptionClass, SupplierWithException<R> f) {
        return new ExceptionWrappingDecorator<Object, Object, R>(exceptionClass).decorate(f);
    }
    
    static <T> Consumer<T> unchecked(ConsumerWithException<T> f) {
        return new ExceptionWrappingDecorator<T, Object, Object>().decorate(f);
    }
    
    static <T> Consumer<T> unchecked(Class<? extends RuntimeException> exceptionClass, ConsumerWithException<T> f) {
        return new ExceptionWrappingDecorator<T, Object, Object>(exceptionClass).decorate(f);
    }
    
    static <T,U> BiConsumer<T,U> unchecked(BiConsumerWithException<T,U> f) {
        return new ExceptionWrappingDecorator<T, U, Object>().decorate(f);
    }
    
    static <T,U> BiConsumer<T,U> unchecked(Class<? extends RuntimeException> exceptionClass, BiConsumerWithException<T,U> f) {
        return new ExceptionWrappingDecorator<T, U, Object>(exceptionClass).decorate(f);
    }    

    static <T,R> Function<T,R> unchecked(FunctionWithException<T,R> f) {
        return new ExceptionWrappingDecorator<T, Object, R>().decorate(f);
    }    
    
    static <T,R> Function<T,R> unchecked(Class<? extends RuntimeException> exceptionClass, FunctionWithException<T,R> f) {
        return new ExceptionWrappingDecorator<T, Object, R>(exceptionClass).decorate(f);
    }
    
    static <T,U,R> BiFunction<T,U,R> unchecked(BiFunctionWithException<T,U,R> f) {
        return new ExceptionWrappingDecorator<T, U, R>().decorate(f);
    }    
    
    static <T,U,R> BiFunction<T,U,R> unchecked(Class<? extends RuntimeException> exceptionClass, BiFunctionWithException<T,U,R> f) {
        return new ExceptionWrappingDecorator<T, U, R>(exceptionClass).decorate(f);
    }
    
    ///////////////////////// decorator applications ////////////////
    
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
    
}

/**
 * Implementation of {@code DecoratorWithException}
 * which wraps checked exceptions into unchecked exceptions.
 */
final class ExceptionWrappingDecorator<T,U,R> implements DecoratorWithException<T,U,R> {
    
    private Class<? extends RuntimeException> exceptionClass;

    ExceptionWrappingDecorator(){}
    
    ExceptionWrappingDecorator (Class<? extends RuntimeException> exceptionClass) {
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
