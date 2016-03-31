package cl.ugly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cl.ugly.file.FileSorterImpl;

/**
 * File utilities
 */
public final class FileUtils {
	
	public FileUtils() {}

	/**
	 * Dump lines into a file.
	 */
	public static void writeLines(File file, Collection<String> lines) throws IOException {
	    Files.write(file.toPath(), lines);
	}
	
	/**
	 * Read strings from file.
	 */
	public static List<String> readLines(File file) throws IOException {
	    return Files.readAllLines(file.toPath());
	}
	
	/**
	 * Read lineCount lines from a file
	 */
    public static List<String> readLines(File file, int lineCount) throws IOException {
    	List<String> result = new ArrayList<>();
    	try(BufferedReader in = new BufferedReader(new FileReader(file))) {
    		for(int i = 0; i < lineCount; i++) {
    			String line = in.readLine();
    			if(line != null) {
    				result.add(line);
    			} else {
    				break;
    			}
    		}
    	}
    	return result;
    }
    
    /**
     * Read first line from a file.
     */
    public static String readOneLine(File file) throws IOException {
    	List<String> lines = readLines(file, 1);
    	if(!lines.isEmpty()) {
    		return lines.get(0);
    	}
    	return null;
    }    

	/**
	 * Read the file, count lines. 
	 */
    public static int countLines(File file) throws IOException {
    	try(BufferedReader in = new BufferedReader(new FileReader(file))) {
    		String s;
    		int lineCount = 0;
    		while((s = in.readLine()) != null) {
    			if(!s.trim().isEmpty()) lineCount++;
    		}
    		return lineCount;
    	}
    }

    /**
     * Split the file to smaller files, each of which has no more rows than the limit.
     * If headerLines parameter is greater than zero, than the header lines will be copied from the 
     * original file to each of the result files.
     */
    public static List<File> chopByLines(File file, int rowLimit, String ext, int headerLines) throws IOException {
        int lines = countLines(file);
        if(lines <= rowLimit) {
            return Collections.singletonList(file);
        }

        List<File> files = new LinkedList<>();
        List<PrintWriter> writers = new LinkedList<>();
        
        try(BufferedReader in = new BufferedReader(new FileReader(file))) {
            List<String> header = null;
            if(headerLines > 0) {
                header = new ArrayList<>(headerLines);
                for (int i = 0; i < headerLines; i++) {
                    header.add(in.readLine());
                }
            }

            int currentRow = 0;
            int currentFileCount = 0;
            File currentFile = getNextFile(file, currentFileCount, ext);
            PrintWriter currentWriter = new PrintWriter(currentFile);
            files.add(currentFile);
            writers.add(currentWriter);
            
            if(header != null) {
                for (String h : header) {
                    currentWriter.println(h);
                    currentRow++;
                }
            }

            String row;
            while((row = in.readLine()) != null) {
                if(currentRow >= rowLimit) {
                    currentRow = 0;
                    currentFileCount++;
                    currentFile = getNextFile(file, currentFileCount, ext);
                    currentWriter = new PrintWriter(currentFile);
                    files.add(currentFile);
                    writers.add(currentWriter);
                    if(header != null) {
                        for (String h : header) {
                            currentWriter.println(h);
                            currentRow++;
                        }
                    }                    
                }
                currentWriter.println(row);
                currentRow++;
            }
        } finally {
            for(PrintWriter writer : writers) {
                if(writer != null) writer.close();
            }        	
        }

        return files;
    }

    /**
     * Split the file to smaller files, each of which has no more rows than the limit.
     * If copyHeader parameter is true, then the top line from the original file will be copied
     * to all result files.
     */
    public static List<File> chopByLines(File file, int rowLimit, String ext, boolean copyHeader) throws IOException {
        return chopByLines(file, rowLimit, ext, copyHeader ? 1 : 0);
    }    
    
