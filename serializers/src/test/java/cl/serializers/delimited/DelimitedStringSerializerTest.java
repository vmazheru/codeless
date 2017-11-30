package cl.serializers.delimited;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class DelimitedStringSerializerTest {

    /**
     * Test serialization of primitive types in Java (this is the case which is hard
     * to cover in Scala specification).
     */
    @Test
    public void testSerializingOfPrimitives() {
        DelimitedStringSerializer<TestObj> s = DelimitedStringSerializer.get();
        assertArrayEquals(new String[] {"5", "0.5", "true", "a"} , s.serialize(new TestObj()));
    }
    
    class TestObj {
        int i = 5;
        double d = 0.5d;
        boolean b = true;
        char c = 'a';
    }
    
}
