package cl.core.util;

import static cl.core.util.Reflections.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

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
    
    @Test
    public void testTrySetField() {
        AllFieldTypesObject obj = new AllFieldTypesObject();
        
        trySetField("string", "Hello", obj);
        assertEquals("Hello", obj.string);

        trySetField("primByte", Byte.toString(Byte.MIN_VALUE), obj);
        assertEquals(Byte.MIN_VALUE, obj.primByte);
        trySetField("objByte", Byte.toString(Byte.MIN_VALUE), obj);
        assertEquals(new Byte(Byte.MIN_VALUE), obj.objByte);
        
        trySetField("primShort", Short.toString(Short.MIN_VALUE), obj);
        assertEquals(Short.MIN_VALUE, obj.primShort);
        trySetField("objShort", Short.toString(Short.MIN_VALUE), obj);
        assertEquals(new Short(Short.MIN_VALUE), obj.objShort);

        trySetField("primInt", Integer.toString(Integer.MIN_VALUE), obj);
        assertEquals(Integer.MIN_VALUE, obj.primInt);
        trySetField("objInt", Integer.toString(Integer.MIN_VALUE), obj);
        assertEquals(new Integer(Integer.MIN_VALUE), obj.objInt);
        
        trySetField("primLong", Long.toString(Long.MIN_VALUE), obj);
        assertEquals(Long.MIN_VALUE, obj.primLong);
        trySetField("objLong", Long.toString(Long.MIN_VALUE), obj);
        assertEquals(new Long(Long.MIN_VALUE), obj.objLong);
        
        trySetField("primFloat", Float.toString(Float.MIN_VALUE), obj);
        assertEquals(Float.MIN_VALUE, obj.primFloat, 0.0001);
        trySetField("objFloat", Float.toString(Float.MIN_VALUE), obj);
        assertEquals(new Float(Float.MIN_VALUE), obj.objFloat);
        
        trySetField("primDouble", Double.toString(Double.MIN_VALUE), obj);
        assertEquals(Double.MIN_VALUE, obj.primDouble, 0.0001);
        trySetField("objDouble", Double.toString(Double.MIN_VALUE), obj);
        assertEquals(new Double(Double.MIN_VALUE), obj.objDouble);
        
        trySetField("primChar", Character.toString(Character.MAX_VALUE), obj);
        assertEquals(Character.MAX_VALUE, obj.primChar);
        trySetField("objChar", Character.toString(Character.MAX_VALUE), obj);
        assertEquals(new Character(Character.MAX_VALUE), obj.objChar);
        
        trySetField("primBoolean", Boolean.TRUE.toString(), obj);
        assertEquals(Boolean.TRUE, obj.primBoolean);
        trySetField("objBoolean", Boolean.TRUE.toString(), obj);
        assertEquals(Boolean.TRUE, obj.objBoolean);
        
        trySetField("bigInteger", "123455656456456465646", obj);
        assertEquals(new BigInteger("123455656456456465646"), obj.bigInteger);
        
        trySetField("bigDecimal", "123455656456456465646.123123123123", obj);
        assertEquals(new BigDecimal("123455656456456465646.123123123123"), obj.bigDecimal);
        
        Date date = new Date();
        trySetField("date", date.toString(), obj);
        assertEquals(date.toString(), obj.date.toString()); // java Date.toString() looses milliseconds
                                                            // that's why we have to compare string representations
                                                            // instead of actual objects
        LocalTime t = LocalTime.now();
        trySetField("localTime", t.toString(), obj);
        assertEquals(t, obj.localTime);
        
        LocalDate d = LocalDate.now();
        trySetField("localDate", d.toString(), obj);
        assertEquals(d, obj.localDate);
        
        LocalDateTime dt = LocalDateTime.now();
        trySetField("localDateTime", dt.toString(), obj);
        assertEquals(dt, obj.localDateTime);
        
        ZonedDateTime zdt = ZonedDateTime.now();
        trySetField("zonedDateTime", zdt.toString(), obj);
        assertEquals(zdt, obj.zonedDateTime);
        
        trySetField("count", Count.one.toString(), obj);
        assertEquals(Count.one, obj.count);
    }
    
    @Test
    public void testFindAndCallSetter() {
        AllFieldTypesObject obj = new AllFieldTypesObject();
        
        findAndCallSetter("string", "Hello", obj);
        assertEquals("Hello", obj.string);

        findAndCallSetter("primByte", Byte.toString(Byte.MIN_VALUE), obj);
        assertEquals(Byte.MIN_VALUE, obj.primByte);
        findAndCallSetter("objByte", Byte.toString(Byte.MIN_VALUE), obj);
        assertEquals(new Byte(Byte.MIN_VALUE), obj.objByte);
        
        findAndCallSetter("primShort", Short.toString(Short.MIN_VALUE), obj);
        assertEquals(Short.MIN_VALUE, obj.primShort);
        findAndCallSetter("objShort", Short.toString(Short.MIN_VALUE), obj);
        assertEquals(new Short(Short.MIN_VALUE), obj.objShort);

        findAndCallSetter("primInt", Integer.toString(Integer.MIN_VALUE), obj);
        assertEquals(Integer.MIN_VALUE, obj.primInt);
        findAndCallSetter("objInt", Integer.toString(Integer.MIN_VALUE), obj);
        assertEquals(new Integer(Integer.MIN_VALUE), obj.objInt);
        
        findAndCallSetter("primLong", Long.toString(Long.MIN_VALUE), obj);
        assertEquals(Long.MIN_VALUE, obj.primLong);
        findAndCallSetter("objLong", Long.toString(Long.MIN_VALUE), obj);
        assertEquals(new Long(Long.MIN_VALUE), obj.objLong);
        
        findAndCallSetter("primFloat", Float.toString(Float.MIN_VALUE), obj);
        assertEquals(Float.MIN_VALUE, obj.primFloat, 0.0001);
        findAndCallSetter("objFloat", Float.toString(Float.MIN_VALUE), obj);
        assertEquals(new Float(Float.MIN_VALUE), obj.objFloat);
        
        findAndCallSetter("primDouble", Double.toString(Double.MIN_VALUE), obj);
        assertEquals(Double.MIN_VALUE, obj.primDouble, 0.0001);
        findAndCallSetter("objDouble", Double.toString(Double.MIN_VALUE), obj);
        assertEquals(new Double(Double.MIN_VALUE), obj.objDouble);
        
        findAndCallSetter("primChar", Character.toString(Character.MAX_VALUE), obj);
        assertEquals(Character.MAX_VALUE, obj.primChar);
        findAndCallSetter("objChar", Character.toString(Character.MAX_VALUE), obj);
        assertEquals(new Character(Character.MAX_VALUE), obj.objChar);
        
        findAndCallSetter("primBoolean", Boolean.TRUE.toString(), obj);
        assertEquals(Boolean.TRUE, obj.primBoolean);
        findAndCallSetter("objBoolean", Boolean.TRUE.toString(), obj);
        assertEquals(Boolean.TRUE, obj.objBoolean);
        
        findAndCallSetter("bigInteger", "123455656456456465646", obj);
        assertEquals(new BigInteger("123455656456456465646"), obj.bigInteger);
        
        findAndCallSetter("bigDecimal", "123455656456456465646.123123123123", obj);
        assertEquals(new BigDecimal("123455656456456465646.123123123123"), obj.bigDecimal);
        
        Date date = new Date();
        findAndCallSetter("date", date.toString(), obj);
        assertEquals(date.toString(), obj.date.toString()); // java Date.toString() looses milliseconds
                                                            // that's why we have to compare string representations
                                                            // instead of actual objects
        LocalTime t = LocalTime.now();
        findAndCallSetter("localTime", t.toString(), obj);
        assertEquals(t, obj.localTime);
        
        LocalDate d = LocalDate.now();
        findAndCallSetter("localDate", d.toString(), obj);
        assertEquals(d, obj.localDate);
        
        LocalDateTime dt = LocalDateTime.now();
        findAndCallSetter("localDateTime", dt.toString(), obj);
        assertEquals(dt, obj.localDateTime);
        
        ZonedDateTime zdt = ZonedDateTime.now();
        findAndCallSetter("zonedDateTime", zdt.toString(), obj);
        assertEquals(zdt, obj.zonedDateTime);
        
        findAndCallSetter("count", Count.one.toString(), obj);
        assertEquals(Count.one, obj.count);
    }
    
    @Test
    public void testFieldExists() {
        AllFieldTypesObject obj = new AllFieldTypesObject();
        try {
            assertTrue(fieldExists("primByte", obj));
            assertFalse(fieldExists("primbyte", obj));
        } catch (Exception e) {
            fail("should not get any exceptions while checking field existsence");
        }
    }
    
    @Test
    public void testSetterExists() {
        AllFieldTypesObject obj = new AllFieldTypesObject();
        try {
            assertTrue(setterExists("primByte", obj));
            assertFalse(fieldExists("primbyte", obj));
        } catch (Exception e) {
            fail("should not get any exceptions while checking setter existsence");
        }
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
    
    static enum Count {one, two, three}
    
    static class AllFieldTypesObject {
        private String string;
        private byte primByte;
        private Byte objByte;
        private short primShort;
        private Short objShort;
        private int primInt;
        private Integer objInt;
        private long primLong;
        private Long objLong;
        private float primFloat;
        private Float objFloat;
        private double primDouble;
        private Double objDouble;
        private char primChar;
        private Character objChar;
        private boolean primBoolean;
        private Boolean objBoolean;
        private BigInteger bigInteger;
        private BigDecimal bigDecimal;
        private Date date;
        private LocalTime localTime;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private ZonedDateTime zonedDateTime;
        private Count count;
        
        public void setString(String string) {
            this.string = string;
        }
        public void setPrimByte(byte primByte) {
            this.primByte = primByte;
        }
        public void setObjByte(Byte objByte) {
            this.objByte = objByte;
        }
        public void setPrimShort(short primShort) {
            this.primShort = primShort;
        }
        public void setObjShort(Short objShort) {
            this.objShort = objShort;
        }
        public void setPrimInt(int primInt) {
            this.primInt = primInt;
        }
        public void setObjInt(Integer objInt) {
            this.objInt = objInt;
        }
        public void setPrimLong(long primLong) {
            this.primLong = primLong;
        }
        public void setObjLong(Long objLong) {
            this.objLong = objLong;
        }
        public void setPrimFloat(float primFloat) {
            this.primFloat = primFloat;
        }
        public void setObjFloat(Float objFloat) {
            this.objFloat = objFloat;
        }
        public void setPrimDouble(double primDouble) {
            this.primDouble = primDouble;
        }
        public void setObjDouble(Double objDouble) {
            this.objDouble = objDouble;
        }
        public void setPrimChar(char primChar) {
            this.primChar = primChar;
        }
        public void setObjChar(Character objChar) {
            this.objChar = objChar;
        }
        public void setPrimBoolean(boolean primBoolean) {
            this.primBoolean = primBoolean;
        }
        public void setObjBoolean(Boolean objBoolean) {
            this.objBoolean = objBoolean;
        }
        public void setBigInteger(BigInteger bigInteger) {
            this.bigInteger = bigInteger;
        }
        public void setBigDecimal(BigDecimal bigDecimal) {
            this.bigDecimal = bigDecimal;
        }
        public void setDate(Date date) {
            this.date = date;
        }
        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }
        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }
        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }
        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }
        public void setCount(Count count) {
            this.count = count;
        }
    }
    
}
