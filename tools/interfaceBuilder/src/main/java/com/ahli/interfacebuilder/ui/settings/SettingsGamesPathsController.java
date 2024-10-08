// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.settings;

import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.i18n.Messages;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsGamesPathsController extends SettingsAutoSaveController {
	private final FileService fileService;
	private final GameService gameService;
	
	@FXML
	private TextField sc2Path;
	@FXML
	private TextField sc2PtrPath;
	@FXML
	private CheckBox sc2Architecture;
	@FXML
	private CheckBox sc2PtrArchitecture;
	@FXML
	private TextField heroesPath;
	@FXML
	private TextField heroesPtrPath;
	@FXML
	private Label sc2PathLabel;
	@FXML
	private Label sc2PtrPathLabel;
	@FXML
	private Label heroesPathLabel;
	@FXML
	private Label heroesPtrPathLabel;
	
	public SettingsGamesPathsController(
			final ConfigService configService, final FileService fileService, final GameService gameService) {
		super(configService);
		this.fileService = fileService;
		this.gameService = gameService;
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
		sc2PtrPath.setText(settings.getSc2PtrPath());
		sc2Architecture.setSelected(settings.isSc2X64());
		sc2PtrArchitecture.setSelected(settings.isSc2PtrX64());
		
		// change listener for editable text fields
		sc2Path.textProperty().addListener((observable, oldVal, newVal) -> {
			if (!oldVal.equals(newVal)) {
				setSc2Path(newVal);
			}
		});
		sc2PtrPath.textProperty().addListener((observable, oldVal, newVal) -> {
			if (!oldVal.equals(newVal)) {
				setSc2PtrPath(newVal);
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
		
		String heroesSwitcher64 = gameService.getGameDefHeroes().switcherExeNameX64();
		validatePath(heroesPath.getText(), heroesSwitcher64, null, heroesPathLabel);
		validatePath(heroesPtrPath.getText(), heroesSwitcher64, null, heroesPtrPathLabel);
		
		String sc2switcher64 = gameService.getGameDefSc2().switcherExeNameX64();
		String sc2switcher32 = gameService.getGameDefSc2().switcherExeNameX32();
		validatePath(sc2Path.getText(), sc2switcher64, sc2switcher32, sc2PathLabel);
		validatePath(sc2PtrPath.getText(), sc2switcher64, sc2switcher32, sc2PtrPathLabel);
	}
	
	@FXML
	public void onSc2ArchitectureChange(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setSc2X64(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onSc2PtrArchitectureChange(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setSc2PtrX64(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onSc2PathButtonClick() {
		final File selectedFile =
				showDirectoryChooser(Messages.getString("selectGamePath.sc2"), Path.of(sc2Path.getText()));
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
	private File showDirectoryChooser(final String title, final Path initialPath) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		final Path path = fileService.cutTillValidDirectory(initialPath);
		if (path != null) {
			directoryChooser.setInitialDirectory(path.toFile());
		}
		return directoryChooser.showDialog(getWindow());
	}
	
	private void setSc2Path(final String path) {
		sc2Path.setText(path);
		configService.getIniSettings().setSc2Path(path);
		String sc2switcher64 = gameService.getGameDefSc2().switcherExeNameX64();
		String sc2switcher32 = gameService.getGameDefSc2().switcherExeNameX32();
		validatePath(path, sc2switcher64, sc2switcher32, sc2PathLabel);
		persistSettingsIni();
	}
	
	/**
	 * Validates a path leading to a game directory and adjusts the UI's error label.
	 *
	 * @param path
	 * 		installation path to validate
	 * @param switcher64
	 * 		name of the 64bit Switcher.exe file found in the "Support64" folder
	 * @param switcher32
	 * 		name of the 32bit Switcher.exe file found in the "Support" folder
	 * @param invalidLabel
	 * 		label set visible if invalid, can be null
	 */
	private static void validatePath(
			final String path, final String switcher64, final String switcher32, final Label invalidLabel) {
		@SuppressWarnings("BooleanVariableAlwaysNegated")
		boolean valid = false;
		if (path != null) {
			valid = switcherExists(path, switcher64, true) || switcherExists(path, switcher32, false);
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
		return Files.exists(Path.of(
				gameDirectoryPath + File.separator + "Support" + (is64bit ? "64" : "") + File.separator +
						switcherName));
	}
	
	@FXML
	public void onHeroesPathButtonClick() {
		final File selectedFile =
				showDirectoryChooser(Messages.getString("selectGamePath.heroes"), Path.of(heroesPath.getText()));
		if (selectedFile != null) {
			setHeroesPath(selectedFile.getAbsolutePath());
		}
	}
	
	private void setHeroesPath(final String path) {
		heroesPath.setText(path);
		configService.getIniSettings().setHeroesPath(path);
		String heroesSwitcher64 = gameService.getGameDefHeroes().switcherExeNameX64();
		validatePath(path, heroesSwitcher64, null, heroesPathLabel);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesPtrPathButtonClick() {
		final File selectedFile =
				showDirectoryChooser(Messages.getString("selectGamePath.heroesPtr"), Path.of(heroesPtrPath.getText()));
		if (selectedFile != null) {
			setHeroesPtrPath(selectedFile.getAbsolutePath());
		}
	}
	
	private void setHeroesPtrPath(final String path) {
		heroesPtrPath.setText(path);
		configService.getIniSettings().setHeroesPtrPath(path);
		String heroesSwitcher64 = gameService.getGameDefHeroes().switcherExeNameX64();
		validatePath(path, heroesSwitcher64, null, heroesPtrPathLabel);
		persistSettingsIni();
	}
	
	@FXML
	public void onSc2PtrPathButtonClick() {
		final File selectedFile =
				showDirectoryChooser(Messages.getString("selectGamePath.sc2Ptr"), Path.of(sc2PtrPath.getText()));
		if (selectedFile != null) {
			setSc2PtrPath(selectedFile.getAbsolutePath());
		}
	}
	
	private void setSc2PtrPath(final String path) {
		sc2PtrPath.setText(path);
		configService.getIniSettings().setSc2PtrPath(path);
		String sc2switcher64 = gameService.getGameDefSc2().switcherExeNameX64();
		String sc2switcher32 = gameService.getGameDefSc2().switcherExeNameX32();
		validatePath(path, sc2switcher64, sc2switcher32, sc2PtrPathLabel);
		persistSettingsIni();
	}
}
