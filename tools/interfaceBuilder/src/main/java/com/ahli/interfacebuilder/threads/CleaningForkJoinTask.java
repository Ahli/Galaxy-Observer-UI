// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.threads;

import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;

import java.io.Serial;
import java.util.concurrent.ForkJoinTask;

/**
 * A task for the ForkJoinPool that allows InterfaceBuilderApp to clean up after itself, if execAndClean() is called
 * instead of the usual exec().
 * <p>
 * exec() should still be overridden with the actual workload.
 */
@Log4j2
public abstract class CleaningForkJoinTask extends ForkJoinTask<Void> {
	
	@Serial
	private static final long serialVersionUID = 8118692371922973864L;
	
	private final transient CleaningForkJoinPool executor;
	
	protected CleaningForkJoinTask(@NonNull final CleaningForkJoinPool executor) {
		this.executor = executor;
	}
	
	@Override
	public Void getRawResult() {
		return null;
	}
	
	@Override
	protected void setRawResult(final Void value) {
	}
	
	/**
	 * Executes the task and causes the InterfacebuilderApp to free up resources.
	 *
	 * @return
	 */
	@Override
	protected boolean exec() {
		try {
			return work();
		} catch (final Exception e) {
			log.error("Error in Task:", e);
			return false;
		} finally {
			StylizedTextAreaAppender.finishedWork(Thread.currentThread().getName(), true, 50);
			executor.tryCleanUp();
		}
	}
	
	/**
	 * CleaningForkJoinTask's workload that is executed.
	 *
	 * @return true, if the task finished normally
	 */
	protected abstract boolean work();
}
