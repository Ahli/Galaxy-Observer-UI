package application.controller;

import application.integration.SettingsIniInterface;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class SettingsGamesPathsController extends SettingsAutoSaveController {
	//	private static final Logger logger = LogManager.getLogger();
	
	@FXML
	private TextField sc2Path;
	@FXML
	private CheckBox sc2Architecture;
	@FXML
	private TextField heroesPath;
	@FXML
	private CheckBox heroesArchitecture;
	@FXML
	private TextField heroesPtrPath;
	@FXML
	private CheckBox heroesPtrArchitecture;
	@FXML
	private Label sc2PathLabel;
	@FXML
	private Label heroesPathLabel;
	@FXML
	private Label heroesPtrPathLabel;
	
	/**
	 * Automatically called by FxmlLoader
	 */
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
		SettingsIniInterface settings = app.getIniSettings();
		heroesPath.setText(settings.getHeroesPath());
		heroesArchitecture.setSelected(settings.isHeroes64bit());
		heroesPtrPath.setText(settings.getHeroesPtrPath());
		heroesPtrArchitecture.setSelected(settings.isHeroesPtr64bit());
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
		
		validatePath(sc2Path.getText(), "SC2Switcher.exe", sc2PathLabel);
		validatePath(heroesPath.getText(), "HeroesSwitcher.exe", heroesPathLabel);
		validatePath(heroesPtrPath.getText(), "HeroesSwitcher.exe", heroesPtrPathLabel);
	}
	
	private void setSc2Path(String path) {
		sc2Path.setText(path);
		app.getIniSettings().setSc2Path(path);
		validatePath(path, "SC2Switcher.exe", sc2PathLabel);
		persistSettingsIni();
	}
	
	private void setHeroesPath(String path) {
		heroesPath.setText(path);
		app.getIniSettings().setHeroesPath(path);
		validatePath(path, "HeroesSwitcher.exe", heroesPathLabel);
		persistSettingsIni();
	}
	
	private void setHeroesPtrPath(String path) {
		heroesPtrPath.setText(path);
		app.getIniSettings().setHeroesPtrPath(path);
		validatePath(path, "HeroesSwitcher.exe", heroesPtrPathLabel);
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
	 * @return whether the path belongs to that game directory or not
	 */
	private boolean validatePath(String path, String switcher, Label invalidLabel) {
		boolean valid = false;
		if (path != null) {
			File f = new File(path);
			valid = f.exists() && f.isDirectory() &&
					new File(path + File.separator + "Support" + File.separator + switcher).exists();
		}
		if (invalidLabel != null) {
			invalidLabel.setVisible(!valid);
		}
		return valid;
	}
	
	@FXML
	public void onSc2ArchitectureChange(ActionEvent event) {
		boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		app.getIniSettings().setSc2Is64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesArchitectureChange(ActionEvent event) {
		boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		app.getIniSettings().setHeroesIs64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesPtrArchitectureChange(ActionEvent event) {
		boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		app.getIniSettings().setHeroesPtrIs64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onSc2PathButtonClick() {
		File selectedFile = showDirectoryChooser("Select StarCraft II's installation directory", sc2Path.getText());
		if (selectedFile != null) {
			String path = selectedFile.getAbsolutePath();
			setSc2Path(path);
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
	private File showDirectoryChooser(String title, String initialPath) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		directoryChooser.setInitialDirectory(new File(initialPath));
		return directoryChooser.showDialog(getWindow());
	}
	
	@FXML
	public void onHeroesPathButtonClick() {
		File selectedFile =
				showDirectoryChooser("Select Heroes of the Storm's installation directory", heroesPath.getText());
		if (selectedFile != null) {
			String path = selectedFile.getAbsolutePath();
			setHeroesPath(path);
		}
	}
	
	@FXML
	public void onHeroesPtrPathButtonClick() {
		File selectedFile = showDirectoryChooser("Select Heroes of the Storm's PTR installation directory",
				heroesPtrPath.getText());
		if (selectedFile != null) {
			String path = selectedFile.getAbsolutePath();
			setHeroesPtrPath(path);
		}
	}
}
