package application.util;

/**
 * Tracks Errors.
 * 
 * @author Ahli
 *
 */
public interface ErrorTracker {
	/**
	 * Checks if an error was recently reported.
	 * 
	 * @return
	 */
	public boolean hasEncounteredError();
	
	/**
	 * Clear the Error.
	 */
	public void clearError();
	
	/**
	 * Reports that an error occurred.
	 */
	public void reportErrorEncounter();
	
	/**
	 * Reports that an error occurred.
	 *
	 * @param exception
	 *            occurred Exception
	 */
	public void reportErrorEncounter(Exception exception);
	
	/**
	 * Returns the Exception reported.
	 * 
	 * @return
	 */
	public Exception getErrorException();
}
