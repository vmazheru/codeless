package cl.ugly.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cl.ugly.util.FileUtils;

/**
 * External sorting implementation of FileSorter.
 * This class generates small sorted files in the directory as [input_file]/sortinprocess,
 * and then merges them into one sorted file. The temporary directory gets deleted when done.
 * The original file remains in place.
 */
public final class FileSorterImpl implements FileSorter {
	
	private static final int NUM_ROWS_PER_FILE = 10000;
	private static final String IN_PROCESS_DIR = "sortinprocess";
	private static final String SORTED_FILE_SUFFIX = "_sorted";

	@Override
	public File sort(File file) throws IOException {
		return sort(file, defaultComparator(), false, 0);
	}
	
	@Override
	public File sort(File file, int numHeaderLines) throws IOException {
	    return sort(file, defaultComparator(), false, numHeaderLines);
	}
	
	@Override
	public File sort(File file, Comparator<String> comparator) throws IOException {
		return sort(file, comparator, false, 0);
	}
	
    @Override
    public File sort(File file, Comparator<String> comparator, int numHeaderLines) throws IOException {
        return sort(file, comparator, false, numHeaderLines);
    }	

	@Override
	public File sort(File file, boolean removeDuplicates) throws IOException {
	    return sort(file, defaultComparator(), removeDuplicates, 0);
	}
	
    @Override
    public File sort(File file, boolean removeDuplicates, int numHeaderLines) throws IOException {
        return sort(file, defaultComparator(), removeDuplicates, numHeaderLines);
    }

	@Override
	public File sort(File file, Comparator<String> comparator, boolean removeDuplicates) throws IOException {
	    return sort(file, comparator, removeDuplicates, 0);
	}

	/**
     * Sorts a file in two steps:
     * 1) Create smaller (small enough to fit in memory) sorted files.
     * 2) Merge these files into one resulting file (multi-way merge)
     */
    @Override
    public File sort(File file, Comparator<String> comparator, boolean removeDuplicates, int numHeaderLines) throws IOException {
        File sortedFile = new File(file.getParentFile(), file.getName() + SORTED_FILE_SUFFIX);
        List<File> inprocessFiles = null;
        BufferedReader[] readers = null;
        try {
            SplitResult r = splitIntoSortedFiles(file, NUM_ROWS_PER_FILE, comparator, numHeaderLines);
            inprocessFiles = r.getFiles();
            String[] header = r.getHeader();
            readers = openReaders(inprocessFiles);
            mergeFiles(readers, sortedFile, comparator, removeDuplicates, header);
        } catch (IOException e) {
            if(sortedFile.exists()) sortedFile.delete();
            throw e;
        } finally {
            closeReaders(readers);
            deleteFiles(inprocessFiles);
        }
        return sortedFile;        
    }

    private static Comparator<String> defaultComparator() {
        return new Comparator<String>() {
            @Override public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        };
    }
    
    private static class SplitResult {
        private final List<File> files;
        private final String[] header;
        SplitResult(String[]header, List<File> files) {
            this.header = header;
            this.files = files;
        }
        List<File> getFiles() { return files; }
        String[] getHeader()  { return header; }
    }
	
	private static SplitResult splitIntoSortedFiles(
	        File file, int rowFileLimit, Comparator<String> comparator, int numHeaderLines)
			throws IOException {
		List<File> inprocessFiles = new ArrayList<>();
		String[] header = null;
		try (BufferedReader in = new BufferedReader(new FileReader(file));) {
		    header = readHeader(in, numHeaderLines);
			List<String> lines = new ArrayList<>(rowFileLimit);
			
			int rowCount = 0;
			int fileCount = 0;
			String s;
			while((s = in.readLine()) != null) {
				lines.add(s);
				if(++rowCount == rowFileLimit) {
					dumpNextInproceessFile(file, fileCount++, inprocessFiles, lines, comparator);
					rowCount = 0;
					lines.clear();
				}
			}
			
			if(!lines.isEmpty()) {
				dumpNextInproceessFile(file, fileCount++, inprocessFiles, lines, comparator);
			}
		}
		
		return new SplitResult(header, inprocessFiles);
	}
	
