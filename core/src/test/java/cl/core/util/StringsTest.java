package cl.core.util;

import static org.junit.Assert.*;
import static cl.core.util.Strings.*;

import org.junit.Test;

public class StringsTest {

    @Test
    public void testCapitalize() {
        assertNull(capitalize(null));
        assertTrue(capitalize("").isEmpty());
        assertEquals("HeLLo", capitalize("heLLo"));
    }
    
    @Test
    public void testToTitleCase() {
        assertNull(toTitleCase(null));
        assertTrue(toTitleCase("").isEmpty());
        assertEquals("Hello", toTitleCase("heLLo"));
    }
}
