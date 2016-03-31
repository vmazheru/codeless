package cl.ugly.util;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import cl.core.ds.Pair;
import cl.ugly.util.Streams;

public class ZipStreamsTest {

    @Test
    public void testZip() {
        int size = 5;
        Stream<Integer> ints = Stream.of(new Integer[] {1,2,3,4,5});
        Stream<String> strings = Stream.of(new String[] {"1", "2", "3", "4", "5", "not", "consumed", "strings"});
        
        List<Pair<Integer, String>> pairs = Streams.zip(ints, strings).collect(toList());
        
        assertEquals(size, pairs.size());
        int i = 1;
        for (Pair<Integer, String> p : pairs) {
            assertEquals(new Integer(i), p._1());
            assertEquals(p._1(), Integer.valueOf(p._2()));
            i++;
            System.out.println(p);
        }
    }
    
}
