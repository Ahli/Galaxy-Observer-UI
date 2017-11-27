package application.util;

import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.scene.control.Tab;

/**
 * Tracks occurrence of an error.
 * 
 * @author Ahli
 */
public final class ErrorTabPaneController implements ErrorTracker {
	private boolean encounteredError = false;
	private Exception exception = null;
	private Tab tab = null;
	private StyleClassedTextArea textArea = null;
	private boolean running = false;
	
	/**
	 * Constructor
	 * 
	 * @param tabPane
	 */
	public ErrorTabPaneController(final Tab tab, final StyleClassedTextArea textArea) {
		this.tab = tab;
		this.textArea = textArea;
	}
	
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
	public void reportErrorEncounter(final Exception exception) {
		this.exception = exception;
	}
	
	@Override
	public Exception getErrorException() {
		return exception;
	}
	
	/**
	 * @return the tab
	 */
	public Tab getTab() {
		return tab;
	}
	
	/**
	 * @param tab
	 *            the tab to set
	 */
	public void setTabPane(final Tab tab) {
		this.tab = tab;
	}
	
	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(final boolean running) {
		this.running = running;
	}
	
	/**
	 * @return
	 */
	public StyleClassedTextArea getTextArea() {
		return textArea;
	}
	
	/**
	 * @param textArea
	 */
	public void setTextArea(final StyleClassedTextArea textArea) {
		this.textArea = textArea;
	}
	
}
