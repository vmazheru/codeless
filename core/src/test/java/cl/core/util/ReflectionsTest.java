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
        // test normally
        SampleObject obj = new SampleObject();
        set("value", "some value", obj);
        assertEquals("some value", obj.getValue());
        
        // call the setter for non existing property
        try {
            set("wrongProperty", "some value", obj);
            fail("Setting wrong property must result in NoSuchMethodException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        
        // call the setter with wrong data type
        try {
            set("value", 42, obj);
            fail("Setting property to the value of wrong data type must result in NoSuchMethodException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }
    
    @Test
    public void testGet() {
        SampleObject obj = new SampleObject();
        obj.setValue("my value");
        assertEquals(obj.getValue(), get("value", obj));
    }
    
    @Test
    public void testSetField() {
        final String value = "my value"; 
        SampleObject obj = new SampleObject();
        setField("value", value, obj);
        assertEquals(value, obj.getValue() );
    }
    
    @Test
    public void testGetField() {
        SampleObject obj = new SampleObject();
        obj.setValue("my value");
        assertEquals(obj.getValue(), getField("value", obj));
    }
    
    @Test
    public void testCall() {
        final String value = "abcdef";
        SampleObject obj = new SampleObject();
        obj.setValue(value);
        String result = call("append", obj);
        assertEquals(value + value, result);
        
        obj.setValue(value);
        result = call("append", obj, "123");
        assertEquals(value + "123", result);
        
        obj.setValue(value);
        result = call("append", obj, "123", "456");
        assertEquals(value + "123456", result);
        
        obj.setValue(value);
        result = call("append", obj, "123", "456", "789");
        assertEquals(value + "123456789", result);
        
        obj.setValue(value);
        assertEquals(value + "123ABC_.txt", call("append", obj, "123", "ABC", "_", ".txt"));
    }
    
    static class SampleObject {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String append() { return append(value); }
        public String append(String other) { value += other; return value; }
        public String append(String s1, String s2) { value += s1; value += s2; return value; }
        public String append(String s1, String s2, String s3) { value += s1; value += s2; value += s3; return value; }
        public String append(String ... others) {
            for (String s : others) append(s) ; 
            return value;
        }
    }
    
}
