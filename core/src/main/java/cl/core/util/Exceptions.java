package cl.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

/**
 * Utilities related to exception handling.
 */
public final class Exceptions {

    private Exceptions(){}
    
    /**
     * Get a stack trace as a string.
     */
    public static String getStackTrace(Throwable e) {
        Objects.requireNonNull(e);
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Get root cause of a throwable.  The root of a throwable which has no causes is the throwable itself.
     */
    public static Throwable getRootCause(Throwable e) {
        Objects.requireNonNull(e);
        Throwable c = e;
        Throwable cause = c;
        while((c = c.getCause()) != null) {
            cause = c;
        }
        return cause;
    }

    /**
     * Get a stack traces of a throwable's root as a string.
     */
    public static String getRootStackTrace(Throwable e) {
        Objects.requireNonNull(e);
        return getStackTrace(getRootCause(e));
    }
}
