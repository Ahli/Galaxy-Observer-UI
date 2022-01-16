// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application;

import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndex;
import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.hotkey_ui.application.controllers.MenuBarController;
import com.ahli.hotkey_ui.application.controllers.TabsController;
import com.ahli.hotkey_ui.application.galaxy.ext.LayoutExtensionReader;
import com.ahli.hotkey_ui.application.i18n.Messages;
import com.ahli.hotkey_ui.application.integration.FileListingVisitor;
import com.ahli.hotkey_ui.application.integration.JarHelper;
import com.ahli.hotkey_ui.application.model.ValueDef;
import com.ahli.hotkey_ui.application.ui.Alerts;
import com.ahli.hotkey_ui.application.ui.ShowToUserException;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Application
 *
 * @author Ahli
 */
public class SettingsEditorApplication extends Application {
	public static final String VERSION = "alpha";
	public static final String STORM_INTERFACE_FILE_FILTER = "*.StormInterface";
	public static final String SC2_INTERFACE_FILE_FILTER = "*.SC2Interface";
	
	private static final Logger logger = LoggerFactory.getLogger(SettingsEditorApplication.class);
	
	private final long appStartTime = System.nanoTime();
	private Stage primaryStage;
	private BorderPane rootLayout;
	private MenuBarController mbarCtrl;
	private TabsController tabsCtrl;
	private Path openedDocPath;
	private MpqEditorInterface mpqi;
	private DescIndex descIndex;
	private Path basePath;
	private boolean hasUnsavedFileChanges;
	private LayoutExtensionReader layoutExtReader;
	
	/**
	 * Entry point of Application.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void main(final String[] args) {
		logger.trace("trace log visible");
		logger.debug("debug log visible");
		logger.info("info log visible");
		logger.warn("warn log visible");
		logger.error("error log visible");
		
		logger.trace("Configuration File of System: {}", System.getProperty("log4j.configurationFile"));
		
		// TEST Locale
		//		Messages.setBundle(Locale.CHINA);
		//		com.ahli.mpq.i18n.Messages.setBundle(Locale.CHINA);
		
		launch(args);
	}
	
	/**
	 * Initialize the UI Application.
	 */
	@Override
	public void start(final Stage primaryStage) {
		try {
			Thread.currentThread().setName("UI");
			logger.trace("start function called after {}ms.", (System.nanoTime() - appStartTime) / 1_000_000);
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			this.primaryStage = primaryStage;
			primaryStage.setMaximized(true);
			primaryStage.setOpacity(0);
			
			setUserAgentStylesheet(STYLESHEET_MODENA);
			
			initAppIcon();
			
			final long time = System.nanoTime();
			initRootLayout();
			logger.trace("initialized root layout within {}ms.", (System.nanoTime() - time) / 1_000_000);
			
			// Load Tab layout from fxml file
			final long time2 = System.nanoTime();
			final FXMLLoader loader = new FXMLLoader();
			loader.setResources(Messages.getBundle());
			
			final TabPane tabPane;
			try (final InputStream is = SettingsEditorApplication.class.getResourceAsStream("/view/TabsLayout.fxml")) {
				tabPane = loader.load(is);
			}
			
			logger.trace("initialized tab layout within {}ms.", (System.nanoTime() - time2) / 1_000_000);
			rootLayout.setCenter(tabPane);
			tabsCtrl = loader.getController();
			
			// ask to save on close
			primaryStage.setOnCloseRequest(event -> {
				final boolean cancelled = !askToSaveUnsavedChanges();
				if (cancelled) {
					// cancel closing
					event.consume();
				}
			});
			
			initPaths();
			
			// Fade animation
			final FadeTransition ft = new FadeTransition(Duration.millis(750), rootLayout);
			ft.setFromValue(0);
			ft.setToValue(1.0);
			ft.play();
			
			final long time3 = System.nanoTime();
			primaryStage.show();
			primaryStage.setOpacity(1);
			logger.trace("executed root layout stage.show() within {}ms", (System.nanoTime() - time3) / 1_000_000);
			
			// hide apps splash screen image
			Platform.runLater(new SplashScreenHider());
			
			logger.trace("finished app initialization after {}ms", (System.nanoTime() - appStartTime) / 1_000_000);
			
			initMpqInterface();
			
		} catch (final Exception e) {
			logger.error("App Error", e);
			this.primaryStage.setOpacity(1);
			Alerts.buildExceptionAlert(this.primaryStage, e).showAndWait();
			closeApp();
		}
	}
	
