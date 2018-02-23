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
	private final boolean noResultIcon;
	private boolean encounteredError = false;
	private Tab tab;
	private StyleClassedTextArea textArea;
	private boolean running = false;
	private boolean encounteredWarning;
	private byte state = 0;
	
	/**
	 * Constructor
	 *
	 * @param tab
	 * @param textArea
	 * @param colorizeTitle
	 * @param noResultIcon
	 */
	public ErrorTabController(final Tab tab, final StyleClassedTextArea textArea, final boolean colorizeTitle,
			final boolean noResultIcon) {
		this.tab = tab;
		this.textArea = textArea;
		this.colorizeTitle = colorizeTitle;
		this.noResultIcon = noResultIcon;
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
		if (!running) {
			if (noResultIcon) {
				if (state != 0) {
					tab.setGraphic(null);
					state = 0;
				}
			} else {
				if (!encounteredError && !encounteredWarning) {
					if (state != 1) {
						final FontAwesomeIconView checkedIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
						checkedIcon.setFill(Color.LAWNGREEN);
						tab.setGraphic(checkedIcon);
						state = 1;
					}
				} else {
					if (encounteredError) {
						if (state != 2) {
							final FontAwesomeIconView errorIcon =
									new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
							errorIcon.setFill(Color.RED);
							tab.setGraphic(errorIcon);
							state = 2;
						}
					} else {
						if (state != 3) {
							final FontAwesomeIconView warningIcon =
									new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
							warningIcon.setFill(Color.YELLOW);
							tab.setGraphic(warningIcon);
							state = 3;
						}
					}
				}
			}
		} else {
			final FontAwesomeIconView loadingIcon = new FontAwesomeIconView(FontAwesomeIcon.SPINNER);
			loadingIcon.setFill(Color.GAINSBORO);
			tab.setGraphic(loadingIcon);
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
}
