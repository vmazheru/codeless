package cl.ugly.file;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * Implementations of this interface will be able to sort large files.
 */
public interface FileSorter {
	/**
	 * Sort a file using String natural sort ordering.
	 */
	File sort(File file) throws IOException;
	
	/**
	 * Sort a file using String natural sort ordering. Skip numHeaderLines on the top of the file.
	 * @return
	 */
	File sort(File file, int numHeaderLines) throws IOException;
	
	/**
	 * Sort a file using a given String comparator.
	 */
	File sort(File file, Comparator<String> comparator) throws IOException;
	
    /**
     * Sort a file using a given comparator. Skip numHeaderLines on the top of the file.
     */
    File sort(File file, Comparator<String> comparator, int numHeaderLines) throws IOException;
	
	/**
	 * Sort a file and remove duplicates from the result.
	 */
	File sort(File file, boolean removeDuplicates) throws IOException;
	
    /**
     * Sort a file and remove duplicates from the result. Skip numHeaderLines lines on top of the file.
     */
    File sort(File file, boolean removeDuplicates, int numHeaderLines) throws IOException;
	
	/**
	 * Sort a file with the given comparator and remove duplicates from the result.
	 */
	File sort(File file, Comparator<String> comparator, boolean removeDuplicates) throws IOException;
	
    /**
     * Sort a file with the given comparator and remove duplicates from the result.
     * Skip numHeaderLines on top of the file.
     */
    File sort(File file, Comparator<String> comparator, boolean removeDuplicates, int numHeaderLines) throws IOException;	
	
}

