package cl.core.function;

import static cl.core.function.Conversions.*;
import static org.junit.Assert.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

/**
 * Function conversion tests.
 */
public class ConversionTest {
    
    private static final String RV = "foo"; // return value for our functions
    
    /*
     * Facility to introduce side effects.
     */
    private static class SomeValue {
        private Integer v = new Integer(0);
        Integer getV()       { return v; }
        void setV(Integer v) { this.v = v; }
        void increment() { v = new Integer(v.intValue() + 1); }
    }
    
    private SomeValue value;
    private SomeValue value2;
    
    // our test functions
    private Runnable r = () -> value.increment();
    private Supplier<String> s = () ->  { value.increment(); return RV; };
    private Consumer<Integer> c = (i) -> value.setV(i);
    private BiConsumer<Integer, Integer> bc = (i,j) -> { value.setV(i); value2.setV(j); };
    private Function<Integer, String> f = (i) -> { value.setV(i); return RV; };
    private BiFunction<Integer, Integer, String> bf = (i,j) -> { value.setV(i); value2.setV(j); return RV; };
    
    @Before
    public void beforeTest() {
        value  = new SomeValue();
        value2 = new SomeValue();
    }

    /**
     * Test conversions between Runnable and other functions.
     */
    @Test
    public void testRunnable() {
        r.run();                          assertEquals(new Integer(1), value.getV());
        assertNull(toS(r).get());         assertEquals(new Integer(2), value.getV()); // no return value
        toC(r).accept(55);                assertEquals(new Integer(3), value.getV()); // argument ignored
        toBC(r).accept(55, 77);           assertEquals(new Integer(4), value.getV()); // both arguments ignored
        assertNull(toF(r).apply(55));     assertEquals(new Integer(5), value.getV()); // no return value, argument ignored
        assertNull(toBF(r).apply(55,77)); assertEquals(new Integer(6), value.getV()); // no return value, both arguments ignored
    }
    
    @Test
    public void testSupplier() {
        toR(s).run();                           assertEquals(new Integer(1), value.getV()); // side effect produced
        assertEquals(RV, s.get());              assertEquals(new Integer(2), value.getV());
        toC(s).accept(55);                      assertEquals(new Integer(3), value.getV()); // argument ignored
        toBC(s).accept(55, 77);                 assertEquals(new Integer(4), value.getV()); // both arguments ignored
        assertEquals(RV, toF(s).apply(55));     assertEquals(new Integer(5), value.getV()); // return value passed, argument ignored
        assertEquals(RV, toBF(s).apply(55,77)); assertEquals(new Integer(6), value.getV()); // no return value, both arguments ignored
    }
    
    @Test
    public void testConsumer() {
        toR(c).run();                      assertNull(value.getV());                    // null value as a side effect
        assertNull(toS(c).get());          assertNull(value.getV());                    // null value as a side effect
        c.accept(55);                      assertEquals(new Integer(55), value.getV());
        toBC(c).accept(77, 987);           assertEquals(new Integer(77), value.getV()); // first argument passed, second argument ignored
        assertNull(toF(c).apply(99));      assertEquals(new Integer(99), value.getV()); // no return value, argument passed
        assertNull(toBF(c).apply(55,987)); assertEquals(new Integer(55), value.getV()); // no return value, first argument passed, second argument ignored
    }
    
    @Test
    public void testBiConsumer() {
        toR(bc).run();                        assertNull(value.getV());                     // null value as a side effect
                                              assertNull(value2.getV());
        assertNull(toS(bc).get());            assertNull(value.getV());                     // no return value, null value as a side effect
                                              assertNull(value2.getV());
        toC(bc).accept(5);                    assertEquals(new Integer(5), value.getV());   // first argument passed, second argument ignored
                                              assertNull(value2.getV());
        bc.accept(55, 77);                    assertEquals(new Integer(55), value.getV());
                                              assertEquals(new Integer(77), value2.getV());
        assertNull(toF(bc).apply(99));        assertEquals(new Integer(99), value.getV());  // no return value, argument passed
        assertNull(toBF(bc).apply(555, 777)); assertEquals(new Integer(555), value.getV()); // no return value, both arguments passed
                                              assertEquals(new Integer(777), value2.getV());
    }
    
    @Test
    public void testFunction() {
        toR(f).run();                            assertNull(value.getV());                    // null value as a side effect
        assertEquals(RV, toS(f).get());          assertNull(value.getV());                    // return value passed, null value as a side effect
        toC(f).accept(55);                       assertEquals(new Integer(55), value.getV()); // parameter passed
        toBC(f).accept(77, 987);                 assertEquals(new Integer(77), value.getV()); // first argument passed, second argument ignored
        assertEquals(RV, f.apply(99));           assertEquals(new Integer(99), value.getV());
        assertEquals(RV, toBF(f).apply(55,987)); assertEquals(new Integer(55), value.getV()); // return value passed, first argument passed, second argument ignored
    }

    @Test
    public void testBiFunction() {
        toR(bf).run();                        assertNull(value.getV());                     // null value as a side effect
                                              assertNull(value2.getV());
        assertEquals(RV, toS(bf).get());      assertNull(value.getV());                     // return value passed, null value as a side effect
                                              assertNull(value2.getV());
        toC(bf).accept(5);                    assertEquals(new Integer(5), value.getV());   // first argument passed, second argument ignored
                                              assertNull(value2.getV());
        toBC(bf).accept(55, 77);              assertEquals(new Integer(55), value.getV());  // both arguments passed
                                              assertEquals(new Integer(77), value2.getV());
        assertEquals(RV, toF(bf).apply(99));  assertEquals(new Integer(99), value.getV());  // return value passed, first argument passed
        assertEquals(RV, bf.apply(555, 777)); assertEquals(new Integer(555), value.getV()); 
                                              assertEquals(new Integer(777), value2.getV());
    }
    
}
