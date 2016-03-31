package cl.ugly.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtils {

    private CryptoUtils(){}

    /**
     * Calculate a string hash using a given crypto algorithm like MD%, or SHA-1 for file content 
     */
    public static String getFileHash(File file, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return getHash(fileContent, algorithm);
    }
    
    /**
     * Calculate a string hash using a given crypto algorithm for byte array.
     */
    public static String getHash(byte[] content, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest d = MessageDigest.getInstance(algorithm);
        byte[] digest = d.digest(content);
        return toHex(digest);
    }

    /*
     * Convert byte array to hexadecimal string.
     */
    private static String toHex(byte hash[]){
        StringBuilder buf = new StringBuilder(hash.length * 2);
        for (int i = 0; i < hash.length; i++){
            int intVal = hash[i] & 0xff;
            if (intVal < 0x10){
                // append a zero before a one digit hex
                // number to make it two digits.
                buf.append("0");
            }
            buf.append(Integer.toHexString(intVal));
        }
        return buf.toString();
    }
 
}
