package cl.core.decorator;

import static cl.core.function.Conversions.*;

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
 * The purpose of this decorator is to transform functions which throw checked exceptions into functions 
 * which throw unchecked exception.
 * See a description of the {@code Decorator} class on how decorators work.
 * 
 * @see Decorator 
 */
public interface DecoratorWithException<T,U,R> {
    
    /**
     * Decorate a RunnableWithException
     */
    default Runnable decorate(RunnableWithException f) {
        return toR(decorate(toBF(f)));
    }

    /**
     * Decorate a SupplierWithException
     */
    default Supplier<R> decorate(SupplierWithException<R> f) {
        return toS(decorate(toBF(f)));
    }

    /**
     * Decorate a ConsumerWithException
     */
    default Consumer<T> decorate(ConsumerWithException<T> f) {
        return toC(decorate(toBF(f)));
    }

    /**
     * Decorate a FunctionWithException
     */
    default Function<T, R> decorate(FunctionWithException<T, R> f) {
        return toF(decorate(toBF(f)));
    }

    /**
     * Decorate a BiConsumerWithException
     */
    default BiConsumer<T, U> decorate(BiConsumerWithException<T, U> f) {
        return toBC(decorate(toBF(f)));
    }

    /**
     * Decorate a BiFunctionWithException
     */
    BiFunction<T, U, R> decorate(BiFunctionWithException<T, U, R> f);
}