	private void initAppIcon() {
		// if it fails to load the resource in as a jar, check the eclipse settings
		try {
			primaryStage.getIcons()
					.add(new Image(SettingsEditorApplication.class.getResourceAsStream("/res/ahliLogo.png")));
		} catch (final NullPointerException e) {
			logger.error("Error loading resource", e);
			primaryStage.getIcons().add(new Image("ahliLogo.png"));
		}
	}
	
	/**
	 * Initializes the root layout.
	 *
	 * @throws IOException
	 */
	public void initRootLayout() throws IOException {
		// Load root layout from fxml file.
		final long time = System.nanoTime();
		final FXMLLoader loader = new FXMLLoader();
		loader.setResources(Messages.getBundle());
		
		try (final InputStream is = SettingsEditorApplication.class.getResourceAsStream("/view/RootLayout.fxml")) {
			rootLayout = loader.load(is);
		}
		logger.trace("initialized root layout fxml within {}ms.", (System.nanoTime() - time) / 1_000_000);
		
		// get Controller
		final long time2 = System.nanoTime();
		mbarCtrl = loader.getController();
		mbarCtrl.setMainApp(this);
		logger.trace("received root layout controller within {}ms.", (System.nanoTime() - time2) / 1_000_000);
		
		// Show the scene containing the root layout.
		final Scene scene = new Scene(rootLayout);
		final long time3 = System.nanoTime();
		scene.getStylesheets()
				.add(SettingsEditorApplication.class.getResource("/view/application.css").toExternalForm());
		
		if (logger.isTraceEnabled()) {
			logger.trace(
					"installed font families: {}\nLocale dflt is '{}'\nLocale of Messages.class is '{}'\nLocale china: {}",
					Font.getFamilies(),
					Locale.getDefault(),
					Messages.getBundle().getLocale(),
					Locale.SIMPLIFIED_CHINESE);
		}
		if (Messages.checkIfTargetResourceIsUsed(Locale.CHINA)) {
			logger.trace("apply Chinese css");
			scene.getStylesheets().add(SettingsEditorApplication.class.getResource("/i18n/china.css").toExternalForm());
		}
		logger.trace("initialized root layout css within {}ms.", (System.nanoTime() - time3) / 1_000_000);
		
		final long time4 = System.nanoTime();
		primaryStage.setTitle(Messages.getString("Main.observerUiSettingsEditorTitle"));
		primaryStage.setScene(scene);
		logger.trace("executed root layout setScene+title within {}ms.", (System.nanoTime() - time4) / 1_000_000);
		
		final long time5 = System.nanoTime();
		updateMenuBar();
		logger.trace("updateMenuBar within {}ms.", (System.nanoTime() - time5) / 1_000_000);
	}
	
	/**
	 * Asks the user to decide on unsaved changes, if there are any.
	 *
	 * @return false if the user pressed cancel, else true
	 */
	public boolean askToSaveUnsavedChanges() {
		if (hasUnsavedFileChanges()) {
			// ask to save changes in the file
			final String title = Messages.getString("Main.unsavedChangesTitle");
			final String content = String.format(Messages.getString("Main.hasUnsavedChanges"), openedDocPath);
			
			final Alert alert = Alerts.buildYesNoCancelAlert(primaryStage, title, title, content);
			
			final Optional<ButtonType> result = alert.showAndWait();
			
			if (result.isPresent()) {
				if (result.get() == ButtonType.YES) {
					saveUiMpq();
				} else {
					return result.get() != ButtonType.CANCEL;
				}
			}
		}
		return true;
	}
	
	/**
	 * Initialize Paths
	 */
	private void initPaths() {
		basePath = JarHelper.getJarDir(SettingsEditorApplication.class);
	}
	
