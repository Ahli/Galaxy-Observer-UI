// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.controllers;

import com.ahli.hotkey_ui.application.SettingsEditorApplication;
import com.ahli.hotkey_ui.application.i18n.Messages;
import com.ahli.hotkey_ui.application.ui.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author Ahli
 */
public class MenuBarController {
	private static final Logger logger = LoggerFactory.getLogger(MenuBarController.class);
	
	private SettingsEditorApplication main;
	
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
		menuSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S,
				KeyCombination.CONTROL_DOWN,
				KeyCombination.ALT_DOWN));
		menuClose.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
	}
	
	/**
	 * @param main
	 */
	public void setMainApp(final SettingsEditorApplication main) {
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
	 * Called when the user clicks on the save-as button.
	 */
	@FXML
	public void saveAs() {
		main.saveAsUiMpq();
	}
	
	/**
	 * Called when the user clicks on the save button.
	 */
	@FXML
	public void saveCurrent() {
		main.saveUiMpqThreaded();
	}
	
	/**
	 * Called when the user clicks on the exit button.
	 */
	@FXML
	public void exit() {
		main.closeApp();
	}
	
	/**
	 * Updates the menu bar's items.
	 */
	public void updateMenuBar() {
		final boolean docNotLoaded = main.isInvalidOpenedDocPath();
		
		menuSave.setDisable(docNotLoaded);
		menuSaveAs.setDisable(docNotLoaded);
		menuClose.setDisable(docNotLoaded);
	}
	
	/**
	 * clicked on About
	 */
	@FXML
	public void aboutClicked() {
		final String content =
				String.format(Messages.getString("MenuBarController.AboutText"), SettingsEditorApplication.VERSION) +
						"\n" + "\n" + Messages.getString("MenuBarController.AboutText2");
		final String title = Messages.getString("MenuBarController.About");
		final String header = Messages.getString("MenuBarController.ObserverUISettingsEditor");
		
		final String imgUrl;
		final URL resource = SettingsEditorApplication.class.getResource("/res/ahliLogo.png");
		if (resource != null) {
			imgUrl = resource.toString();
		} else {
			logger.error("Error loading resource. Resource not found");
			imgUrl = "/ahliLogo.png";
		}
		
		final Alert alert = Alerts.buildAboutAlert(main.getPrimaryStage(), title, header, content, imgUrl);
		alert.showAndWait();
	}
	
}