	private static String[] readHeader(BufferedReader in, int numHeaderLines) throws IOException {
	    if (numHeaderLines > 0) {
	        String[] header = new String[numHeaderLines];
	        for (int i = 0; i < numHeaderLines; i++) {
	            header[i] = in.readLine();
	        }
	        return header;
	    }
	    return null;
	}
	
	private static void writeHeader(PrintWriter out, String[] header) {
	    if (header != null) {
	        for (int i = 0; i < header.length; i++) {
	            out.println(header[i]);
	        }
	    }
	}
	
	private static void dumpNextInproceessFile(
			File file, int fileCount, List<File> inprocessFiles, List<String> lines, Comparator<String> comparator) 
		throws IOException {
		Collections.<String>sort(lines, comparator);
		File inprocessFile = getNextInprocessFile(file, fileCount+1);
		FileUtils.writeLines(inprocessFile, lines);
		inprocessFiles.add(inprocessFile);
	}
	
	/*
	 * This method maintains array of "pending" lines, one line per file.  Initially all lines are nulls, and the readers
	 * will read one line each and put them into the array.
	 * 
	 * Next, the minimum line is chosen, dumped into the result file, and the array entry for this line (reader) is set to null, 
	 * so on the next iteration the corresponding reader will read the next line from the its file.
	 * 
	 * Whenever a reader has no more lines to read, it is closed, and set to null.
	 * 
	 * The process stops when no more readers exist in the array (all nulls).
	 */
	private static void mergeFiles(BufferedReader[] readers, File sortedFile,
			Comparator<String> comparator, boolean removeDuplicates, String[] header) throws IOException {
		if(readers != null) {
			String[] lines = new String[readers.length];
			
			try (PrintWriter out = new PrintWriter(sortedFile)) {
			    writeHeader(out, header);
			    
				String prevLine = null; //all lines after this one will be skipped if equal on removeDuplicates

				while(hasOpenReaders(readers)) {
					for(int i = 0; i < readers.length; i++) {
						BufferedReader reader = readers[i];
						if(reader != null) {
							if(lines[i] == null) {
								String line = reader.readLine();								
								lines[i] = line;
								if(line == null) {
									closeReader(reader);
									readers[i] = null;
								}
							}
						}
					}
					
					String minLine = null;
					int minLineIndex = -1;
					for(int i = 0; i < lines.length; i++) {
						String line = lines[i];
						if(line != null) {
							if(minLine == null || comparator.compare(line, minLine) < 0) {
								minLine = line;
								minLineIndex = i;
							}
						}
					}
					
					if(minLine != null) {
						lines[minLineIndex] = null;
						
						if(removeDuplicates) {
							if(!minLine.equals(prevLine)) {
								prevLine = minLine;
								out.println(minLine);
							}
						} else {
							out.println(minLine);
						}
					}
				}
			}
		}
	}
	
	private static File getNextInprocessFile(File file, int nextFileNum) {
		return new File(getInprocessDir(file), file.getName() + "_" + nextFileNum);
	}
	
	private static File getInprocessDir(File file) {
		File inprocessDir = new File(file.getParentFile(), IN_PROCESS_DIR);
		inprocessDir.mkdirs();
		return inprocessDir;
	}
	
	private static void deleteFiles(List<File> files) {
		if(files == null) return;
		File dir = null;
		for(File f : files) {
			if(dir == null) dir = f.getParentFile();
			f.delete();
		}
		if(dir != null) dir.delete();
	}
	
	private static BufferedReader[] openReaders(List<File> files) throws IOException {
		BufferedReader[] readers = new BufferedReader[files.size()];
		for(int i = 0; i < files.size(); i++) {
			readers[i] = new BufferedReader(new FileReader(files.get(i)));
		}
		return readers;
	}
	
	private static void closeReaders(Reader[] readers) {
		if(readers == null) return;
		for(Reader reader : readers) {
			closeReader(reader);
		}
	}
	
	private static void closeReader(Reader reader) {
		if(reader != null) try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
	}
	
	private static boolean hasOpenReaders(Reader[] readers) {
		for(Reader r : readers) {
			if(r != null) return true;
		}
		return false;
	}
	
}

