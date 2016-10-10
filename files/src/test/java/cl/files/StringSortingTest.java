package cl.files;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

import cl.files.filesorters.FileSorter;
import cl.files.serializers.iterators.StringIterator;
import cl.files.serializers.writers.ObjectWriter;
import cl.files.serializers.writers.StringWriter;

/**
 * Unit tests in this class verify static sort() methods in FileSorter interface.
 */
public class StringSortingTest {

    /**
     * Test FileSorter.sort(File f)
     */
    @Test
    public void testSortSameFile() {
        File input = unsortedFile(false);
        try {
            FileSorter.sort(input);
            assertTrue(isSorted(input, false, false, Comparator.naturalOrder()));
        } finally {
            input.delete();
        }
    }
    
    /**
     * Test FileSorter.sort(File input, File output)
     */
    @Test
    public void testSortDifferentFiles() {
        File input = unsortedFile(false);
        File output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output);
            assertTrue(isSorted(output, false, false, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
    }
    
    /**
     * Test FileSorter.sort(
     *  File input, File output, boolean removeDuplicates, int numHeaderLines, Comparator<String> c)
     */
    @Test
    public void testSortWithAllParameters() {
        // test removeDuplicates = true and numHeaderLines = 2
        File input = unsortedFile(true);
        File output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true, 2, Comparator.naturalOrder());
            assertTrue(isSorted(output, true, true, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = false and numHeaderLines = 2
        input = unsortedFile(true);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, false, 2, Comparator.naturalOrder());
            assertTrue(isSorted(output, true, false, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = true and numHeaderLines = 0
        input = unsortedFile(false);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true, 0, Comparator.naturalOrder());
            assertTrue(isSorted(output, false, true, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = true , numHeaderLines = 2, and reversed comparator
        input = unsortedFile(true);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true, 2, Comparator.reverseOrder());
            assertTrue(isSorted(output, true, true, Comparator.reverseOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = false , numHeaderLines = 0, and reversed comparator
        input = unsortedFile(false);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, false, 0, Comparator.reverseOrder());
            assertTrue(isSorted(output, false, false, Comparator.reverseOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = false , numHeaderLines = 2, and reversed comparator
        input = unsortedFile(true);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, false, 2, Comparator.reverseOrder());
            assertTrue(isSorted(output, true, false, Comparator.reverseOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
    }
    
    /**
     * Test sort(File input, File output, boolean removeDuplicates, int numHeaderLines)
     */
    @Test
    public void testSortWithoutComparatorParam() {
        // test removeDuplicates = true and numHeaderLines = 2
        File input = unsortedFile(true);
        File output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true, 2);
            assertTrue(isSorted(output, true, true, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = false and numHeaderLines = 2
        input = unsortedFile(true);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, false, 2);
            assertTrue(isSorted(output, true, false, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = true and numHeaderLines = 0
        input = unsortedFile(false);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true, 0);
            assertTrue(isSorted(output, false, true, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }        
    }
    
    /**
     * Test FileSorter.sort(File input, File output, boolean removeDuplicates) 
     */
    @Test
    public void testSortWithRemoveDuplicates() {
        // test removeDuplicates = true
        File input = unsortedFile(false);
        File output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, true);
            assertTrue(isSorted(output, false, true, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }
        
        // test removeDuplicates = false
        input = unsortedFile(false);
        output = emptyFile();
        try {
            List<String> linesFromInput = StringIterator.fromFile(input).read();
            FileSorter.sort(input, output, false, 0);
            assertTrue(isSorted(output, false, false, Comparator.naturalOrder()));
            assertEquals(linesFromInput, StringIterator.fromFile(input).read());
        } finally {
            input.delete();
            output.delete();
        }        
    }
    
    /**
     * Test FileSorter.sort(
     *  File f, boolean removeDuplicates, int numHeaderLines, Comparator<String> c)
     */
    @Test
    public void testSortWithAllParametersAndOneFile() {
        // test removeDuplicates = true and numHeaderLines = 2
        File file = unsortedFile(true);
        try {
            FileSorter.sort(file, true, 2, Comparator.naturalOrder());
            assertTrue(isSorted(file, true, true, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = false and numHeaderLines = 2
        file = unsortedFile(true);
        try {
            FileSorter.sort(file, file, false, 2, Comparator.naturalOrder());
            assertTrue(isSorted(file, true, false, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = true and numHeaderLines = 0
        file = unsortedFile(false);
        try {
            FileSorter.sort(file, true, 0, Comparator.naturalOrder());
            assertTrue(isSorted(file, false, true, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = true , numHeaderLines = 2, and reversed comparator
        file = unsortedFile(true);
        try {
            FileSorter.sort(file, true, 2, Comparator.reverseOrder());
            assertTrue(isSorted(file, true, true, Comparator.reverseOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = false , numHeaderLines = 0, and reversed comparator
        file = unsortedFile(false);
        try {
            FileSorter.sort(file, false, 0, Comparator.reverseOrder());
            assertTrue(isSorted(file, false, false, Comparator.reverseOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = false , numHeaderLines = 2, and reversed comparator
        file = unsortedFile(true);
        try {
            FileSorter.sort(file, false, 2, Comparator.reverseOrder());
            assertTrue(isSorted(file, true, false, Comparator.reverseOrder()));
        } finally {
            file.delete();
        }
    }
    
    /**
     * Test sort(File file, boolean removeDuplicates, int numHeaderLines)
     */
    @Test
    public void testSortWithoutComparatorParamWithOneFile() {
        // test removeDuplicates = true and numHeaderLines = 2
        File file = unsortedFile(true);
        try {
            FileSorter.sort(file, true, 2);
            assertTrue(isSorted(file, true, true, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = false and numHeaderLines = 2
        file = unsortedFile(true);
        try {
            FileSorter.sort(file, false, 2);
            assertTrue(isSorted(file, true, false, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = true and numHeaderLines = 0
        file = unsortedFile(false);
        try {
            FileSorter.sort(file, true, 0);
            assertTrue(isSorted(file, false, true, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }        
    } 
    
    /**
     * Test FileSorter.sort(File file, boolean removeDuplicates) 
     */
    @Test
    public void testSortWithRemoveDuplicatesWithOneFile() {
        // test removeDuplicates = true
        File file = unsortedFile(false);
        try {
            FileSorter.sort(file, true);
            assertTrue(isSorted(file, false, true, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }
        
        // test removeDuplicates = false
        file = unsortedFile(false);
        try {
            FileSorter.sort(file, false, 0);
            assertTrue(isSorted(file, false, false, Comparator.naturalOrder()));
        } finally {
            file.delete();
        }        
    }

    private static List<String> getHeaders() {
        List<String> lines = new ArrayList<>();
        lines.add("header 1");
        lines.add("header 2");
        return lines;
    }
    
    private static List<String> getLines() {
        List<String> lines = new ArrayList<>();
        lines.add("1");
        lines.add("2");
        lines.add("2");
        lines.add("3");
        lines.add("3");
        lines.add("3");
        lines.add("4");
        return lines;
    }
    
    private static List<String> getShuffledLines() {
        List<String> lines = getLines();
        Collections.shuffle(lines);
        return lines;
    }
    
    private static List<String> getSortedLines(Comparator<String> c) {
        List<String> lines = getLines();
        Collections.sort(lines, c);
        return lines;
    }
    
    private static List<String> getSortedUniqueLines(Comparator<String> c) {
        return new ArrayList<>(new LinkedHashSet<>(getSortedLines(c)));
    }
    
    private static File unsortedFile(boolean withHeader) {
        return
        uncheck(() -> {
            File f = File.createTempFile("tmp", ".txt");
            try (ObjectWriter<String> w = StringWriter.toFile(f)) {
                if (withHeader) {
                    w.write(getHeaders());
                }
                w.write(getShuffledLines());
            }
            return f;
        });
    }
    
    private static File emptyFile() {
        return uncheck(() -> File.createTempFile("tmp", ".txt"));
    }
    
    private static boolean isSorted(File f, boolean withHeader, boolean removeDuplicates, Comparator<String> c) {
        List<String> listFromFile = StringIterator.fromFile(f).read();
        List<String> expectedLines = removeDuplicates ? getSortedUniqueLines(c) : getSortedLines(c);
        boolean headerOk = true;
        boolean dataOk = true;
        if (withHeader) {
            headerOk = listFromFile.subList(0, getHeaders().size()).equals(getHeaders());
            dataOk = listFromFile.subList(2, listFromFile.size()).equals(expectedLines);
        } else {
            dataOk = listFromFile.equals(expectedLines);
        }
        return headerOk && dataOk;
    }
    
}
