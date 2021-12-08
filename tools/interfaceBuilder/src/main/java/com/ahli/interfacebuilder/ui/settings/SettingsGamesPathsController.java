// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.settings;

import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class SettingsGamesPathsController extends SettingsAutoSaveController {
	public static final String HEROES_SWITCHER_EXE = "HeroesSwitcher.exe";
	public static final String SC2_SWITCHER_EXE = "SC2Switcher.exe";
	@FXML
	private TextField sc2Path;
	@FXML
	private CheckBox sc2Architecture;
	@FXML
	private TextField heroesPath;
	@FXML
	private TextField heroesPtrPath;
	@FXML
	private Label sc2PathLabel;
	@FXML
	private Label heroesPathLabel;
	@FXML
	private Label heroesPtrPathLabel;
	@Autowired
	private FileService fileService;
	
	public SettingsGamesPathsController(final ConfigService configService) {
		super(configService);
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		super.initialize();
		sc2PathLabel.setTextFill(Color.YELLOW);
		heroesPathLabel.setTextFill(Color.YELLOW);
		heroesPtrPathLabel.setTextFill(Color.YELLOW);
		FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
		icon.setFill(Color.YELLOW);
		sc2PathLabel.setGraphic(icon);
		icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
		icon.setFill(Color.YELLOW);
		heroesPathLabel.setGraphic(icon);
		icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
		icon.setFill(Color.YELLOW);
		heroesPtrPathLabel.setGraphic(icon);
	}
	
	@Override
	public void update() {
		super.update();
		
		// load values from ini
		final SettingsIniInterface settings = configService.getIniSettings();
		heroesPath.setText(settings.getHeroesPath());
		heroesPtrPath.setText(settings.getHeroesPtrPath());
		sc2Path.setText(settings.getSc2Path());
		sc2Architecture.setSelected(settings.isSc64bit());
		
		// change listener for editable text fields
		sc2Path.textProperty().addListener((observable, oldVal, newVal) -> {
			if (!oldVal.equals(newVal)) {
				setSc2Path(newVal);
			}
		});
		heroesPath.textProperty().addListener((observable, oldVal, newVal) -> {
			if (!oldVal.equals(newVal)) {
				setHeroesPath(newVal);
			}
		});
		heroesPtrPath.textProperty().addListener((observable, oldVal, newVal) -> {
			if (!oldVal.equals(newVal)) {
				setHeroesPtrPath(newVal);
			}
		});
		
		validatePath(sc2Path.getText(), SC2_SWITCHER_EXE, sc2PathLabel);
		validatePath(heroesPath.getText(), HEROES_SWITCHER_EXE, heroesPathLabel);
		validatePath(heroesPtrPath.getText(), HEROES_SWITCHER_EXE, heroesPtrPathLabel);
	}
	
	@FXML
	public void onSc2ArchitectureChange(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setSc2Is64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onSc2PathButtonClick() {
		final File selectedFile =
				showDirectoryChooser("Select StarCraft II's installation directory", sc2Path.getText());
		if (selectedFile != null) {
			setSc2Path(selectedFile.getAbsolutePath());
		}
	}
	
	/**
	 * Shows a new directory selection dialog. The method doesn't return until the displayed dialog is dismissed. The
	 * return value specifies the directory chosen by the user or null if no selection has been made. If the owner
	 * window for the directory selection dialog is set, input to all windows in the dialog's owner chain is blocked
	 * while the dialog is being shown.
	 *
	 * @param title
	 * @param initialPath
	 * @return the selected directory or null if no directory has been selected
	 */
	private File showDirectoryChooser(final String title, final String initialPath) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		final File f = fileService.cutTillValidDirectory(initialPath);
		if (f != null) {
			directoryChooser.setInitialDirectory(f);
		}
		return directoryChooser.showDialog(getWindow());
	}
	
	private void setSc2Path(final String path) {
		sc2Path.setText(path);
		configService.getIniSettings().setSc2Path(path);
		validatePath(path, SC2_SWITCHER_EXE, sc2PathLabel);
		persistSettingsIni();
	}
	
	/**
	 * Validates a path leading to a game directory and adjusts the UI's error label.
	 *
	 * @param path
	 * 		installation path to validate
	 * @param switcher
	 * 		name of the Switcher.exe file found in the "Support" folder
	 * @param invalidLabel
	 * 		label set visible if invalid, can be null
	 */
	private static void validatePath(final String path, final String switcher, final Label invalidLabel) {
		@SuppressWarnings("BooleanVariableAlwaysNegated")
		boolean valid = false;
		if (path != null) {
			valid = switcherExists(path, switcher, false) ||
					switcherExists(path, switcher.replace(".exe", "_x64.exe"), true);
		}
		if (invalidLabel != null) {
			invalidLabel.setVisible(!valid);
		}
	}
	
	/**
	 * @param gameDirectoryPath
	 * @param switcherName
	 * @param is64bit
	 * @return
	 */
	private static boolean switcherExists(
			final String gameDirectoryPath, final String switcherName, final boolean is64bit) {
		return new File(gameDirectoryPath + File.separator + "Support" + (is64bit ? "64" : "") + File.separator +
				switcherName).exists();
	}
	
	@FXML
	public void onHeroesPathButtonClick() {
		final File selectedFile =
				showDirectoryChooser("Select Heroes of the Storm's installation directory", heroesPath.getText());
		if (selectedFile != null) {
			setHeroesPath(selectedFile.getAbsolutePath());
		}
	}
	
	private void setHeroesPath(final String path) {
		heroesPath.setText(path);
		configService.getIniSettings().setHeroesPath(path);
		validatePath(path, HEROES_SWITCHER_EXE, heroesPathLabel);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesPtrPathButtonClick() {
		final File selectedFile = showDirectoryChooser("Select Heroes of the Storm's PTR installation directory",
				heroesPtrPath.getText());
		if (selectedFile != null) {
			setHeroesPtrPath(selectedFile.getAbsolutePath());
		}
	}
	
	private void setHeroesPtrPath(final String path) {
		heroesPtrPath.setText(path);
		configService.getIniSettings().setHeroesPtrPath(path);
		validatePath(path, HEROES_SWITCHER_EXE, heroesPtrPathLabel);
		persistSettingsIni();
	}
}
