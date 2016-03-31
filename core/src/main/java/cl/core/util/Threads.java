package cl.core.util;

public final class Threads {
    
    private Threads() {}
    
    /**
     * Sleep for given number of milliseconds. While sleeping, the thread may be interrupted.
     */
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
