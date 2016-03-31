package cl.core.util;

/**
 * String utilities.
 */
public final class Strings {
    
    private Strings() {}

    /**
     * Capitalize the first character of the string. The rest of the string will hold it's original case.
     * Return null if the input is null.
     */
    public static String capitalize(CharSequence s) {
        return capitalize(s, false);
    }
    
    /**
     * Capitalize the first character in the string and force the rest of the string to lower case.
     * Return null if the input is null.
     */
    public static String toTitleCase(CharSequence s) {
        return capitalize(s, true);
    }
    
    private static String capitalize(CharSequence s, boolean forceLowercase) {
        if (s == null)       return null;
        if (s.length() == 0) return ""  ;
        
        return new StringBuilder()
                .append(Character.toUpperCase(s.charAt(0)))
                .append(forceLowercase ? 
                        s.subSequence(1, s.length()).toString().toLowerCase() :
                        s.subSequence(1, s.length()))
                .toString();
    }
    
}