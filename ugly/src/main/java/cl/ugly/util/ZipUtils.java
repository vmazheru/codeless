package cl.ugly.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utilities to manipulate zip/jar archives
 */
public final class ZipUtils {

    private ZipUtils(){}
    
    /**
     * Extract all entries from the zip file into a directory with the same name
     */
    public static File extractHere(File zipFile) throws IOException {
        File destination = new File(zipFile.getParentFile(), zipFile.getName().replace(".zip", ""));
        extract(zipFile, destination);
        return destination;
    }
    
    /**
     * Extract all entries to the given destination directory
     */
    public static void extract(File zipFile, File destDirectory) throws IOException {
        if(!zipFile.exists()) throw new FileNotFoundException("File not found: " + zipFile.getAbsolutePath());
        
        destDirectory.mkdirs();
        
        try(ZipFile zFile = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                File destDir = e.isDirectory() ? new File(destDirectory, e.getName()) : destDirectory;
                if(e.isDirectory()) destDir.mkdirs();
                else {
                    File dest = new File(destDir, e.getName());
                    try(InputStream in = zFile.getInputStream(e);
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
                        int i;
                        while((i = in.read()) != -1) out.write(i);
                    }
                }
            }
        }
    }

    /**
     * Extract a given zip entry from a zip file to the output file.
     * If an entry is not found by name, the output file will not be created.
     */
    public static void extractEntryToFile(File zipFile, String fileName, File outputFile) throws IOException {
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            extractEntry(zipFile, fileName, out);
        }
        if(outputFile.exists() && outputFile.length() == 0) outputFile.delete();
    }
    
    /**
     * Extract a given zip entry form a zip file to the byte array.
     * Return null if the entry is not found in the zip file.
     */
    public static byte[] extractEntry(File zipFile, String fileName) throws IOException {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            extractEntry(zipFile, fileName, out);
            byte[] result = out.toByteArray(); 
            return result.length != 0 ? result : null;
        }
    }
    
    /*
     * Extract entry to the output stream. If entry does not exists the output stream will be open,
     * but no data will be written to it. 
     */
    private static void extractEntry(File zipFile, String fileName, OutputStream out) throws IOException {
        try(ZipFile zFile = new ZipFile(zipFile)) {
            ZipEntry zEntry = zFile.getEntry(fileName);
            if(zEntry != null) {
                try(InputStream in = new BufferedInputStream(zFile.getInputStream(zEntry))) {
                    int i;
                    while((i = in.read()) != -1) out.write(i);
                }
            }
        }
    }
    
    
}
