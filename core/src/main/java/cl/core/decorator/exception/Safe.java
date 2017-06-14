package cl.core.decorator.exception;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import cl.core.decorator.DecoratorWithException;
import cl.core.function.BiFunctionWithException;
import cl.core.util.Exceptions;

/**
 * This exception decorator implementation make execution of operations "safe",
 * that is it returns null instead of throwing an exception should something go wrong.
 * 
 * <p>An optional array of exception classes may be given to the decorator, so
 * it will only check for exceptions of these classes. Otherwise, it'll return 
 * null in case of any type of exception.
 */
class Safe <T,U,R> implements DecoratorWithException<T,U,R> {
    
    private Class<? extends Exception>[] catchExceptions;

    Safe(){}
    
    @SafeVarargs
    Safe(Class<? extends Exception> ... catchExceptions) {
        this.catchExceptions = catchExceptions;
    }

    @Override
    public BiFunction<T, U, R> decorate(BiFunctionWithException<T, U, R> f) {
        return (t, u) -> {
            try {
                return f.apply(t, u);
            } catch (Exception e) {
                if (catchExceptions == null || catchExceptions != null &&
                    Stream.of(catchExceptions).anyMatch(klass -> klass.isInstance(e))) {
                    return null;
                }
                throw Exceptions.toUnchecked(e);
            }
        };
    }

}
