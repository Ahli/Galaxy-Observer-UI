// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.threads;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * The default ForkJoinPoolThreadFactory is not using Spring's classloader. Thus, it won't find the dependencies in the
 * jar. E.g. Apache commons-configuration2 requires this.
 */
public class SpringForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
	
	public SpringForkJoinWorkerThreadFactory() {
		// explicit default constructor
	}
	
	@Override
	public final ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
		return new MyForkJoinWorkerThread(pool);
	}
	
	private static final class MyForkJoinWorkerThread extends ForkJoinWorkerThread {
		
		private MyForkJoinWorkerThread(final ForkJoinPool pool) {
			super(pool);
			// set the correct classloader to work with Spring
			setContextClassLoader(Thread.currentThread().getContextClassLoader());
		}
	}
}
