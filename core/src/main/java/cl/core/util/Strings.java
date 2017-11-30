package cl.core.util;

import java.util.function.IntPredicate;

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
     * Capitalize the first character of the string. Optionally, bring the rest
     * of the string to lower case.
     * 
     * @param s               input string
     * @param forceLowercase  if true bring the rest of the string to lower case,
     *                        leave original case otherwise
     */
    public static String capitalize(CharSequence s, boolean forceLowercase) {
        if (s == null) return null;
        if (s.length() == 0) return "";
        
        return new StringBuilder()
                .append(Character.toUpperCase(s.charAt(0)))
                .append(forceLowercase ? 
                        s.subSequence(1, s.length()).toString().toLowerCase() :
                        s.subSequence(1, s.length()))
                .toString();
    }
    
    /**
     * Convert a string in so called "snake" case (f.e. this_is_my_snake_case_string)
     * into a string in Camel case.
     * 
     * <p>This method merely removes underscores and capitalizes characters after them.
     * It will not make any changes to other characters' case, nor it will remove
     * spaces.
     */
    public static String snakeToCamel(CharSequence s) {
        return delimitedToCamel(s, ch -> ch == '_', false);
    }
    
    /**
     * Convert a string in Camel case into a string in snake case.
     */
    public static String camelToSnake(CharSequence s) {
        return camelToDelimited(s, '_');
    }
    
    /**
     * Convert a dash-separated string (f.e. this-is-my-dash-separated-string)
     * into a string in Camel case.
     * 
     * <p>This method merely removes dashes and capitalizes characters after them.
     * It will not make any changes to other characters' case, nor it will remove
     * spaces.
     */
    public static String dashedToCamel(CharSequence s) {
        return delimitedToCamel(s, ch -> ch == '-', false);
    }
    
    /**
     * Convert a string in Camel case into a string where words are separated by dashes.
     */
    public static String camelToDashed(CharSequence s) {
        return camelToDelimited(s, '-');
    }
    
    /**
     * Convert a dot-separated string (f.e. this.is.my.dot.separated.string)
     * into a string in Camel case.
     * 
     * <p>This method merely removes dots and capitalizes characters after them.
     * It will not make any changes to other characters' case, nor it will remove
     * spaces.
     */
    public static String dottedToCamel(CharSequence s) {
        return delimitedToCamel(s, ch -> ch == '.', false);
    }
    
    /**
     * Convert a string in Camel case into a string where words are separated by dots.
     */
    public static String camelToDotted(CharSequence s) {
        return camelToDelimited(s, '.');
    }
    
    /**
     * Convert a whitespace-separated string into a string in Camel case.
     * The characters which follow white space characters will be capitalized,
     * while the rest of the characters will be brought to lower case.
     */
    public static String spacedToCamel(CharSequence s) {
        return delimitedToCamel(s, ch -> Character.isWhitespace(ch), true);
    }
    
    /**
     * Convert a string in Camel case into a string where words are separated by spaces.
     */
    public static String camelToSpaced(CharSequence s) {
        return camelToDelimited(s, ' ');
    }
    
    /**
     * Convert a Pascal case string into a Camel case string. It returns
     * the same string as input but with the first character in lower case.
     */
    public static String pascalToCamel(CharSequence s) {
        if (s == null) return null;
        if (s.length() == 0) return "";
        return new StringBuilder()
                .append(Character.toLowerCase(s.charAt(0)))
                .append(s.subSequence(1, s.length()))
                .toString();
    }
    
    private static String delimitedToCamel(
            CharSequence s, IntPredicate delimiterTest, boolean forceLowercase) {
        if (s == null) return null;
        if (s.length() == 0) return "";
        
        StringBuilder sb = new StringBuilder();
        boolean lastWasDelimiter = false;
        for (char ch : s.toString().toCharArray()) {
            if (delimiterTest.test(ch)) {
                lastWasDelimiter = true;
            } else {
                if (lastWasDelimiter) {
                    sb.append(Character.toUpperCase(ch));
                    lastWasDelimiter = false;
                } else {
                    sb.append(forceLowercase ? Character.toLowerCase(ch) : ch);
                }
            }
        }
        return sb.toString();
    }
    
    private static String camelToDelimited(CharSequence s, char delimiter) {
        if (s == null) return null;
        if (s.length() == 0) return "";
        
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toString().toCharArray()) {
            if (Character.isUpperCase(ch)) {
                sb.append(delimiter);
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
}