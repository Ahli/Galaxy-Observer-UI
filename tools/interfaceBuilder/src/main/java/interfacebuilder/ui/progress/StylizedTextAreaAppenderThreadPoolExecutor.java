package interfacebuilder.ui.progress;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A StylizedTextAreaAppenderThreadPoolExecutor is an executor that messages StylizedTextAreaAppenders that the thread
 * finished running. Each runnable executed alters the name of the thread, so the StylizedTextAreaAppender can find out
 * which Thread created which log message.
 */
public class StylizedTextAreaAppenderThreadPoolExecutor extends ThreadPoolExecutor {
	
	public StylizedTextAreaAppenderThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
			final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
			final ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}
	
	@Override
	protected void beforeExecute(final Thread t, final Runnable r) {
		super.beforeExecute(t, r);
		// altering the thread name allows the logging to use the correct controller
		// -> log message is always put into the correct ID
		t.setName(t.getId() + "_" + r.hashCode());
	}
	
	@Override
	protected void afterExecute(final Runnable r, final Throwable t) {
		super.afterExecute(r, t);
		StylizedTextAreaAppender.finishedWork(Thread.currentThread().getName());
	}
	
}