	/**
	 * Initializes the MPQ Editor Interface.
	 */
	private void initMpqInterface() {
		final String tempDirectory = System.getProperty("java.io.tmpdir");
		final Path cachePath = Path.of(tempDirectory, "ObserverUiSettingsEditor", "_ExtractedMpq");
		final Path mpqEditorPath =
				basePath.resolve("plugins" + File.separator + "mpq" + File.separator + "MPQEditor.exe");
		mpqi = new MpqEditorInterface(cachePath, mpqEditorPath);
		if (!Files.isExecutable(mpqEditorPath)) {
			logger.error("Could not find MPQEditor.exe within its expected path: {}", mpqEditorPath);
			final String title = Messages.getString("Main.warningAlertTitle");
			final String content = String.format(Messages.getString("Main.couldNotFindMpqEditor"), mpqEditorPath);
			final Alert alert = Alerts.buildWarningAlert(primaryStage, title, title, content);
			alert.showAndWait();
		}
		descIndex = new DescIndex(mpqi);
	}
	
	/**
	 * Causes the App to shut down. This includes asking to save unsaved changes.
	 */
	public void closeApp() {
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}
	
	/**
	 * Update the menu bar in the main window. E.g. this needs to be done after opening/closing a document to
	 * enable/disable the save buttons.
	 */
	private void updateMenuBar() {
		Platform.runLater(() -> getMbarCtrl().updateMenuBar());
	}
	
	/**
	 * Whether the opened file was changed and the app notified about it or not.
	 *
	 * @return
	 */
	public boolean hasUnsavedFileChanges() {
		return hasUnsavedFileChanges;
	}
	
	/**
	 * Saves the currently opened document.
	 */
	public void saveUiMpq() {
		final long time = System.nanoTime();
		
		// cannot save, if not valid
		if (isInvalidOpenedDocPath()) {
			return;
		}
		
		try {
			if (compile()) {
				mpqi.buildMpq(openedDocPath, false, MpqEditorCompression.BLIZZARD_SC2_HEROES, false);
			}
			hasUnsavedFileChanges = false;
			updateAppTitle();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException | ParserConfigurationException | TransformerConfigurationException | MpqException e) {
			logger.error("Failed to save MPQ.", e);
			showErrorAlert(e);
		}
		logger.trace("opened mpq within {}ms.", (System.nanoTime() - time) / 1_000_000);
	}
	
	/**
	 * @return the mbarCtrl
	 */
	public MenuBarController getMbarCtrl() {
		return mbarCtrl;
	}
	
	/**
	 * Returns true, if the path of the current opened document is valid. Invalid usually means that no document has
	 * been opened.
	 *
	 * @return
	 */
	public boolean isInvalidOpenedDocPath() {
		return openedDocPath == null || "".equals(openedDocPath.toString());
	}
	
	/**
	 * Compiles and updates the data in the cache.
	 *
	 * @return true if changes were present
	 * @throws ParserConfigurationException
	 * @throws TransformerConfigurationException
	 */
	public boolean compile() throws ParserConfigurationException, TransformerConfigurationException {
		final boolean changed = layoutExtReader.updateLayoutFiles(mpqi.getCache());
		return layoutExtReader.updateGameStrings(mpqi.getCache()) || changed;
	}
	
	/**
	 * Updates the title of the App.
	 */
	public void updateAppTitle() {
		Platform.runLater(() -> {
			// Update UI here
			final StringBuilder sb = new StringBuilder(Messages.getString("Main.observerUiSettingsEditorTitle"));
			final Path openedDocPathTmp = getOpenedDocPath();
			if (openedDocPathTmp != null) {
				sb.append("- [").append(openedDocPathTmp).append(']');
			}
			if (hasUnsavedFileChanges()) {
				sb.append('*');
			}
			getPrimaryStage().setTitle(sb.toString());
		});
	}
	
	/**
	 * Shows an Error Alert with the specified message.
	 *
	 * @param e
	 */
	private void showErrorAlert(final Exception e) {
		Platform.runLater(() -> {
			logger.trace("showing error popup");
			final String title = Messages.getString("Main.anErrorOccured");
			final String content = e.getMessage();
			final Alert alert = Alerts.buildErrorAlert(getPrimaryStage(), title, title, content);
			alert.showAndWait();
		});
	}
	
