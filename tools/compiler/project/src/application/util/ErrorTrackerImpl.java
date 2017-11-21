package application.util;

/**
 * Tracks occurrence of an error.
 * 
 * @author Ahli
 *
 */
public class ErrorTrackerImpl implements ErrorTracker {
	private boolean encounteredError = false;
	private Exception exception = null;
	
	@Override
	public boolean hasEncounteredError() {
		return encounteredError;
	}
	
	@Override
	public void reportErrorEncounter() {
		encounteredError = true;
		exception = null;
	}
	
	@Override
	public void clearError() {
		encounteredError = false;
		exception = null;
	}
	
	@Override
	public void reportErrorEncounter(Exception exception) {
		this.exception = exception;
	}
	
	@Override
	public Exception getErrorException() {
		return exception;
	}
	
}
