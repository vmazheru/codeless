package cl.core.util;

import static org.junit.Assert.*;
import static cl.core.util.Reflections.*;

import org.junit.Test;

/**
 * Unit tests for Reflections.java
 */
public class ReflectionsTest {

    @Test
    public void testSet() {
        
        class ObjectWithSetter {
            private String value;
            public String getValue() { return value; }
            @SuppressWarnings("unused")
            public void setValue(String value) { this.value = value; }
        }
        
        // test normally
        ObjectWithSetter obj = new ObjectWithSetter();
        set("value", "some value", obj);
        assertEquals("some value", obj.getValue());
        
        // call the setter for non existing property
        try {
            set("wrongProperty", "some value", obj);
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        
        // call the setter with wrong data type
        try {
            set("value", 42, obj);
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        
    }
    
}
