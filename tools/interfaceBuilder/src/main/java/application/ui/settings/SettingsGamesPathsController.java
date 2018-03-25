package application.ui.settings;

import application.integration.FileService;
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
	@Autowired
	private FileService fileService;
	
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
		
		validatePath(sc2Path.getText(), SC2_SWITCHER_EXE, sc2PathLabel);
		validatePath(heroesPath.getText(), HEROES_SWITCHER_EXE, heroesPathLabel);
		validatePath(heroesPtrPath.getText(), HEROES_SWITCHER_EXE, heroesPtrPathLabel);
	}
	
	private void setSc2Path(final String path) {
		sc2Path.setText(path);
		configService.getIniSettings().setSc2Path(path);
		validatePath(path, SC2_SWITCHER_EXE, sc2PathLabel);
		persistSettingsIni();
	}
	
	private void setHeroesPath(final String path) {
		heroesPath.setText(path);
		configService.getIniSettings().setHeroesPath(path);
		validatePath(path, HEROES_SWITCHER_EXE, heroesPathLabel);
		persistSettingsIni();
	}
	
	private void setHeroesPtrPath(final String path) {
		heroesPtrPath.setText(path);
		configService.getIniSettings().setHeroesPtrPath(path);
		validatePath(path, HEROES_SWITCHER_EXE, heroesPtrPathLabel);
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
	private boolean validatePath(final String path, final String switcher, final Label invalidLabel) {
		boolean valid = false;
		if (path != null) {
			final File f = new File(path);
			valid = f.exists() && f.isDirectory() &&
					new File(path + File.separator + "Support" + File.separator + switcher).exists();
		}
		if (invalidLabel != null) {
			invalidLabel.setVisible(!valid);
		}
		return valid;
	}
	
	@FXML
	public void onSc2ArchitectureChange(final ActionEvent event) {
		final boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setSc2Is64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesArchitectureChange(final ActionEvent event) {
		final boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setHeroesIs64Bit(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesPtrArchitectureChange(final ActionEvent event) {
		final boolean val = ((CheckBox) event.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setHeroesPtrIs64Bit(val);
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
	
	@FXML
	public void onHeroesPathButtonClick() {
		final File selectedFile =
				showDirectoryChooser("Select Heroes of the Storm's installation directory", heroesPath.getText());
		if (selectedFile != null) {
			setHeroesPath(selectedFile.getAbsolutePath());
		}
	}
	
	@FXML
	public void onHeroesPtrPathButtonClick() {
		final File selectedFile = showDirectoryChooser("Select Heroes of the Storm's PTR installation directory",
				heroesPtrPath.getText());
		if (selectedFile != null) {
			setHeroesPtrPath(selectedFile.getAbsolutePath());
		}
	}
}
