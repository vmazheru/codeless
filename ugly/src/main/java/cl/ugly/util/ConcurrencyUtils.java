package cl.ugly.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities to ease running tasks in parallel.
 */
public final class ConcurrencyUtils {
	
	private ConcurrencyUtils(){}
	
	/**
	 * Run tasks in a pool of threads of size tasks.length
	 */
	public static void run(Runnable[] tasks) throws ExecutionException, InterruptedException {
		run(tasks, tasks.length);
	}

	/**
	 * Run tasks in a pool of threads of the specified size
	 */
	public static void run(final Runnable[] tasks, int numThreads)
			throws ExecutionException, InterruptedException {
		
		class RunnableCallable implements Callable<Void> {
			final Runnable task;
			RunnableCallable(Runnable task) {
				this.task = task;
			}
			@Override public Void call() throws Exception {
				task.run();
				return null;
			}
		}
		
		RunnableCallable[] cTasks = new RunnableCallable[tasks.length];
		for(int i = 0; i < tasks.length; i++) {
			cTasks[i] = new RunnableCallable(tasks[i]);
		}
		
		run(cTasks, numThreads);
	}

	/**
	 * Run tasks in a pool of threads of size tasks.length
	 */
	public static <T> List<T> run(List<Callable<T>> tasks)
			throws ExecutionException, InterruptedException {
		@SuppressWarnings("unchecked")
		Callable<T>[] arr = tasks.toArray(new Callable[tasks.size()]);
		return run(arr, tasks.size());
	}

	/**
	 * Run tasks in a pool of threads of size tasks.length
	 */
	public static <T> List<T> run(Callable<T>[] tasks)
			throws ExecutionException, InterruptedException {
		return run(tasks, tasks.length);
	}

	/**
	 * Run tasks in a pool of threads of the specified size
	 */
	public static <T> List<T> run(Callable<T>[] tasks, int maxThreads)
			throws ExecutionException, InterruptedException {
	    int numThreads = Math.min(tasks.length, maxThreads);
		ExecutorService executors = null;
		try {
			List<Future<T>> futures = new ArrayList<>(tasks.length);
			List<T> result = new ArrayList<>(tasks.length);
			executors = Executors.newFixedThreadPool(numThreads);
			for(Callable<T> task : tasks) {
				futures.add(executors.submit(task));
			}

			for(Future<T> f : futures) {
				result.add(f.get());
			}
			
			return result;
		} finally {
			if(executors != null) {
				executors.shutdown();
			}
		}
	}
}
