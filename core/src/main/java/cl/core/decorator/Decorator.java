package cl.core.decorator;

import static cl.core.function.Conversions.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@code Decorator} is an object which changes the behavior of a given function
 * in a certain way.
 * 
 * <p>For example, adding a retry logic to your method can be done by creating a decorator.  Such
 * a decorator can later be used with any method.  The other examples include aspects like exception
 * handling, batching, throttling, transaction handling, etc.
 * 
 * <p>{@code Decorator} interface defines a number of overloaded {@code decorate()} methods each of 
 * which takes an object of a specific function type and returns a function of the same type.
 * The returned function can be called normally as the original function, but will expose some additional behavior.
 * 
 * <p>To implement a decorator, one has to implement only one {@code decorate()} method, which takes
 * a parameter of type {@code BiFunction}. The other {@code decorate()} methods will delegate to it by
 * converting their input functions into a {@code BiFunction} and back.
 * 
 * <p>The following snippet shows how to create a decorator which executes its wrapped method
 * after a delay of 1 second.
 * 
 * <pre>{@code
 *   public class DelayDecorator implements Decorator {
 *       public <T, U, R> BiFunction<T, U, R> decorate(BiFunction<T, U, R> f) {
 *           return (t,u) -> {
 *             try { Thread.sleep(1000); } catch (InterruptedException e) { }
 *             return f.apply(t, u);
 *           };
 *       }
 *   
 *       public static void withDelay(Runnable r) {
 *           new DelayDecorator().decorate(r).run();
 *       }
 *       
 *       public static void main(String[] args) {
 *          // the parameter here is Runnable not a BiFunction, but still works
 *           withDelay(() -> System.out.prinltn("Hello, world!")); 
 *       }
 *   }
 * }</pre>
 * 
 */
public interface Decorator<T,U,R> {
    
    /**
     * Decorate a {@code Runnable}
     */
    default Runnable decorate(Runnable f) {
        return toR(decorate(toBF(f)));
    }

    /**
     * Decorate a {@code Supplier}
     */
    default Supplier<R> decorate(Supplier<R> f) {
        return toS(decorate(toBF(f)));
    }

    /**
     * Decorate a {@code Consumer}
     */
    default Consumer<T> decorate(Consumer<T> f) {
        return toC(decorate(toBF(f)));
    }

    /**
     * Decorate a {@code Function}
     */
    default Function<T, R> decorate(Function<T, R> f) {
        return toF(decorate(toBF(f)));
    }

    /**
     * Decorate a {@code BiConsumer}
     */
    default BiConsumer<T, U> decorate(BiConsumer<T, U> f) {
        return toBC(decorate(toBF(f)));
    }

    /**
     * Decorate a {@code BiFunction}
     */
    BiFunction<T, U, R> decorate(BiFunction<T, U, R> f);
}
