package com.ahli.hotkeyUi.application.controller;

import com.ahli.hotkeyUi.application.Main;
import com.ahli.hotkeyUi.application.i18n.Messages;
import com.ahli.hotkeyUi.application.ui.Alerts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * 
 * @author Ahli
 *
 */
public class MenuBarController {
	// static Logger LOGGER = LogManager.getLogger(MenuBarController.class);
	// //$NON-NLS-1$
	
	private Main main;
	
	@FXML
	private MenuItem menuOpen;
	@FXML
	private MenuItem menuSave;
	@FXML
	private MenuItem menuSaveAs;
	@FXML
	private MenuItem menuClose;
	
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
	 * 
	 * @param main
	 */
	public void setMainApp(final Main main) {
		this.main = main;
	}
	
	/**
	 * Called when the user clicks on the delete button.
	 */
	@FXML
	public void close(final ActionEvent event) {
		main.closeFile();
	}
	
	/**
	 * Called when the user clicks on the open button.
	 */
	@FXML
	public void open(final ActionEvent event) {
		main.openUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void saveAs(final ActionEvent event) {
		main.saveAsUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void saveCurrent(final ActionEvent event) {
		main.saveUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save as button.
	 */
	@FXML
	public void exit(final ActionEvent event) {
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
		final String content = String.format(Messages.getString("MenuBarController.AboutText"), Main.VERSION) + "\n\n" //$NON-NLS-2$
				+ Messages.getString("MenuBarController.AboutText2");
		final String title = Messages.getString("MenuBarController.About"); //$NON-NLS-1$
		final String header = Messages.getString("MenuBarController.ObserverUISettingsEditor"); //$NON-NLS-1$
		final String imgUrl = Main.class.getResource("/res/ahliLogo.png").toString(); //$NON-NLS-1$
		final Alert alert = Alerts.buildAboutAlert(main.getPrimaryStage(), title, header, content, imgUrl);
		alert.showAndWait();
	}
	
}
