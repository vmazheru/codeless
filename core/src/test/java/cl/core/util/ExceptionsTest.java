package cl.core.util;

import static cl.core.util.Exceptions.*;
import static org.junit.Assert.*;

import org.junit.Test;

import cl.core.ds.Pair;

public class ExceptionsTest {
    
    @Test
    public void testGetRootCause() {
        Exception root = new Exception("The one which root cause is self");
        assertEquals(root, getRootCause(root));
        Pair<Exception, Exception> exWithRoot = exceptionWithItsRoot();
        assertEquals(exWithRoot._2(), getRootCause(exWithRoot._1()));
    }
    
    @Test
    public void testGetStackTrace() {
        Exception ex = getNestedExceptions();
        assertTrue(getStackTrace(ex).startsWith("java.lang.Exception: " + ex.getMessage()));
    }
    
    @Test
    public void testGetRootStackTrace() {
        Pair<Exception, Exception> exWithRoot = exceptionWithItsRoot();
        assertTrue(getRootStackTrace(exWithRoot._1()).startsWith("java.lang.Exception: " + exWithRoot._2().getMessage()));
    }
    
    private static Exception getNestedExceptions() {
        return exceptionWithItsRoot()._1();
    }
    
    private static Pair<Exception, Exception> exceptionWithItsRoot() {
        Exception root = new Exception("Deeply nested root cause");
        Exception intermediate = new Exception("Some intermediate exception", root);
        Exception actuallyCaught = new Exception("The one which was actually caught", intermediate);
        return new Pair<>(actuallyCaught, root);
    }

}
