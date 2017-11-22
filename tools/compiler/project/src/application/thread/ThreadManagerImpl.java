package application.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages Threads. They are tracked via a tag and returnable by them.
 * 
 * @author Ahli
 *
 */
public class ThreadManagerImpl implements ThreadManager {
	HashMap<Integer, Thread> idToThread = new HashMap<>();
	HashMap<Thread, Integer> threadToId = new HashMap<>();
	HashMap<Thread, String> threadToTag = new HashMap<>();
	int nextFreeThreadId = 0;
	
	@Override
	public int registerThread(final Thread thread, final String tag) {
		final int threadId = getUnusedThreadId();
		idToThread.put(threadId, thread);
		threadToId.put(thread, threadId);
		threadToTag.put(thread, getInternalTag(threadId, tag));
		return threadId;
	}
	
	@Override
	public Set<Thread> getThreadsByTag(final String tag) {
		final Set<Thread> set = new HashSet<>();
		final String regex = getInternalTagValidationRegex(tag);
		// arraylist clones key set to fix java.util.ConcurrentModificationException
		for (final Thread thread : new ArrayList<>(threadToId.keySet())) {
			if (thread.getName().matches(regex)) {
				set.add(thread);
			}
		}
		return set;
	}
	
	@Override
	public Set<Thread> getAllThreads() {
		return threadToId.keySet();
	}
	
	@Override
	public boolean unregisterThread(final Thread thread) {
		final Integer id = threadToId.get(thread);
		if (id != null) {
			threadToId.remove(thread);
			idToThread.remove(id);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns an unused ID.
	 * 
	 * @return
	 */
	private int getUnusedThreadId() {
		return nextFreeThreadId++;
	}
	
	/**
	 * Returns the internal tag constructed by the .
	 * 
	 * @param threadId
	 * @param tag
	 * @return
	 */
	private String getInternalTag(final int threadId, final String tag) {
		return tag + "_" + threadId;
	}
	
	/**
	 * Returns a regex that validates the specified tag.
	 * 
	 * @param tag
	 *            the tag String
	 * @return regex to validate these tags
	 */
	private String getInternalTagValidationRegex(final String tag) {
		return "^" + tag + "_[0-9]+$";
	}
	
}
