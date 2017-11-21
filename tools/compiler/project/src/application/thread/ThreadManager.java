package application.thread;

import java.util.Set;

/**
 * ThreadManager manages Threads. They are tracked via a tag and returnable by
 * them.
 * 
 * @author Ahli
 *
 */
public interface ThreadManager {
	
	/**
	 * Register a Thread with a tag.
	 * 
	 * @param thread
	 *            Thread to register
	 * @param tag
	 *            String the thread will be associated and retrievable with
	 * @return Integer that resembles a unique ID for this Thread
	 */
	int registerThread(Thread thread, String tag);
	
	/**
	 * Returns Threads using the specified tag.
	 * 
	 * @param tag
	 * @return threads of tag
	 */
	Set<Thread> getThreadsByTag(String tag);
	
	/**
	 * Returns all Threads.
	 * 
	 * @return
	 */
	Set<Thread> getAllThreads();
	
	/**
	 * Removes the Thread from this Manager.
	 * 
	 * @param thread
	 *            Thread to remove
	 * @return whether the thread was contained or not
	 */
	boolean unregisterThread(Thread thread);
	
}
