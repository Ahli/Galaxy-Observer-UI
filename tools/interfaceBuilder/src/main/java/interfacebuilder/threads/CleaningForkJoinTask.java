package interfacebuilder.threads;

import interfacebuilder.InterfaceBuilderApp;

import java.util.concurrent.ForkJoinTask;

/**
 * A task for the ForkJoinPool that allows InterfaceBuilderApp to clean up after itself, if execAndClean() is called
 * instead of the usual exec().
 * <p>
 * exec() should still be overridden with the actual workload.
 *
 * @param <V>
 */
public abstract class CleaningForkJoinTask<V> extends ForkJoinTask<V> {
	// TODO maybe make this more generic, so this can be re-used -> define interface & call member of that to clean up
	
	@Override
	public V getRawResult() {
		return null;
	}
	
	@Override
	protected void setRawResult(final V value) {
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
		} finally {
			InterfaceBuilderApp.tryCleanUp();
		}
	}
	
	/**
	 * CleaningForkJoinTask's workload that is executed.
	 *
	 * @return
	 */
	protected abstract boolean work();
}