	/**
     * Split the file to smaller files, each having the size no more than the given limit.
     * If headerLines parameter is greater than zero, than the header lines will be copied from the 
     * original file to each of the result files.
     */
	public static List<File> chopBySize(File file, long size, String ext, int headerLines) throws IOException {
		long fileSize = file.length();
		if(fileSize <= size) {
			return Collections.singletonList(file);
		}
		
		List<File> retVal = new LinkedList<>();
		List<PrintWriter> writers = new LinkedList<>();
		
		try(BufferedReader in = new BufferedReader(new FileReader(file))) {
			int currentFile = 0;
			File nextFile = getNextFile(file, currentFile, ext);
			retVal.add(nextFile);
			PrintWriter currentWriter = new PrintWriter(nextFile);
			writers.add(currentWriter);
			long currentSize = 0;
			
			List<String> header = null;
			int headerSize = 0;
			if(headerLines > 0) {
			    header = new ArrayList<>(headerLines);
			    for (int i = 0; i < headerLines; i++) {
			        String h = in.readLine();
			        header.add(h);
			        headerSize += (h.toCharArray().length * 2 + 1);
			        currentWriter.println(h);
			    }
				currentSize += headerSize; 
			}
			
			String row;
			while((row = in.readLine()) != null) {
				currentSize += (row.toCharArray().length * 2 + 1);
				if(currentSize >= size) {
					currentFile++;
					currentSize = 0;
					nextFile = getNextFile(file, currentFile, ext);
					retVal.add(nextFile);
					currentWriter = new PrintWriter(nextFile);
					writers.add(currentWriter);
					
					if(header != null) {
					    for (String h : header) {
					        currentWriter.println(h);
					    }
					    currentSize += headerSize;
					}
				}
				currentWriter.println(row);
			}			
		} finally {
			for(PrintWriter writer : writers) {
				if(writer != null) writer.close();
			}			
		}
		
		return retVal;
	}
	
	/**
	 * Split the file to smaller files, each having the size no more than the given limit.
	 * If copyHeader parameter is true, then the top line from the original file will be copied
     * to all result files.
	 */
    public static List<File> chopBySize(File file, long size, String ext, boolean copyHeader) throws IOException {
        return chopBySize(file, size, ext, copyHeader ? 1 : 0);
    }	

    private static File getNextFile(File inputFile, int counter, String ext) {
        return new File(inputFile.getParentFile(), inputFile.getName().replace("." + ext, "_" + counter + "." + ext));
    }
    
