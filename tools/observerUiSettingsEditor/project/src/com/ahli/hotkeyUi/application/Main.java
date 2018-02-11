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
import com.ahli.hotkeyUi.application.model.ValueDef;
import com.ahli.hotkeyUi.application.ui.Alerts;
import com.ahli.hotkeyUi.application.ui.ShowToUserException;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.util.JarHelper;
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
public class Main extends Application {
	public static final String VERSION = "alpha";
	public static final String STORM_INTERFACE_FILE_FILTER = "*.StormInterface";
	public static final String SC2_INTERFACE_FILE_FILTER = "*.SC2Interface";
	static Logger logger = LogManager.getLogger(Main.class); // $NON-NLS-1$
	private final long appStartTime = System.nanoTime();
	private Stage primaryStage;
	private BorderPane rootLayout;
	private MenuBarController mbarCtrl;
	private TabsController tabsCtrl;
	private String openedDocPath = null;
	private MpqEditorInterface mpqi = null;
	private DescIndexData descIndex = null;
	private File basePath = null;
	private boolean hasUnsavedFileChanges = false;
	private LayoutExtensionReader layoutExtReader;
	
	/**
	 * Entry point of Application.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void main(final String[] args) {
		logger.trace("trace log visible"); //$NON-NLS-1$
		logger.debug("debug log visible"); //$NON-NLS-1$
		logger.info("info log visible"); //$NON-NLS-1$
		logger.warn("warn log visible"); //$NON-NLS-1$
		logger.error("error log visible"); //$NON-NLS-1$
		logger.fatal("fatal log visible"); //$NON-NLS-1$
		
		logger.trace("Configuration File of System: " +
				System.getProperty("log4j.configurationFile")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// TEST Locale
		// Messages.setBundle(Locale.CHINA);
		
		launch(args);
	}
	
	/**
	 * Initialize the UI Application.
	 */
	@Override
	public void start(final Stage primaryStage) {
		try {
			Thread.currentThread().setName("UI"); //$NON-NLS-1$
			logger.warn("start function called after " + (System.nanoTime() - appStartTime) / 1000000 + "ms.");
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			this.primaryStage = primaryStage;
			primaryStage.setMaximized(true);
			primaryStage.setOpacity(0);
			
			setUserAgentStylesheet(STYLESHEET_MODENA);
			
			initAppIcon();
			
			long time = System.nanoTime();
			initRootLayout();
			logger.warn("initialized root layout within " + (System.nanoTime() - time) / 1000000 + "ms.");
			
			// Load Tab layout from fxml file
			time = System.nanoTime();
			final FXMLLoader loader = new FXMLLoader();
			loader.setResources(Messages.getBundle());
			
			final TabPane tabPane;
			try (InputStream is = Main.class.getResourceAsStream("view/TabsLayout.fxml")) {
				tabPane = loader.load(is); // $NON-NLS-1$
			}
			
			logger.warn("initialized tab layout within " + (System.nanoTime() - time) / 1000000 + "ms.");
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
			
			time = System.nanoTime();
			primaryStage.show();
			primaryStage.setOpacity(1);
			logger.warn("executed root layout stage.show() within " + (System.nanoTime() - time) / 1000000 + "ms.");
			
			// hide apps splash screen image
			Platform.runLater(new SplashScreenHider());
			
			logger.warn("finished app initialization after " + (System.nanoTime() - appStartTime) / 1000000 + "ms.");
			
			initMpqInterface();
			
		} catch (final Exception e) {
			logger.error("App Error: " + ExceptionUtils.getStackTrace(e), e); //$NON-NLS-1$
			this.primaryStage.setOpacity(1);
			Alerts.buildExceptionAlert(this.primaryStage, e).showAndWait();
			closeApp();
		}
	}
	
