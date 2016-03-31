package cl.ugly.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class facilitates giving application threads custom names, when past to
 * executors service.
 * 
 * For example:
 * 
 * Executors.newFixedThreadPool(1, new NamedThreadFactory("dedupers-refresher"));
 */
public class NamedThreadFactory implements ThreadFactory {

    private static AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * Whenever a new thread gets created by executor service, it will be given a name
     * with the given prefix and a counter as a suffix.
     */
    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, namePrefix + "-" + threadNumber.getAndIncrement());
    }

}