    /**
     * Concatenate text files into one file. This method will make sure that the header, if given,
     * will not be repeated and appear once only on the top of the result file.
     * If headerLines parameter is greater than zero, than the header lines will be written on top
     * of the result file once.  
     */
    public static void concat(List<File> files, File outFile, int headerLines) throws IOException {
        try (PrintWriter out = new PrintWriter(outFile)) {
            boolean headerWritten = false;
            for(File file : files) {
                try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                    if(headerLines > 0) {
                        if(headerWritten) {
                            for (int i = 0; i < headerLines; i++) {
                                in.readLine();
                            }
                        } else {
                            for (int i = 0; i < headerLines; i++) {
                                out.println(in.readLine());
                            }
                            headerWritten = true;
                        }
                    }
                    String s;
                    while((s = in.readLine()) != null) {
                        out.println(s);
                    }
                }
            }
        }
    }
    
    /**
     * Concatenate text files into one file. This method will make sure that the header, if given,
     * will not be repeated and appear once only on the top of the result file.
     * If withHeaders parameter is true, then one line header will be written to the result file once.  
     */    
    public static void concat(List<File> files, File outFile, boolean withHeaders) throws IOException {
        concat (files, outFile, withHeaders ? 1 : 0);
    }    
    
    /**
     * Write to file D (aka difference) rows which exist in file M (aka minuend) but not in file S (aka subtrahend)
     * Essentially it says: M - S = D
     * Strings in files M and S will be compared by using the passed comparator.
     * 
     * The difference file (file D) will have rows sorted with duplicates removed. 
     * 
     * If either parameter is null return immediately.
     * If file M is empty or absent, create empty file D, because the result of subtraction is 0.
     * If file S is empty or absent, the D file will contain all rows from file M (sorted with duplicates removed)
     * 
     * In order to do its job, the method needs to sort the two input files first.  If lines have the same
     * format, than the natural sorting order can be used, but if the lines have different format, the 
     * method would need two additional comparators for M and S files.
     * 
     * @param m             M-file (file, from which we are removing lines)
     * @param s             S-file (file, which contains lines we want to remove from M-file)
     * @param d             D-file (the result file)
     * @param mComparator   Comparator used to sort M-file
     * @param sComparator   Comparator used to sort S-file
     * @param msComparator  Comparator used to compare a line from M-file to a line from S-file
     */
    public static void subtract(Path m, Path s, Path d,
            Comparator<String> mComparator,
            Comparator<String> sComparator,
            Comparator<String> msComparator) throws IOException {
        if(m == null || s == null || d == null) return;
        
        // if M is empty create empty D file (if doesn't exist already)
        if(!Files.exists(m) || Files.size(m) == 0) {
            Files.deleteIfExists(d);
            Files.createFile(d);
            return;
        }
        
        // if S is empty, sort M file and copy it to D file
        if(!Files.exists(s) || Files.size(s) == 0) {
            File mSorted = new FileSorterImpl().sort(m.toFile(), true); //sort with duplicates removed
            Files.move(mSorted.toPath(), d,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        
        // main procedure
        
        // we have to sort files, otherwise removing rows of file S from file M will take quadratic time
        // duplicates removed during sorting
        File mSorted = new FileSorterImpl().sort(m.toFile(), mComparator, true);
        File sSorted = new FileSorterImpl().sort(s.toFile(), sComparator, true);
        
        try (BufferedReader mReader = Files.newBufferedReader(mSorted.toPath());
             BufferedReader sReader = Files.newBufferedReader(sSorted.toPath());
             PrintWriter dWriter = new PrintWriter(d.toFile())) {

            String mLine = mReader.readLine();
            String sLine = sReader.readLine();
            
            boolean mFileDone = false;
            
            // loop until either file M or file S ends
            while(true) {
                if(mLine == null) {
                    mFileDone = true;
                    break; 
                } else if(sLine == null) {
                    break;  
                }

                int compResult = msComparator.compare(mLine, sLine);
                
                // (M-line < S-line) => no such line in S file => write it to the result
                // and advance M-file
                if(compResult < 0) {
                    dWriter.println(mLine);
                    mLine = mReader.readLine();
                // (M-line > S-line) => we don't know weather we should write this line to result
                // but we have to advance S file
                } else if (compResult > 0) {
                    sLine = sReader.readLine();
                // (M-line = S-line) => line M exist in file S => do not write it to the result, and just
                // advance M-file
                } else {
                    mLine = mReader.readLine();
                }
            }
            
            // if S-file is smaller (M file is not done, but S file is), write the rest
            // of the M file to the result
            if(!mFileDone) {
                dWriter.println(mLine);
                String line;
                while((line = mReader.readLine()) != null) dWriter.println(line);
            }
        } finally {
            Files.delete(mSorted.toPath());
            Files.delete(sSorted.toPath());
        }
    }
    
    /**
     * Subtract files with default string comparator. This method compares the lines as they are.
     */
    public static void subtract(Path m, Path s, Path d) throws IOException {
        Comparator<String> c = Comparator.naturalOrder(); 
        subtract(m, s, d, c, c, c);
    }
    
    /**
     * Subtract files with default string comparator. This method assumes that the rows in M and S files
     * are formatted the same way, but uses a custom comparator to match lines in M-file to lines in S-file.
     * 
     * With the custom comparator like that, the rows which are not considered equal under natural order,
     * may be considered equal, and hence removed from the M-file.
     */
    public static void subtract(Path m, Path s, Path d, Comparator<String> msComparator) throws IOException {
        Comparator<String> c = Comparator.naturalOrder(); 
        subtract(m, s, d, c, c, msComparator);
    }

}
