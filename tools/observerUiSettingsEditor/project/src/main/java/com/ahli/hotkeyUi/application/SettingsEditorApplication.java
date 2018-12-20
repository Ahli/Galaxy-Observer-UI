package com.ahli.hotkeyUi.application;

import com.ahli.galaxy.archive.ComponentsListReader;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.hotkeyUi.application.controller.MenuBarController;
import com.ahli.hotkeyUi.application.controller.TabsController;
import com.ahli.hotkeyUi.application.galaxy.ext.LayoutExtensionReader;
import com.ahli.hotkeyUi.application.i18n.Messages;
import com.ahli.hotkeyUi.application.integration.JarHelper;
import com.ahli.hotkeyUi.application.model.ValueDef;
import com.ahli.hotkeyUi.application.ui.Alerts;
import com.ahli.hotkeyUi.application.ui.ShowToUserException;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
	private static final Logger logger = LogManager.getLogger(SettingsEditorApplication.class);
	private final long appStartTime = System.nanoTime();
	private Stage primaryStage;
	private BorderPane rootLayout;
	private MenuBarController mbarCtrl;
	private TabsController tabsCtrl;
	private String openedDocPath;
	private MpqEditorInterface mpqi;
	private DescIndexData descIndex;
	private File basePath;
	private boolean hasUnsavedFileChanges;
	private LayoutExtensionReader layoutExtReader;
	
	public SettingsEditorApplication() {
		// nothing to do
	}
	
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
		logger.fatal("fatal log visible");
		
		logger.trace("Configuration File of System: {}", () -> System.getProperty("log4j.configurationFile"));
		
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
			logger.trace("start function called after {}ms.", () -> (System.nanoTime() - appStartTime) / 1000000);
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			this.primaryStage = primaryStage;
			primaryStage.setMaximized(true);
			primaryStage.setOpacity(0);
			
			setUserAgentStylesheet(STYLESHEET_MODENA);
			
			initAppIcon();
			
			final long time = System.nanoTime();
			initRootLayout();
			logger.trace("initialized root layout within {}ms.", () -> (System.nanoTime() - time) / 1000000);
			
			// Load Tab layout from fxml file
			final long time2 = System.nanoTime();
			final FXMLLoader loader = new FXMLLoader();
			loader.setResources(Messages.getBundle());
			
			final TabPane tabPane;
			try (final InputStream is = SettingsEditorApplication.class.getResourceAsStream("/view/TabsLayout.fxml")) {
				tabPane = loader.load(is);
			}
			
			logger.trace("initialized tab layout within {}ms.", () -> (System.nanoTime() - time2) / 1000000);
			rootLayout.setCenter(tabPane);
			tabsCtrl = loader.getController();
			tabsCtrl.setMainApp(this);
			
			// ask to save on close
			primaryStage.setOnCloseRequest(new EventHandler<>() {
				@Override
				public void handle(final WindowEvent event) {
					final boolean notCancelled = askToSaveUnsavedChanges();
					if (!notCancelled) {
						// cancel closing
						event.consume();
					}
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
			logger.trace("executed root layout stage.show() within {}", () -> (System.nanoTime() - time3) / 1000000);
			
			// hide apps splash screen image
			Platform.runLater(new SplashScreenHider());
			
			logger.trace("finished app initialization after {}", () -> (System.nanoTime() - appStartTime) / 1000000);
			
			initMpqInterface();
			
		} catch (final Exception e) {
			logger.error("App Error: " + ExceptionUtils.getStackTrace(e), e);
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
			logger.error("Error loading resource");
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
		logger.trace("initialized root layout fxml within {}ms.", () -> (System.nanoTime() - time) / 1000000);
		
		// get Controller
		final long time2 = System.nanoTime();
		mbarCtrl = loader.getController();
		mbarCtrl.setMainApp(this);
		logger.trace("received root layout controller within {}ms.", () -> (System.nanoTime() - time2) / 1000000);
		
		// Show the scene containing the root layout.
		final Scene scene = new Scene(rootLayout);
		final long time3 = System.nanoTime();
		scene.getStylesheets()
				.add(SettingsEditorApplication.class.getResource("/view/application.css").toExternalForm());
		
		if (logger.isTraceEnabled()) {
			logger.trace("installed font families: " + Font.getFamilies());
			logger.trace("Locale dflt is '" + Locale.getDefault() + "'");
			logger.trace("Locale of Messages.class is '" + Messages.getBundle().getLocale() + "'");
			logger.trace("Locale china: " + Locale.SIMPLIFIED_CHINESE);
		}
		if (Messages.checkIfTargetResourceIsUsed(Locale.CHINA)) {
			logger.trace("apply Chinese css");
			scene.getStylesheets().add(SettingsEditorApplication.class.getResource("/i18n/china.css").toExternalForm());
		}
		logger.trace("initialized root layout css within {}ms.", () -> (System.nanoTime() - time3) / 1000000);
		
		final long time4 = System.nanoTime();
		primaryStage.setTitle(Messages.getString("Main.observerUiSettingsEditorTitle"));
		primaryStage.setScene(scene);
		logger.trace("executed root layout setScene+title within {}ms.", () -> (System.nanoTime() - time4) / 1000000);
		
		final long time5 = System.nanoTime();
		updateMenuBar();
		logger.trace("updateMenuBar within {}ms.", () -> (System.nanoTime() - time5) / 1000000);
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
		final String cachePath = tempDirectory + "ObserverUiSettingsEditor" + File.separator + "_ExtractedMpq";
		final String mpqEditorPath =
				basePath + File.separator + "plugins" + File.separator + "mpq" + File.separator + "MPQEditor.exe";
		mpqi = new MpqEditorInterface(cachePath, mpqEditorPath);
		final File f = new File(mpqEditorPath);
		if (!f.exists() || !f.isFile()) {
			logger.error("Could not find MPQEditor.exe within its expected path: " + mpqEditorPath);
			final String title = Messages.getString("Main.warningAlertTitle");
			final String content = String.format(Messages.getString("Main.couldNotFindMpqEditor"), mpqEditorPath);
			final Alert alert = Alerts.buildWarningAlert(primaryStage, title, title, content);
			alert.showAndWait();
		}
		descIndex = new DescIndexData(mpqi);
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
		if (!isValidOpenedDocPath()) {
			return;
		}
		
		try {
			compile();
			mpqi.buildMpq(openedDocPath, false, MpqEditorCompression.BLIZZARD_SC2_HEROES, false);
			hasUnsavedFileChanges = false;
			updateAppTitle();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException | ParserConfigurationException | SAXException | MpqException e) {
			logger.error(ExceptionUtils.getStackTrace(e), e);
			showErrorAlert(e);
		}
		logger.trace("opened mpq within {}ms.", () -> (System.nanoTime() - time) / 1000000);
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
	public boolean isValidOpenedDocPath() {
		return openedDocPath != null && !openedDocPath.equals("");
	}
	
	/**
	 * Compiles and updates the data in the cache.
	 *
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void compile() throws ParserConfigurationException, SAXException {
		final File cache = new File(mpqi.getMpqCachePath());
		final String[] suffixes = new String[] { ".stormlayout", ".SC2Layout" };
		final Collection<File> layoutFiles =
				FileUtils.listFiles(cache, new SuffixFileFilter(suffixes, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
		layoutExtReader.updateLayoutFiles(layoutFiles);
	}
	
	/**
	 * Updates the title of the App.
	 */
	public void updateAppTitle() {
		Platform.runLater(() -> {
			// Update UI here
			final StringBuilder sb = new StringBuilder(Messages.getString("Main.observerUiSettingsEditorTitle"));
			final String openedDocPathTmp = getOpenedDocPath();
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
			if (logger.isTraceEnabled()) {
				logger.trace("showing error popup");
			}
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
	public String getOpenedDocPath() {
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
		
		final ExtensionFilter genExtFilter =
				new ExtensionFilter(Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
						SC2_INTERFACE_FILE_FILTER, STORM_INTERFACE_FILE_FILTER);
		
		fileChooser.getExtensionFilters()
				.addAll(new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"), genExtFilter,
						new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
						new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"),
								STORM_INTERFACE_FILE_FILTER));
		fileChooser.setSelectedExtensionFilter(genExtFilter);
		final File f = fileChooser.showOpenDialog(primaryStage);
		
		openMpqFileThreaded(f);
	}
	
	/**
	 * Opens the specified MPQ file.
	 *
	 * @param f
	 */
	public void openMpqFileThreaded(final File f) {
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				setName(getName().replaceFirst("Thread", "Open"));
				openMpqFile(f);
			}
		}.start();
	}
	
	/**
	 * @param f
	 */
	public void openMpqFile(final File f) {
		final long time = System.nanoTime();
		if (f != null) {
			try {
				mpqi.extractEntireMPQ(f.getAbsolutePath());
				openedDocPath = f.getAbsolutePath();
				updateAppTitle();
				
				final File componentListFile = mpqi.getComponentListFile();
				if (componentListFile == null) {
					throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList"));
				}
				
				final boolean isNamespaceHeroes = isNameSpaceHeroes(mpqi);
				final GameDef game = isNamespaceHeroes ? new HeroesGameDef() : new SC2GameDef();
				
				// load desc index from mpq
				descIndex.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile, game));
				
				final File descIndexFile = mpqi.getFilePathFromMpq(descIndex.getDescIndexIntPath()).toFile();
				descIndex.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, true));
				
				tabsCtrl.clearData();
				hasUnsavedFileChanges = false;
				
				final File cache = new File(mpqi.getMpqCachePath());
				final Collection<File> layoutFiles = FileUtils.listFiles(cache, null, true);
				
				layoutExtReader = new LayoutExtensionReader();
				layoutExtReader.processLayoutFiles(layoutFiles);
				
				final List<ValueDef> hotkeys = layoutExtReader.getHotkeys();
				tabsCtrl.getHotkeysData().addAll(hotkeys);
				
				final List<ValueDef> settings = layoutExtReader.getSettings();
				tabsCtrl.getSettingsData().addAll(settings);
				
			} catch (final MpqException | ShowToUserException e) {
				logger.error("File could not be opened. MPQ-Error: " + ExceptionUtils.getStackTrace(e), e);
				openedDocPath = null;
				updateAppTitle();
				showErrorAlert(e);
			} catch (final Exception e) {
				logger.error("File could not be opened. Error: " + ExceptionUtils.getStackTrace(e), e);
				openedDocPath = null;
				updateAppTitle();
				showExceptionAlert(e);
			}
			updateMenuBar();
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("File to open was null, most likely due to 'cancel'.");
			}
		}
		logger.trace("opened mpq within {}ms.", () -> (System.nanoTime() - time) / 1000000);
	}
	
	/**
	 * @param mpqi
	 * @return
	 * @throws ShowToUserException
	 */
	private boolean isNameSpaceHeroes(final MpqEditorInterface mpqi) throws ShowToUserException {
		try {
			return mpqi.isHeroesMpq();
		} catch (final MpqException e) {
			// special case to show readable error to user
			throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList"));
		}
	}
	
	/**
	 * Shows an Exception Alert.
	 *
	 * @param e
	 */
	private void showExceptionAlert(final Exception e) {
		Platform.runLater(() -> {
			if (logger.isTraceEnabled()) {
				logger.trace("showing exception popup");
			}
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
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				setName(getName().replaceFirst("Thread", "Save"));
				saveUiMpq();
			}
		}.start();
	}
	
	/**
	 * Save As window + actions.
	 */
	public void saveAsUiMpq() {
		// cannot save, if not valid
		if (!isValidOpenedDocPath()) {
			return;
		}
		
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Messages.getString("Main.saveUiTitle"));
		
		final ExtensionFilter genExtFilter =
				new ExtensionFilter(Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
						SC2_INTERFACE_FILE_FILTER, STORM_INTERFACE_FILE_FILTER);
		
		fileChooser.getExtensionFilters()
				.addAll(new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"), genExtFilter,
						new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
						new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"),
								STORM_INTERFACE_FILE_FILTER));
		fileChooser.setSelectedExtensionFilter(genExtFilter);
		
		final File loadedF = new File(getOpenedDocPath());
		fileChooser.setInitialFileName(loadedF.getName());
		fileChooser.setInitialDirectory(loadedF.getParentFile());
		
		final File f = fileChooser.showSaveDialog(primaryStage);
		
		if (f != null) {
			try {
				compile();
				mpqi.buildMpq(f.getParentFile().getAbsolutePath(), f.getName(), false,
						MpqEditorCompression.BLIZZARD_SC2_HEROES, false);
				hasUnsavedFileChanges = false;
				openedDocPath = f.getAbsolutePath();
				updateAppTitle();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final IOException | ParserConfigurationException | SAXException | MpqException e) {
				logger.error(ExceptionUtils.getStackTrace(e), e);
				showErrorAlert(e);
			}
		}
		updateMenuBar();
	}
	
	/**
	 *
	 */
	public void notifyFileDataWasChanged() {
		if (!hasUnsavedFileChanges) {
			hasUnsavedFileChanges = true;
			updateAppTitle();
		}
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
