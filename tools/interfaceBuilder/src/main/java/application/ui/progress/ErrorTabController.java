package application.ui.progress;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Tracks occurrence of an error.
 *
 * @author Ahli
 */
public final class ErrorTabController {
	private static final String TAB_TEXT_COLOR_YELLOW = "-tab-text-color: yellow;";
	private static final String TAB_TEXT_COLOR_RED = "-tab-text-color: red;";
	private final boolean colorizeTitle;
	private final boolean showResultIcon;
	private boolean encounteredError;
	private Tab tab;
	private StyleClassedTextArea textArea;
	private boolean running;
	private boolean encounteredWarning;
	private State state = State.NOT_STARTED;
	private boolean errorsDoNotPreventExit;
	
	/**
	 * Constructor
	 *
	 * @param tab
	 * @param textArea
	 * @param colorizeTitle
	 * @param noResultIcon
	 */
	public ErrorTabController(final Tab tab, final StyleClassedTextArea textArea, final boolean colorizeTitle,
			final boolean noResultIcon, final boolean errorsDoNotPreventExit) {
		this.tab = tab;
		this.textArea = textArea;
		this.colorizeTitle = colorizeTitle;
		showResultIcon = !noResultIcon;
		this.errorsDoNotPreventExit = errorsDoNotPreventExit;
	}
	
	/**
	 * @return
	 */
	public boolean hasEncounteredError() {
		return encounteredError;
	}
	
	/**
	 * @return
	 */
	public boolean hasEncounteredWarning() {
		return encounteredWarning;
	}
	
	public void clearError() {
		encounteredError = false;
	}
	
	/**
	 * @return the tab
	 */
	public Tab getTab() {
		return tab;
	}
	
	/**
	 * @param tab
	 * 		the tab to set
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
	 * 		the running to set
	 */
	public void setRunning(final boolean running) {
		this.running = running;
		updateIcon();
	}
	
	/**
	 *
	 */
	private void updateIcon() {
		if (running) {
			setIconAppearance(State.RUNNING, FontAwesomeIcon.SPINNER, Color.GAINSBORO);
		} else if (encounteredError) {
			setIconAppearance(State.ERROR, FontAwesomeIcon.EXCLAMATION_TRIANGLE, Color.RED);
		} else if (encounteredWarning) {
			setIconAppearance(State.WARNING, FontAwesomeIcon.EXCLAMATION_TRIANGLE, Color.YELLOW);
		} else {
			setIconAppearance(State.GOOD, FontAwesomeIcon.CHECK, Color.LAWNGREEN);
		}
	}
	
	private void setIconAppearance(final State newState, final FontAwesomeIcon icon, final Color color) {
		if (!state.equals(newState)) {
			state = newState;
			if (newState == State.RUNNING || newState == State.NOT_STARTED || showResultIcon) {
				final FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
				iconView.setFill(color);
				tab.setGraphic(iconView);
			} else {
				tab.setGraphic(null);
			}
		}
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
	
	/**
	 *
	 */
	public void reportError() {
		if (!encounteredError) {
			encounteredError = true;
			if (colorizeTitle) {
				tab.setStyle(TAB_TEXT_COLOR_RED);
			}
			updateIcon();
		}
	}
	
	/**
	 *
	 */
	public void reportWarning() {
		if (!encounteredWarning) {
			encounteredWarning = true;
			if (colorizeTitle && !encounteredError) {
				tab.setStyle(TAB_TEXT_COLOR_YELLOW);
			}
			updateIcon();
		}
	}
	
	public boolean isErrorsDoNotPreventExit() {
		return errorsDoNotPreventExit;
	}
	
	public void setErrorsDoNotPreventExit(final boolean errorsDoNotPreventExit) {
		this.errorsDoNotPreventExit = errorsDoNotPreventExit;
	}
	
	private enum State {NOT_STARTED, RUNNING, WARNING, ERROR, GOOD}
}
