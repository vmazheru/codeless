package cl.core.util;

import static cl.core.util.Sets.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class SetsTest {

    @Test
    public void testUnion() {
        Collection<String> a = Arrays.asList("one", "two", "two", "three", "four", "five");
        Collection<String> b = Arrays.asList("four", "five", "five", "six");
        String[] expected = {"one", "two", "three", "four", "five", "six"}; 
        
        assertHashSet(expected, union(a,b));
        assertTreeSet(expected, union(a,b, TreeSet::new));
        assertLinkSet(expected, union(a,b, LinkedHashSet::new));
    }
    
    @Test
    public void testIntersection() {
        Collection<String> a = Arrays.asList("one", "two", "two", "three", "four", "five");
        Collection<String> b = Arrays.asList("four", "five", "five", "six");
        String[] expected = {"four", "five"}; 
        
        assertHashSet(expected, intersection(a,b));
        assertTreeSet(expected, intersection(a,b, TreeSet::new));
        assertLinkSet(expected, intersection(a,b, LinkedHashSet::new));
    }
    
    @Test
    public void testDifference() {
        Collection<String> a = Arrays.asList("one", "two", "two", "three", "four", "five");
        Collection<String> b = Arrays.asList("four", "five", "five", "six");
        String[] expected = {"one", "two", "three"}; 
        
        assertHashSet(expected, difference(a,b));
        assertTreeSet(expected, difference(a,b, TreeSet::new));
        assertLinkSet(expected, difference(a,b, LinkedHashSet::new));
    }
    
    @Test
    public void testComplement() {
        Collection<String> a = Arrays.asList("one", "two", "two", "three", "four", "five");
        Collection<String> b = Arrays.asList("four", "five", "five", "six");
        String[] expected = {"one", "two", "three", "six"}; 
        
        assertHashSet(expected, complement(a,b));
        assertTreeSet(expected, complement(a,b, TreeSet::new));
        assertLinkSet(expected, complement(a,b, LinkedHashSet::new));
    }
    
    private static void assertHashSet(String[] expected, Set<String> set) {
        assertTrue(set instanceof HashSet);
        assertEquals(new HashSet<>(Arrays.asList(expected)), set);
    }
    
    private static void assertTreeSet(String[] expected, Set<String> set) {
        assertTrue(set instanceof TreeSet);
        assertEquals(new TreeSet<>(Arrays.asList(expected)), set);
    }
    
    private static void assertLinkSet(String[] expected, Set<String> set) {
        assertTrue(set instanceof LinkedHashSet);
        int i = 0;
        for (String s : set) {
            assertEquals(expected[i++], s);
        }
    }
}
