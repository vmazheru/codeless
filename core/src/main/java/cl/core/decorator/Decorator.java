package cl.core.decorator;

import static cl.core.function.Conversions.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Decorator interface. Most decorators which implement this interface will only need to override
 * one method which is {@code <T, U, R> BiFunction<T, U, R> decorate(BiFunction<T, U, R> f)}.
 * All other methods will delegate to this most general method.
 */
public interface Decorator<T,U,R> {
    
    /**
     * Decorate a Runnable
     */
    default Runnable decorate(Runnable f) {
        return toR(decorate(toBF(f)));
    }

    /**
     * Decorate a Supplier
     */
    default Supplier<R> decorate(Supplier<R> f) {
        return toS(decorate(toBF(f)));
    }

    /**
     * Decorate a Consumer
     */
    default Consumer<T> decorate(Consumer<T> f) {
        return toC(decorate(toBF(f)));
    }

    /**
     * Decorate a Function
     */
    default Function<T, R> decorate(Function<T, R> f) {
        return toF(decorate(toBF(f)));
    }

    /**
     * Decorate a BiConsumer
     */
    default BiConsumer<T, U> decorate(BiConsumer<T, U> f) {
        return toBC(decorate(toBF(f)));
    }

    /**
     * Decorate a BiFunction
     */
    BiFunction<T, U, R> decorate(BiFunction<T, U, R> f);
}
