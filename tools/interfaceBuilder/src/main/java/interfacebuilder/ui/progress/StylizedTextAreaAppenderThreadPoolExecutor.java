// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

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
	private Runnable cleanUpTask;
	
	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial parameters and {@linkplain
	 * ThreadPoolExecutor.AbortPolicy default rejected execution handler}.
	 *
	 * @param corePoolSize
	 * 		the number of threads to keep in the pool, even if they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize
	 * 		the maximum number of threads to allow in the pool
	 * @param keepAliveTime
	 * 		when the number of threads is greater than the core, this is the maximum time that excess idle threads will
	 * 		wait for new tasks before terminating.
	 * @param unit
	 * 		the time unit for the {@code keepAliveTime} argument
	 * @param workQueue
	 * 		the queue to use for holding tasks before they are executed.  This queue will hold only the {@code Runnable}
	 * 		tasks submitted by the {@code execute} method.
	 * @param threadFactory
	 * 		the factory to use when the executor creates a new thread
	 * @throws IllegalArgumentException
	 * 		if one of the following holds:<br> {@code corePoolSize < 0}<br> {@code keepAliveTime < 0}<br> {@code
	 * 		maximumPoolSize <= 0}<br> {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException
	 * 		if {@code workQueue} or {@code threadFactory} is null
	 */
	public StylizedTextAreaAppenderThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
			final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
			final ThreadFactory threadFactory, final Runnable cleanUpTask) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		this.cleanUpTask = cleanUpTask;
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
		
		if (cleanUpTask != null && r != cleanUpTask) {
			final int count = getActiveCount() - 1; // subtract this task
			if (count <= 0) {
				execute(cleanUpTask);
			}
		}
	}
	
	public void setCleanUpTask(final Runnable cleanUpTask) {
		this.cleanUpTask = cleanUpTask;
	}
	
}