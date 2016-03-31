package cl.ugly.util;

public final class StringUtils2 {
    
    private StringUtils2(){}

    /**
     * Return binary string for a number
     */
    public static String toBinaryString(long value, int numDigits) {
        StringBuilder sb = new StringBuilder();
        for(int i = numDigits-1; i >= 0; i--) {
            sb.append(((value & (1 << i)) != 0) ? "1" :"0");
        }
        return sb.toString();
    }
    
    private static char[] BAD_CHARS = "-,.?!'\":;()%$*[]{}+=_\\/`~@#^&|<>".toCharArray();

    /**
     * Remove special characters (all other than letters, digits, and spaces) from the string.
     */
    public static String replaceSpecialCharacters(String keyword, String replacement) {
        StringBuilder sb = new StringBuilder();
        char[] orig = keyword.toCharArray();
        outer: for(int i = 0; i < orig.length; i++) {
            char origCh = orig[i];
            for(int j = 0; j < BAD_CHARS.length; j++) {
               if(origCh == BAD_CHARS[j]) {
                   sb.append(replacement);
                   continue outer;
               }
            }
            sb.append(origCh);
        }
        return sb.toString();
    }

    /**
     * Normalize the string (that is replace sequences of spaces with one space).
     * New lines and tabs will be replaced, too.
     */
    public static String normalize(String source) {
        char[] chars = source.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(int i = 0; i < chars.length; i++) {
            if(Character.isWhitespace(chars[i])){
                if(isFirst) {
                    isFirst = false;
                    sb.append(' ');
                    continue;
                }
                continue;
            }
            isFirst = true;
            sb.append(chars[i]);
        }
        return sb.toString();
    }
    
    /**
     * If the string is null, return null, if it's empty, return null, too. Otherwise return trimmed string
     */
    public static String trimToNull(String source) {
        if(source == null) return null;
        String trimmed = source.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
