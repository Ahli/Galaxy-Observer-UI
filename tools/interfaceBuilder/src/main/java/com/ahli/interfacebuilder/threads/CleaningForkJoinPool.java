// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.threads;

import java.io.Serial;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class CleaningForkJoinPool extends ForkJoinPool {
	
	private final CleaningForkJoinTaskCleaner cleaner;
	
	public CleaningForkJoinPool(
			final int parallelism,
			final ForkJoinWorkerThreadFactory factory,
			final Thread.UncaughtExceptionHandler handler,
			final boolean asyncMode,
			final int corePoolSize,
			final int maximumPoolSize,
			final int minimumRunnable,
			final Predicate<? super ForkJoinPool> saturate,
			final long keepAliveTime,
			final TimeUnit unit,
			final CleaningForkJoinTaskCleaner cleaner) {
		super(
				parallelism,
				factory,
				handler,
				asyncMode,
				corePoolSize,
				maximumPoolSize,
				minimumRunnable,
				saturate,
				keepAliveTime,
				unit);
		this.cleaner = cleaner;
	}
	
	@Override
	public void execute(final Runnable task) {
		if (task == null) {
			throw new NullPointerException(null);
		}
		if (task instanceof CleaningForkJoinTask) {
			// is already a CleaningForkJoinTask
			super.execute(task);
		} else {
			// wrap into CleaningForkJoinTask
			execute(new TaskStarter(cleaner, task));
		}
	}
	
	private static final class TaskStarter extends CleaningForkJoinTask {
		
		@Serial
		private static final long serialVersionUID = 5128277301331174458L;
		
		private final transient Runnable task;
		
		private TaskStarter(final CleaningForkJoinTaskCleaner cleaner, final Runnable task) {
			super(cleaner);
			this.task = task;
		}
		
		@Override
		protected boolean work() {
			task.run();
			return true;
		}
	}
}