	/**
	 * Returns the File path of the opened Document.
	 *
	 * @return
	 */
	public Path getOpenedDocPath() {
		return openedDocPath;
	}
	
	/**
	 * Returns the App's main window stage.
	 *
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	/**
	 * Open File window and actions.
	 */
	public void openUiMpq() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Messages.getString("Main.openObserverInterfaceTitle"));
		
		final ExtensionFilter genExtFilter = new ExtensionFilter(
				Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
				SC2_INTERFACE_FILE_FILTER,
				STORM_INTERFACE_FILE_FILTER);
		
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"),
				genExtFilter,
				new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
				new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"), STORM_INTERFACE_FILE_FILTER));
		fileChooser.setSelectedExtensionFilter(genExtFilter);
		final File f = fileChooser.showOpenDialog(primaryStage);
		if (f != null) {
			openMpqFileThreaded(f.toPath());
		}
	}
	
	/**
	 * Opens the specified MPQ file.
	 *
	 * @param file
	 */
	public void openMpqFileThreaded(final Path file) {
		new Thread(() -> {
			final Thread curThread = Thread.currentThread();
			curThread.setPriority(Thread.NORM_PRIORITY);
			//noinspection DynamicRegexReplaceableByCompiledPattern
			curThread.setName(curThread.getName().replaceFirst("Thread", "Open"));
			openMpqFile(file);
		}).start();
	}
	
	/**
	 * @param file
	 */
	public void openMpqFile(final Path file) {
		final long time = System.nanoTime();
		if (file != null) {
			try {
				mpqi.extractEntireMPQ(file.toString());
				openedDocPath = file;
				updateAppTitle();
				
				final Path componentListFile = mpqi.getComponentListFile();
				if (componentListFile == null) {
					throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList"));
				}
				
				final boolean isNamespaceHeroes = isNameSpaceHeroes(mpqi);
				final GameDef game = isNamespaceHeroes ? GameDef.buildHeroesGameDef() : GameDef.buildSc2GameDef();
				
				// load desc index from mpq
				descIndex.setDescIndexPathAndClear(ComponentsListReaderDom.getDescIndexPath(componentListFile, game));
				
				final Path descIndexFile = mpqi.getFilePathFromMpq(descIndex.getDescIndexIntPath());
				descIndex.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile).loaded());
				
				tabsCtrl.clearData();
				hasUnsavedFileChanges = false;
				
				// TODO rewrite with nio stream
				final List<Path> layoutFiles = listFiles(mpqi.getCache());
				
				layoutExtReader = new LayoutExtensionReader();
				layoutExtReader.processLayoutFiles(layoutFiles);
				
				final List<ValueDef> hotkeys = layoutExtReader.getHotkeys();
				tabsCtrl.getHotkeysData().addAll(hotkeys);
				
				final List<ValueDef> settings = layoutExtReader.getSettings();
				tabsCtrl.getSettingsData().addAll(settings);
				
				final ChangeListener<String> changeListener =
						(observable, oldValue, newValue) -> notifyFileDataWasChanged();
				
				for (final ValueDef valueDef : hotkeys) {
					valueDef.valueProperty().addListener(changeListener);
				}
				for (final ValueDef valueDef : settings) {
					valueDef.valueProperty().addListener(changeListener);
				}
				
			} catch (final MpqException | ShowToUserException e) {
				logger.error("File could not be opened.", e);
				openedDocPath = null;
				updateAppTitle();
				showErrorAlert(e);
			} catch (final InterruptedException e) {
				logger.error("Opening File was interrupted.", e);
				Thread.currentThread().interrupt();
				openedDocPath = null;
				updateAppTitle();
				showExceptionAlert(e);
			} catch (final Exception e) {
				logger.error("File could not be opened.", e);
				openedDocPath = null;
				updateAppTitle();
				showExceptionAlert(e);
			}
			updateMenuBar();
		} else {
			logger.trace("File to open was null, most likely due to 'cancel'.");
		}
		logger.trace("opened mpq within {}ms.", (System.nanoTime() - time) / 1_000_000);
	}
	
	/**
	 * @param mpqi
	 * @return
	 * @throws ShowToUserException
	 */
	private static boolean isNameSpaceHeroes(final MpqEditorInterface mpqi) throws ShowToUserException {
		try {
			return mpqi.isHeroesMpq();
		} catch (final MpqException e) {
			logger.error("Error while checking if namespace is heroes", e);
			// special case to show readable error to user
			throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList"));
		}
	}
	
	private List<Path> listFiles(final Path path) throws IOException {
		final FileListingVisitor fileVisitor = new FileListingVisitor();
		Files.walkFileTree(path, fileVisitor);
		return fileVisitor.getFilePaths();
	}
	
	/**
	 *
	 */
	public void notifyFileDataWasChanged() {
		final boolean oldState = hasUnsavedFileChanges;
		hasUnsavedFileChanges = layoutExtReader.hasChanges();
		
		if (oldState != hasUnsavedFileChanges) {
			updateAppTitle();
		}
	}
	
	/**
	 * Shows an Exception Alert.
	 *
	 * @param e
	 */
	private void showExceptionAlert(final Exception e) {
		Platform.runLater(() -> {
			logger.trace("showing exception popup");
			final Alert alert = Alerts.buildExceptionAlert(getPrimaryStage(), e);
			alert.showAndWait();
		});
	}
	
	/**
	 * Closes the currently opened document.
	 */
	public void closeFile() {
		if (askToSaveUnsavedChanges()) {
			// user did not press cancel
			openedDocPath = null;
			mpqi.clearCacheExtractedMpq();
			updateAppTitle();
			updateMenuBar();
			tabsCtrl.clearData();
			layoutExtReader.clearData();
			hasUnsavedFileChanges = false;
		}
	}
	
	/**
	 * Saves the currently opened document.
	 */
	public void saveUiMpqThreaded() {
		// steal focus to finish validating input before saving
		primaryStage.getScene().getRoot().requestFocus();
		new Thread(() -> {
			final Thread curThread = Thread.currentThread();
			curThread.setPriority(Thread.NORM_PRIORITY);
			//noinspection DynamicRegexReplaceableByCompiledPattern
			curThread.setName(curThread.getName().replaceFirst("Thread", "Save"));
			saveUiMpq();
		}).start();
	}
	
	/**
	 * Save As window + actions.
	 */
	public void saveAsUiMpq() {
		// cannot save, if not valid
		if (isInvalidOpenedDocPath()) {
			return;
		}
		
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Messages.getString("Main.saveUiTitle"));
		
		final ExtensionFilter genExtFilter = new ExtensionFilter(
				Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
				SC2_INTERFACE_FILE_FILTER,
				STORM_INTERFACE_FILE_FILTER);
		
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"),
				genExtFilter,
				new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
				new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"), STORM_INTERFACE_FILE_FILTER));
		fileChooser.setSelectedExtensionFilter(genExtFilter);
		
		final Path loadedF = getOpenedDocPath();
		fileChooser.setInitialFileName(loadedF.getFileName().toString());
		fileChooser.setInitialDirectory(loadedF.getParent().toFile());
		
		final File f = fileChooser.showSaveDialog(primaryStage);
		
		if (f != null) {
			try {
				compile();
				final File parentFile = f.getParentFile();
				if (parentFile == null) {
					throw new IOException(String.format("Parent of File %s is null.", f));
				}
				mpqi.buildMpq(parentFile.toPath(), f.getName(), false, MpqEditorCompression.BLIZZARD_SC2_HEROES, false);
				hasUnsavedFileChanges = false;
				openedDocPath = f.toPath();
				updateAppTitle();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final IOException | ParserConfigurationException | TransformerConfigurationException | MpqException e) {
				logger.error("Error while saving", e);
				showErrorAlert(e);
			}
		}
		updateMenuBar();
	}
	
	/**
	 * Hides the Splash Screen.
	 *
	 * @author Ahli
	 */
	private static final class SplashScreenHider implements Runnable {
		@Override
		public void run() {
			final SplashScreen splash = SplashScreen.getSplashScreen();
			if (splash != null) {
				splash.close();
			}
		}
	}
	
}
