package com.ahli.hotkeyUi.application.controller;

import com.ahli.hotkeyUi.application.Main;
import com.ahli.hotkeyUi.application.i18n.Messages;
import com.ahli.hotkeyUi.application.ui.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ahli
 */
public class MenuBarController {
	private static final Logger logger = LogManager.getLogger();
	
	private Main main;
	
	@FXML
	private MenuItem menuOpen;
	@FXML
	private MenuItem menuSave;
	@FXML
	private MenuItem menuSaveAs;
	@FXML
	private MenuItem menuClose;
	
	public MenuBarController() {
		// nothing to do
	}
	
	/**
	 * Automatically called on Controller initialization.
	 */
	public void initialize() {
		menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		menuSaveAs.setAccelerator(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
		menuClose.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
	}
	
	/**
	 * @param main
	 */
	public void setMainApp(final Main main) {
		this.main = main;
	}
	
	/**
	 * Called when the user clicks on the delete button.
	 */
	@FXML
	public void close() {
		main.closeFile();
	}
	
	/**
	 * Called when the user clicks on the open button.
	 */
	@FXML
	public void open() {
		main.openUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void saveAs() {
		main.saveAsUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void saveCurrent() {
		main.saveUiMpqThreaded();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void exit() {
		main.closeApp();
	}
	
	/**
	 * Updates the menu bar's items.
	 */
	public void updateMenuBar() {
		final boolean docLoaded = main.isValidOpenedDocPath();
		
		menuSave.setDisable(!docLoaded);
		menuSaveAs.setDisable(!docLoaded);
		menuClose.setDisable(!docLoaded);
	}
	
	/**
	 * clicked on About
	 */
	@FXML
	public void aboutClicked() {
		final String content =
				String.format(Messages.getString("MenuBarController.AboutText"), Main.VERSION) + "\n" + "\n" +
						Messages.getString("MenuBarController.AboutText2");
		final String title = Messages.getString("MenuBarController.About");
		final String header = Messages.getString("MenuBarController.ObserverUISettingsEditor");
		String imgUrl;
		try {
			imgUrl = Main.class.getResource("/res/ahliLogo.png").toString();
		} catch (final NullPointerException e) {
			logger.error("Error loading resource");
			imgUrl = "ahliLogo.png";
		}
		
		final Alert alert = Alerts.buildAboutAlert(main.getPrimaryStage(), title, header, content, imgUrl);
		alert.showAndWait();
	}
	
}
