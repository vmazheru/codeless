package cl.core.decorator.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import cl.core.decorator.DecoratorWithException;
import cl.core.function.BiFunctionWithException;
import cl.core.util.Exceptions;

/**
 * This implementation of {@code DecoratorWithException}
 * turn code which may throw checked exceptions into code which may throw
 * run-time exceptions.
 */
class Uncheck <T,U,R> implements DecoratorWithException<T,U,R> {
    
    private Class<? extends RuntimeException> exceptionClass;

    Uncheck(){}
    
    Uncheck(Class<? extends RuntimeException> exceptionClass) {
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
                throw Exceptions.toUnchecked(e);
            }
        };
    }
}