	private void initAppIcon() {
		// if it fails to load the resource in as a jar, check the eclipse settings
		try {
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/res/ahliLogo.png"))); //$NON-NLS-1$
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
		long time = System.nanoTime();
		final FXMLLoader loader = new FXMLLoader();
		loader.setResources(Messages.getBundle());
		
		try (InputStream is = Main.class.getResourceAsStream("view/RootLayout.fxml")) {
			rootLayout = loader.load(is); // $NON-NLS-1$
		}
		logger.warn("initialized root layout fxml within " + (System.nanoTime() - time) / 1000000 + "ms.");
		
		// get Controller
		time = System.nanoTime();
		mbarCtrl = loader.getController();
		mbarCtrl.setMainApp(this);
		logger.warn("received root layout controller within " + (System.nanoTime() - time) / 1000000 + "ms.");
		
		// Show the scene containing the root layout.
		final Scene scene = new Scene(rootLayout);
		time = System.nanoTime();
		scene.getStylesheets().add(Main.class.getResource("view/application.css").toExternalForm()); //$NON-NLS-1$
		
		logger.debug("installed font families: " + Font.getFamilies());
		logger.trace("Locale dflt is '" + Locale.getDefault() + "'"); //$NON-NLS-1$
		logger.trace("Locale of Messages.class is '" + Messages.getBundle().getLocale() + "'"); //$NON-NLS-1$
		logger.trace("Locale china: " + Locale.SIMPLIFIED_CHINESE); //$NON-NLS-1$
		if (Messages.checkIfTargetResourceIsUsed(Locale.CHINA)) {
			logger.trace("apply Chinese css"); //$NON-NLS-1$
			
			scene.getStylesheets().add(Main.class.getResource("i18n/china.css").toExternalForm());
			// //$NON-NLS-1$
			
		}
		logger.warn("initialized root layout css within " + (System.nanoTime() - time) / 1000000 + "ms.");
		
		time = System.nanoTime();
		primaryStage.setTitle(Messages.getString("Main.observerUiSettingsEditorTitle")); //$NON-NLS-1$
		primaryStage.setScene(scene);
		logger.warn("executed root layout setScene+title within " + (System.nanoTime() - time) / 1000000 + "ms.");
		
		time = System.nanoTime();
		updateMenuBar();
		logger.warn("updateMenuBar within " + (System.nanoTime() - time) / 1000000 + "ms.");
	}
	
	/**
	 * Asks the user to decide on unsaved changes, if there are any.
	 *
	 * @return false if the user pressed cancel, else true
	 */
	public boolean askToSaveUnsavedChanges() {
		if (hasUnsavedFileChanges()) {
			// ask to save changes in the file
			final String title = Messages.getString("Main.unsavedChangesTitle"); //$NON-NLS-1$
			final String content =
					String.format(Messages.getString("Main.hasUnsavedChanges"), openedDocPath); //$NON-NLS-1$
			
			final Alert alert = Alerts.buildYesNoCancelAlert(primaryStage, title, title, content);
			
			final Optional<ButtonType> result = alert.showAndWait();
			
			if ((result.isPresent())) {
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
		basePath = JarHelper.getJarDir(Main.class);
	}
	
	/**
	 * Initializes the MPQ Editor Interface.
	 */
	private void initMpqInterface() {
		final String tempDirectory = System.getProperty("java.io.tmpdir");
		final String cachePath = tempDirectory + "ObserverUiSettingsEditor" + File.separator + "_ExtractedMpq";
		mpqi = new MpqEditorInterface(cachePath);
		final String path = basePath + File.separator + "plugins" + File.separator + "mpq" + File.separator
				//$NON-NLS-1$ //$NON-NLS-2$
				+ "MPQEditor.exe"; //$NON-NLS-1$
		mpqi.setMpqEditorPath(path);
		final File f = new File(path);
		if (!f.exists() || !f.isFile()) {
			logger.error("Could not find MPQEditor.exe within its expected path: " + path);
			final String title = Messages.getString("Main.warningAlertTitle"); //$NON-NLS-1$
			final String content = String.format(Messages.getString("Main.couldNotFindMpqEditor"), path); //$NON-NLS-1$
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
		} catch (IOException | InterruptedException | ParserConfigurationException | SAXException | MpqException e) {
			logger.error(ExceptionUtils.getStackTrace(e), e);
			showErrorAlert(e);
		}
		logger.warn("opened mpq within " + (System.nanoTime() - time) / 1000000 + "ms.");
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
		return openedDocPath != null && !openedDocPath.equals(""); //$NON-NLS-1$
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
		final boolean recursive = true;
		final String[] extensions = new String[] { "StormLayout", "SC2Layout" }; //$NON-NLS-1$ //$NON-NLS-2$
		final Collection<File> layoutFiles = FileUtils.listFiles(cache, extensions, recursive);
		layoutExtReader.updateLayoutFiles(layoutFiles);
	}
	
	/**
	 * Updates the title of the App.
	 */
	public void updateAppTitle() {
		Platform.runLater(() -> {
			// Update UI here
			String title = Messages.getString("Main.observerUiSettingsEditorTitle"); //$NON-NLS-1$
			final String openedDocPathTmp = getOpenedDocPath();
			if (openedDocPathTmp != null) {
				title += "- [" + openedDocPathTmp + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (hasUnsavedFileChanges()) {
				title += "*"; //$NON-NLS-1$
			}
			getPrimaryStage().setTitle(title);
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
			final String title = Messages.getString("Main.anErrorOccured"); //$NON-NLS-1$
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
		fileChooser.setTitle(Messages.getString("Main.openObserverInterfaceTitle")); //$NON-NLS-1$
		
		final ExtensionFilter genExtFilter =
				new ExtensionFilter(Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
						SC2_INTERFACE_FILE_FILTER, STORM_INTERFACE_FILE_FILTER);
		
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"),
				//$NON-NLS-1$ //$NON-NLS-2$
				genExtFilter,
				new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
				//$NON-NLS-1$ //$NON-NLS-2$
				new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"),
						STORM_INTERFACE_FILE_FILTER)); //$NON-NLS-1$ //$NON-NLS-2$
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
				setName(getName().replaceFirst("Thread", "Open")); //$NON-NLS-1$
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
				
				// load desc index from mpq
				final boolean isNamespaceHeroes;
				try {
					isNamespaceHeroes = mpqi.isHeroesMpq();
				} catch (final MpqException e) {
					// special case to show readable error to user
					throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList"));
				}
				final boolean ignoreRequiredToLoadEntries = true;
				
				final File componentListFile = mpqi.getComponentListFile();
				if (componentListFile == null) {
					throw new ShowToUserException(Messages.getString("Main.OpenedFileNoComponentList")); //$NON-NLS-1$
				}
				
				final GameDef game = isNamespaceHeroes ? new HeroesGameDef() : new SC2GameDef();
				
				descIndex.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile, game));
				
				final File descIndexFile = mpqi.getFileFromMpq(descIndex.getDescIndexIntPath());
				descIndex.addLayoutIntPath(
						DescIndexReader.getLayoutPathList(descIndexFile, ignoreRequiredToLoadEntries));
				
				tabsCtrl.clearData();
				hasUnsavedFileChanges = false;
				
				final boolean recursive = true;
				final File cache = new File(mpqi.getMpqCachePath());
				final Collection<File> layoutFiles = FileUtils.listFiles(cache, null, recursive);
				
				layoutExtReader = new LayoutExtensionReader();
				layoutExtReader.processLayoutFiles(layoutFiles);
				
				final List<ValueDef> hotkeys = layoutExtReader.getHotkeys();
				tabsCtrl.getHotkeysData().addAll(hotkeys);
				
				final List<ValueDef> settings = layoutExtReader.getSettings();
				tabsCtrl.getSettingsData().addAll(settings);
				
			} catch (MpqException | ShowToUserException e) {
				logger.error("File could not be opened. MPQ-Error: " + ExceptionUtils.getStackTrace(e),
						e); //$NON-NLS-1$
				openedDocPath = null;
				updateAppTitle();
				showErrorAlert(e);
			} catch (final Exception e) {
				logger.error("File could not be opened. Error: " + ExceptionUtils.getStackTrace(e), e); //$NON-NLS-1$
				openedDocPath = null;
				updateAppTitle();
				showExceptionAlert(e);
			}
			updateMenuBar();
		} else {
			logger.trace("File to open was null, most likely due to 'cancel'."); //$NON-NLS-1$
		}
		logger.warn("opened mpq within " + (System.nanoTime() - time) / 1000000 + "ms.");
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
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				setName(getName().replaceFirst("Thread", "Save")); //$NON-NLS-1$
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
		fileChooser.setTitle(Messages.getString("Main.saveUiTitle")); //$NON-NLS-1$
		
		final ExtensionFilter genExtFilter =
				new ExtensionFilter(Messages.getString("Main.sc2HeroesObserverInterfaceExtFilter"),
						SC2_INTERFACE_FILE_FILTER,
						//$NON-NLS-1$ //$NON-NLS-2$
						STORM_INTERFACE_FILE_FILTER); //$NON-NLS-1$
		
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(Messages.getString("Main.allFilesFilter"), "*.*"),
				//$NON-NLS-1$ //$NON-NLS-2$
				genExtFilter,
				new ExtensionFilter(Messages.getString("Main.sc2InterfaceFilter"), SC2_INTERFACE_FILE_FILTER),
				//$NON-NLS-1$ //$NON-NLS-2$
				new ExtensionFilter(Messages.getString("Main.heroesInterfaceFilter"),
						STORM_INTERFACE_FILE_FILTER)); //$NON-NLS-1$ //$NON-NLS-2$
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
			} catch (IOException | InterruptedException | ParserConfigurationException | SAXException | MpqException e) {
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
