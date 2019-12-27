package interfacebuilder.threads;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * The default ForkJoinPoolThreadFactory is not using Spring's classloader. Thus, it won't find the dependencies in the
 * jar. E.g. Apache commons-configuration2 requires this.
 */
public class SpringForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
	
	@Override
	public final ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
		return new MyForkJoinWorkerThread(pool);
	}
	
	private static class MyForkJoinWorkerThread extends ForkJoinWorkerThread {
		
		private MyForkJoinWorkerThread(final ForkJoinPool pool) {
			super(pool);
			// set the correct classloader
			setContextClassLoader(Thread.currentThread().getContextClassLoader());
		}
	}
}
