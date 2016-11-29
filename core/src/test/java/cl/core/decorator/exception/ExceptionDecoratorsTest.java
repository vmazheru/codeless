package cl.core.decorator.exception;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static org.junit.Assert.*;

import org.junit.Test;

import cl.core.ds.Counter;
import cl.core.function.BiConsumerWithException;
import cl.core.function.BiFunctionWithException;
import cl.core.function.ConsumerWithException;
import cl.core.function.FunctionWithException;
import cl.core.function.RunnableWithException;
import cl.core.function.SupplierWithException;

/**
 * Test static exception decorators
 */
public class ExceptionDecoratorsTest {

    @Test
    public void testDecorators() {
        testDecorator(1, () -> unchecked(Functions.runnable()).run());
        testDecorator(2, () -> uncheck(Functions.runnable()));
        testDecorator(3, () -> unchecked(Functions.supplier()).get());
        testDecorator(4, () -> uncheck(Functions.supplier()));
        testDecorator(5, () -> unchecked(Functions.consumer()).accept(1));
        testDecorator(6, () -> unchecked(Functions.biConsumer()).accept(1,2));
        testDecorator(7, () -> unchecked(Functions.function()).apply(1));
        testDecorator(8, () -> unchecked(Functions.biFunction()).apply(1,2));
        
        testDecoratorWithMyException( 9, () -> unchecked(MyRuntimeException.class, Functions.runnable()).run());
        testDecoratorWithMyException(10, () -> uncheck(MyRuntimeException.class, Functions.runnable()));
        testDecoratorWithMyException(11, () -> unchecked(MyRuntimeException.class, Functions.supplier()).get());
        testDecoratorWithMyException(12, () -> uncheck(MyRuntimeException.class, Functions.supplier()));
        testDecoratorWithMyException(13, () -> unchecked(MyRuntimeException.class, Functions.consumer()).accept(1));
        testDecoratorWithMyException(14, () -> unchecked(MyRuntimeException.class, Functions.biConsumer()).accept(1,2));
        testDecoratorWithMyException(15, () -> unchecked(MyRuntimeException.class, Functions.function()).apply(1));
        testDecoratorWithMyException(16, () -> unchecked(MyRuntimeException.class, Functions.biFunction()).apply(1,2));
    }
    
    private static void testDecorator(int expectedCounterValue, Runnable r) {
        try {
            r.run();
            fail("Runtime exception must be thrown");
        } catch (RuntimeException e) {
            assertEquals("foo", e.getCause().getMessage());
            assertEquals(expectedCounterValue, Functions.counter.get());
        }
    }
    
    private static void testDecoratorWithMyException(int expectedCounterValue, Runnable r) {
        try {
            r.run();
            fail("MyException must be thrown");
        } catch (MyRuntimeException e) {
            assertEquals("foo", e.getCause().getMessage());
            assertEquals(expectedCounterValue, Functions.counter.get());
        } catch (RuntimeException e) {
            fail("MyException must be thrown");
        }
    }
}

@SuppressWarnings("serial")
class MyRuntimeException extends RuntimeException {
    public MyRuntimeException(Throwable cause) { // must have a public constructor which takes a Throwable
        super(cause);
    }
}

class Functions {
    
    static Counter counter = new Counter();
    
    static RunnableWithException runnable() { 
        return () ->  {
            makeSideEffect();
            throwIt();
        };
    }
    
    static SupplierWithException<Integer> supplier() {
        return () ->  {
            makeSideEffect();
            throwIt();
            return 0;
        };
    }
    
    static ConsumerWithException<Integer> consumer() {
        return i -> {
            makeSideEffect();
            throwIt();
        };
    }
    
    static BiConsumerWithException<Integer, Integer> biConsumer() {
        return (list, i) -> {
            makeSideEffect();
            throwIt();
        };
    }
    
    static FunctionWithException<Integer, Integer> function() {
        return i -> {
            makeSideEffect();
            throwIt();
            return i+1;
        };
    }
    
    static BiFunctionWithException<Integer, Integer, Integer> biFunction() {
        return (a, b) -> {
            makeSideEffect();
            throwIt();
            return a + b;
        };
    }
    
    static private void makeSideEffect() {
        counter.increment();
    }
    
    private static void throwIt() throws Exception {
        throw new Exception("foo");
    }

}
