package cl.ugly.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {
    
    private ExceptionUtils(){}
    
    /**
     * Return the root cause of the exception.
     */
    public static Throwable getRootCause(Throwable e) {
        if(e == null) return null;
        Throwable c = e;
        Throwable cause = c;
        while((c = c.getCause()) != null) {
            cause = c;
        }
        return cause;
    }
    
    /**
     * Return exception stack trace as string, where each new line
     * in the stack trace goes to the next line.
     */
    public static String getStackTrace(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return result.toString();
    }
}
