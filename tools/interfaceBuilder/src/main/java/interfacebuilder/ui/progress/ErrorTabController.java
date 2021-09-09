// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;

/**
 * Tracks occurrence of an error.
 *
 * @author Ahli
 */
public final class ErrorTabController {
	private static final String TAB_TEXT_COLOR_YELLOW = "-tab-text-color: yellow;";
	private static final String TAB_TEXT_COLOR_RED = "-tab-text-color: red;";
	private static final String TAB_TEXT_COLOR_WHITE = "-tab-text-color: white;";
	private final boolean colorizeTitle;
	private final boolean showResultIcon;
	private final Tab tab;
	private final TextFlow textArea;
	private boolean encounteredError;
	private boolean running;
	private boolean encounteredWarning;
	private State state = State.NOT_STARTED;
	private boolean errorPreventsExit;
	
	/**
	 * @param tab
	 * @param textArea
	 * @param colorizeTitle
	 * @param noResultIcon
	 */
	public ErrorTabController(
			final Tab tab,
			final TextFlow textArea,
			final boolean colorizeTitle,
			final boolean noResultIcon,
			final boolean errorPreventsExit) {
		this.tab = tab;
		this.textArea = textArea;
		this.colorizeTitle = colorizeTitle;
		showResultIcon = !noResultIcon;
		this.errorPreventsExit = errorPreventsExit;
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
	
	public void clearError(final boolean updateTabHeader) {
		encounteredError = false;
		if (updateTabHeader) {
			updateIcon();
			updateLabel();
		}
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
	
	private void updateLabel() {
		if (!encounteredError && !encounteredWarning) {
			tab.setStyle(TAB_TEXT_COLOR_WHITE);
		}
	}
	
	private void setIconAppearance(final State newState, final FontAwesomeIcon icon, final Color color) {
		if (state != newState) {
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
	
	public void clearWarning(final boolean updateTabHeader) {
		encounteredWarning = false;
		if (updateTabHeader) {
			updateIcon();
			updateLabel();
		}
	}
	
	/**
	 * @return the tab
	 */
	public Tab getTab() {
		return tab;
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
	 * @return
	 */
	public TextFlow getTextArea() {
		return textArea;
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
	
	public boolean isErrorPreventsExit() {
		return errorPreventsExit;
	}
	
	public void setErrorPreventsExit(final boolean errorPreventsExit) {
		this.errorPreventsExit = errorPreventsExit;
	}
	
	private enum State {NOT_STARTED, RUNNING, WARNING, ERROR, GOOD}
}
