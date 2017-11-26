package application.util;

/**
 * Tracks Errors.
 * 
 * @author Ahli
 */
public interface ErrorTracker {
	/**
	 * Checks if an error was recently reported.
	 * 
	 * @return whether an error was occurred
	 */
	boolean hasEncounteredError();
	
	/**
	 * Clear the Error.
	 */
	void clearError();
	
	/**
	 * Reports that an error occurred.
	 */
	void reportErrorEncounter();
	
	/**
	 * Reports that an error occurred.
	 *
	 * @param exception
	 *            occurred Exception
	 */
	void reportErrorEncounter(Exception exception);
	
	/**
	 * Returns the Exception reported.
	 * 
	 * @return exception of the report, can be null
	 */
	Exception getErrorException();
}
