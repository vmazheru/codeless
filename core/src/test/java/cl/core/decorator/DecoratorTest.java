package cl.core.decorator;

import static org.junit.Assert.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.Test;

import cl.core.decorator.Decorator;
import cl.core.ds.Counter;

/**
 * A simple test which illustrates the use of decorators in general.
 * We create and use a custom decorator which counts calls to a function.
 */
public class DecoratorTest {
    
    /*
     * Simple decorator which counts how many times the function was executed.
     */
    private static class CountDecorator<T,U,R> implements Decorator<T,U,R> {
        
        private Counter counter;
        
        CountDecorator(Counter counter) {
            this.counter = counter;
        }

        /**
         * A decorator usually needs to implement method which decorates the most "general" BiFunction
         * interface, the other function types will be converted to it by default.
         * 
         * @see cl.core.decorator.Decorator#decorate(java.util.function.BiFunction)
         */
        @Override
        public BiFunction<T, U, R> decorate(BiFunction<T, U, R> f) {
            return (t,u) -> {
                counter.increment();
                return f.apply(t, u);
            };
        }
        
        public static void count(Counter counter, Runnable r) {
            new CountDecorator<>(counter).decorate(r).run();
        }
        
        public static Integer count(Counter counter, Supplier<Integer> s) {
            return new CountDecorator<Object, Object, Integer>(counter).decorate(s).get();
        }
    }
    
    @Test
    public void testSimpleDecorator() {
        int TIMES = 7;
        Counter counter = new Counter();
        
        IntStream.range(0, TIMES).forEach(i -> {
            CountDecorator.count(counter, () -> {
                // do some stuff with that i
            });
        });
        assertEquals(TIMES, counter.getValue());
        
        counter.reset();
        
        IntStream.range(0, TIMES).forEach(i -> {
            CountDecorator.count(counter, () -> i);
        });
        assertEquals(TIMES, counter.getValue());
    }
    
}
