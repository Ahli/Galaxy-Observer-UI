// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.controllers;

import com.ahli.hotkey_ui.application.SettingsEditorApplication;
import com.ahli.hotkey_ui.application.i18n.Messages;
import com.ahli.hotkey_ui.application.integration.RecentlyUsed;
import com.ahli.hotkey_ui.application.ui.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ahli
 */
@Slf4j
public class MenuBarController {
	private final RecentlyUsed recentlyUsed = new RecentlyUsed();
	private SettingsEditorApplication main;
	@FXML
	private MenuItem menuOpen;
	@FXML
	private MenuItem menuSave;
	@FXML
	private MenuItem menuSaveAs;
	@FXML
	private MenuItem menuClose;
	@FXML
	private Menu menuRecent;
	
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
		menuRecent.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		for (final Path recent : recentlyUsed.getRecent()) {
			final MenuItem menuItem = new MenuItem(recent.toString());
			menuItem.setOnAction(event -> {
				if (Files.exists(recent)) {
					main.openMpqFileThreaded(recent);
					recentlyUsed.addRecent(recent);
				} else {
					Alerts.buildWarningAlert(main.getPrimaryStage().getOwner(),
							Messages.getString("Main.warningAlertTitle"),
							Messages.getString("Main.fileNotFound"),
							"");
				}
			});
			menuRecent.getItems().add(menuItem);
		}
	}
	
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
		final File file = main.openUiMpq();
		if (file != null) {
			recentlyUsed.addRecent(file.toPath());
			final MenuItem menuItem = new MenuItem(file.getAbsolutePath());
			menuItem.setOnAction(event -> main.openMpqFileThreaded(file.toPath()));
			menuRecent.getItems().add(menuItem);
		}
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
	
	public void updateMenuBar() {
		final boolean docNotLoaded = main.isInvalidOpenedDocPath();
		
		menuSave.setDisable(docNotLoaded);
		menuSaveAs.setDisable(docNotLoaded);
		menuClose.setDisable(docNotLoaded);
	}
	
	@FXML
	public void aboutClicked() {
		final String content =
				String.format(Messages.getString("MenuBarController.AboutText"), SettingsEditorApplication.VERSION) +
						"\n\n" + Messages.getString("MenuBarController.AboutText2");
		final String title = Messages.getString("MenuBarController.About");
		final String header = Messages.getString("MenuBarController.ObserverUISettingsEditor");
		
		final String imgUrl;
		final URL resource = SettingsEditorApplication.class.getResource("/res/ahliLogo.png");
		if (resource != null) {
			imgUrl = resource.toString();
		} else {
			log.error("Error loading resource. Resource not found");
			imgUrl = "/ahliLogo.png";
		}
		
		final Alert alert = Alerts.buildAboutAlert(main.getPrimaryStage(), title, header, content, imgUrl);
		alert.showAndWait();
	}
	
}
