package cl.core.util;

import static org.junit.Assert.*;
import static cl.core.util.Strings.*;

import org.junit.Test;

public class StirngsTest {
    
    @Test
    public void testCapitalize() {
        assertEquals("Hello, World", capitalize("hello, World"));
        assertEquals("Hello, World", capitalize("Hello, World"));
        assertEquals("Hello, world", capitalize("hello, World", true));
        assertEquals("", capitalize(""));
        assertEquals(" ", capitalize(" "));
        assertEquals(null, capitalize(null));
    }
    
    @Test
    public void testSnakeToCamel() {
        assertEquals("helloHappyWorld", snakeToCamel("hello_happy_world"));
        assertEquals("", snakeToCamel(""));
        assertEquals(null, snakeToCamel(null));
    }
    
    @Test
    public void testDashedToCamel() {
        assertEquals("helloHappyWorld", dashedToCamel("hello-happy-world"));
        assertEquals("", dashedToCamel(""));
        assertEquals(null, dashedToCamel(null));
    }
    
    @Test
    public void testDottedToCamel() {
        assertEquals("helloHappyWorld", dottedToCamel("hello.happy.world"));
        assertEquals("", dottedToCamel(""));
        assertEquals(null, dottedToCamel(null));
    }
    
    @Test
    public void testSpacedToCamel() {
        assertEquals("helloHappyWorld", spacedToCamel("Hello happy world"));
        assertEquals("helloHappyWorld", spacedToCamel("Hello   Happy    WOrld"));
        assertEquals("", spacedToCamel("   "));
        assertEquals("", spacedToCamel(""));
        assertEquals(null, spacedToCamel(null));
    }

}
