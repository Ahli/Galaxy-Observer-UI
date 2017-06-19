package com.ahli.hotkeyUi.application;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.ahli.hotkeyUi.application.controller.MenuBarController;
import com.ahli.hotkeyUi.application.controller.TabsController;
import com.ahli.hotkeyUi.application.model.ValueDef;
import com.ahli.mpq.MpqInterface;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Application
 * 
 * @author Ahli
 *
 */
public class Main extends Application {
	static Logger LOGGER = LogManager.getLogger("Main");

	private Stage primaryStage;
	private BorderPane rootLayout;
	private MenuBarController mbarCtrl;
	private TabsController tabsCtrl;
	private String openedDocPath = null;
	private boolean isNamespaceHeroes = true;
	private MpqInterface mpqi = new MpqInterface();
	private DescIndexData descIndex = new DescIndexData(this, mpqi);
	private File basePath = null;
	private boolean hasUnsavedFileChanges = false;
	private LayoutExtensionReader layoutExtReader;

	/**
	 * Initialize the UI Application.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			Thread.currentThread().setName("UI");
			this.primaryStage = primaryStage;
			primaryStage.setMaximized(true);

			// if it fails to load the resource in as a jar, check the eclipse
			// settings
			this.primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/res/ahliLogo.png")));

			initRootLayout();

			// Load Tab layout from fxml file
			FXMLLoader loader = new FXMLLoader();
			TabPane tabPane = (TabPane) loader.load(this.getClass().getResourceAsStream("view/TabsLayout.fxml"));
			rootLayout.setCenter(tabPane);
			tabsCtrl = loader.getController();
			tabsCtrl.setMainApp(this);

			// ask to save on close
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (hasUnsavedFileChanges()) {
						// ask to save changes in the file
						Alert alert = new Alert(AlertType.CONFIRMATION, "Unsaved changes", ButtonType.YES,
								ButtonType.NO, ButtonType.CANCEL);
						alert.setContentText(openedDocPath + " has unsaved changes. Save them?");
						alert.setHeaderText("Unsaved Changes");
						Optional<ButtonType> result = alert.showAndWait();

						if ((result.isPresent())) {
							if (result.get() == ButtonType.YES) {
								saveUiMpq();
							} else {
								if (result.get() == ButtonType.CANCEL) {
									event.consume();
								}
							}
						}
					}
				}
			});

			initPaths();
			initMpqInterface(mpqi);

		} catch (Exception e) {
			LOGGER.error("App Error: " + ExceptionUtils.getStackTrace(e), e);
			e.printStackTrace();
		}
	}

	/**
	 * Entry point of Application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.trace("trace log visible");
		LOGGER.debug("debug log visible");
		LOGGER.info("info log visible");
		LOGGER.warn("warn log visible");
		LOGGER.error("error log visible");
		LOGGER.fatal("fatal log visible");

		LOGGER.trace("Configuration File of System: " + System.getProperty("log4j.configurationFile"));

		launch(args);
	}

	/**
	 * Returns true, if the path of the current opened document is valid.
	 * Invalid usually means that no document has been opened.
	 * 
	 * @return
	 */
	public boolean isValidOpenedDocPath() {
		return openedDocPath != null && !openedDocPath.equals("");
	}

	/**
	 * Open File window and actions.
	 */
	public void openUiMpq() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Observer Interface...");

		ExtensionFilter genExtFilter = new ExtensionFilter("SC2/Heroes Observer Interface", "*.SC2Interface",
				"*.StormInterface");

		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"), genExtFilter,
				new ExtensionFilter("SC2 Interface", "*.SC2Interface"),
				new ExtensionFilter("Heroes Interface", "*.StormInterface"));
		fileChooser.setSelectedExtensionFilter(genExtFilter);
		File f = fileChooser.showOpenDialog(primaryStage);

		openMpqFile(f);
	}

	/**
	 * Opens the specified MPQ file.
	 * 
	 * @param f
	 */
	public void openMpqFile(File f) {
		if (f != null) {
			try {
				mpqi.extractEntireMPQ(f.getAbsolutePath());
				openedDocPath = f.getAbsolutePath();
				updateAppTitle();

				// load desc index from mpq
				isNamespaceHeroes = mpqi.isHeroesNamespace();
				boolean ignoreRequiredToLoadEntries = true;

				File componentListFile = mpqi.getComponentListFile();
				descIndex.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile));

				File descIndexFile = mpqi.getCachedFile(descIndex.getDescIndexIntPath());
				descIndex.addLayoutIntPath(
						DescIndexReader.getLayoutPathList(descIndexFile, ignoreRequiredToLoadEntries));

				tabsCtrl.clearData();
				hasUnsavedFileChanges = false;

				boolean recursive = true;
				File cache = new File(mpqi.getMpqCachePath());
				Collection<File> layoutFiles = FileUtils.listFiles(cache, null, recursive);

				layoutExtReader = new LayoutExtensionReader();
				layoutExtReader.processLayoutFiles(layoutFiles);

				ArrayList<ValueDef> hotkeys = layoutExtReader.getHotkeys();
				tabsCtrl.getHotkeysData().addAll(hotkeys);

				ArrayList<ValueDef> settings = layoutExtReader.getSettings();
				tabsCtrl.getSettingsData().addAll(settings);

			} catch (Exception e) {
				// opening failed as namespace could not be read
				// TODO improve
				LOGGER.error("File could not be opened. Error: " + ExceptionUtils.getStackTrace(e), e);
				e.printStackTrace();
				openedDocPath = null;
				updateAppTitle();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error opening file");
				alert.setHeaderText("Error opening file");
				alert.setContentText("An error occurred:\n" + e.getMessage());
				alert.showAndWait();
			}
			updateMenuBar();

		} else {
			LOGGER.trace("File to open was null, most likely due to 'cancel'.");
		}
	}

	/**
	 * Closes the currently opened document.
	 */
	public void closeFile() {
		openedDocPath = null;
		mpqi.clearCacheExtractedMpq();
		updateAppTitle();
		updateMenuBar();
		tabsCtrl.clearData();
		layoutExtReader.clearData();
		hasUnsavedFileChanges = false;
	}

	/**
	 * Update the menu bar in the main window. E.g. this needs to be done after
	 * opening/closing a document to enable/disable the save buttons.
	 */
	private void updateMenuBar() {
		mbarCtrl.updateMenuBar();
	}

	/**
	 * Initializes the root layout.
	 * 
	 * @throws IOException
	 */
	public void initRootLayout() throws IOException {
		// Load root layout from fxml file.
		FXMLLoader loader = new FXMLLoader();
		/*
		 * works only in eclipse, not in jar:
		 * loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"
		 * )); rootLayout = (BorderPane) loader.load();
		 */
		rootLayout = (BorderPane) loader.load(this.getClass().getResourceAsStream("view/RootLayout.fxml"));

		// get Controller
		mbarCtrl = loader.getController();
		mbarCtrl.setMainApp(this);

		// Show the scene containing the root layout.
		Scene scene = new Scene(rootLayout);
		scene.getStylesheets().add(this.getClass().getResource("view/application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setTitle("Observer UI Settings Editor");
		updateMenuBar();
	}

	/**
	 * Returns true, if it belongs to Heroes of the Storm, false otherwise.
	 * 
	 * @return
	 */
	public boolean isHeroesFile() {
		return isNamespaceHeroes;
	}

	/**
	 * Saves the currently opened document.
	 */
	public void saveUiMpq() {
		// cannot save, if not valid
		if (!isValidOpenedDocPath()) {
			return;
		}

		try {
			compile();
			mpqi.buildMpq(openedDocPath, false, false);
			hasUnsavedFileChanges = false;
			updateAppTitle();
		} catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
			LOGGER.error(ExceptionUtils.getStackTrace(e), e);
			e.printStackTrace();
		}
	}

	/**
	 * Save As window + actions.
	 */
	public void saveAsUiMpq() {
		// cannot save, if not valid
		if (!isValidOpenedDocPath()) {
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save UI...");

		ExtensionFilter genExtFilter = new ExtensionFilter("SC2/Heroes Observer Interface", "*.SC2Interface",
				"*.StormInterface");

		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"), genExtFilter,
				new ExtensionFilter("SC2 Interface", "*.SC2Interface"),
				new ExtensionFilter("Heroes Interface", "*.StormInterface"));
		fileChooser.setSelectedExtensionFilter(genExtFilter);

		File loadedF = new File(this.getOpenedDocPath());
		fileChooser.setInitialFileName(loadedF.getName());
		fileChooser.setInitialDirectory(loadedF.getParentFile());

		File f = fileChooser.showSaveDialog(primaryStage);

		if (f != null) {
			try {
				compile();
				mpqi.buildMpq(f.getParentFile().getAbsolutePath(), f.getName(), false, false);
				hasUnsavedFileChanges = false;
				openedDocPath = f.getAbsolutePath();
				updateAppTitle();
			} catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
				LOGGER.error(ExceptionUtils.getStackTrace(e), e);
				e.printStackTrace();
			}
		}
		updateMenuBar();
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
	 * Compiles and updates the data in the cache.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void compile() throws ParserConfigurationException, SAXException, IOException {
		File cache = new File(mpqi.getMpqCachePath());
		boolean recursive = true;
		String[] extensions = new String[] { "StormLayout", "SC2Layout" };
		Collection<File> layoutFiles = FileUtils.listFiles(cache, extensions, recursive);
		layoutExtReader.updateLayoutFiles(layoutFiles);
	}

	/**
	 * Causes the App to shut down. This includes asking to save unsaved
	 * changes.
	 */
	public void closeApp() {
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	/**
	 * Initialize Paths
	 */
	private void initPaths() {
		basePath = getJarDir(Main.class);
	}

	/**
	 * Initializes the MPQ Interface.
	 * 
	 * @param mpqi
	 */
	private void initMpqInterface(MpqInterface mpqi) {
		mpqi.setMpqEditorPath(
				basePath + File.separator + "plugins" + File.separator + "mpq" + File.separator + "MPQEditor.exe");
	}

	/**
	 * from stackoverflow because why doesn't java have this functionality? It's
	 * not like nobody would need that or it is trivial to create...
	 * 
	 * @param aclass
	 * @return File at base path
	 */
	public static File getJarDir(Class<Main> aclass) {
		LOGGER.debug("_FINDING JAR'S PATH");

		// ATTEMPT #1
		File f = new File(System.getProperty("java.class.path"));
		File dir = f.getAbsoluteFile().getParentFile();
		String str = dir.toString();
		LOGGER.debug("Attempt#1 java.class.path: " + str);

		// check if started in eclipse
		if (str.contains("tools" + File.separator + "observerUiSettingsEditor" + File.separator + "project"
				+ File.separator + "target" + File.separator + "classes;")) {
			// get current working directory
			URI uri = new File(".").toURI();
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			// addLogMessage("METHOD TEST: " + new File(".").toURI());

			str = uri.getPath();
			LOGGER.debug("_URI path:" + str);

			if (str.startsWith("file:/")) {
				str = str.substring(6);
			}
			if (str.startsWith("/")) {
				str = str.substring(1);
			}
			if (str.endsWith("/./")) {
				str = str.substring(0, str.length() - 3);
			}

			URL url = aclass.getProtectionDomain().getCodeSource().getLocation();
			// class returns "rsrc:./", if 2nd option during jar export was
			// chosen
			if (!url.toString().startsWith("rsrc:./")) {
				// wild guess that we are in test environment
				str += "/testEnv/dev/";
				LOGGER.debug("assuming Test Environment: " + str);
			}

		}
		LOGGER.debug("_RESULT PATH: " + str);

		return new File(str);

		// URL url;
		// String extURL; // url.toExternalForm();
		//
		// // get an url
		// try {
		// addLogMessage("attempting to get URL");
		// url = aclass.getProtectionDomain().getCodeSource().getLocation();
		// // url is in one of two forms
		// // ./build/classes/ NetBeans test
		// // jardir/JarName.jar froma jar
		// } catch (SecurityException ex) {
		// addLogMessage("attempting to get URL - had exception");
		// url = aclass.getResource(aclass.getSimpleName() + ".class");
		// // url is in one of two forms, both ending
		// // "/com/physpics/tools/ui/PropNode.class"
		// // file:/U:/Fred/java/Tools/UI/build/classes
		// // jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
		// }
		// addLogMessage("URL: "+url);
		//
		// // convert to external form
		// extURL = url.toExternalForm();
		// addLogMessage("URL external form: "+extURL);
		//
		// // prune for various cases
		// if (extURL.endsWith(".jar")) { // from getCodeSource
		// addLogMessage("URL ends with jar");
		//
		// extURL = extURL.substring(0, extURL.lastIndexOf("/"));
		// addLogMessage("extURL: "+extURL);
		// } else { // from getResource
		// addLogMessage("get URL from resource");
		// String suffix = "/" + (aclass.getName()).replace(".", "/") +
		// ".class";
		// extURL = extURL.replace(suffix, "");
		// addLogMessage("extURL: "+extURL);
		// if (extURL.startsWith("jar:") && extURL.endsWith(".jar!")) {
		// addLogMessage("JAR path cutting");
		// extURL = extURL.substring(4, extURL.lastIndexOf("/"));
		// addLogMessage("extURL: "+extURL);
		// } else {
		// addLogMessage("not JAR");
		// //hack for dev environment => move up two levels
		// if(extURL.endsWith("/classes/")){
		// addLogMessage("URL ends with classes");
		// extURL = extURL.substring(0, extURL.lastIndexOf("/"));
		// extURL = extURL.substring(0, extURL.lastIndexOf("/"));
		// extURL = extURL.substring(0, extURL.lastIndexOf("/"));
		// extURL += "/testEnv/baseUI/";
		// addLogMessage("extURL: "+extURL);
		// }
		// }
		// }
		//
		// // convert back to url
		// try {
		// url = new URL(extURL);
		// addLogMessage("URL: "+url);
		// } catch (MalformedURLException mux) {
		// // leave url unchanged; probably does not happen
		// addLogMessage("leave url unchanged; probably does not happen");
		// }
		//
		// // convert url to File
		// try {
		// return new File(url.toURI());
		// } catch (URISyntaxException ex) {
		// addLogMessage("catched URISyntaxException");
		// return new File(url.getPath());
		// }
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
	 * 
	 */
	public void notifyFileDataWasChanged() {
		if (!hasUnsavedFileChanges) {
			hasUnsavedFileChanges = true;
			updateAppTitle();
		}
	}

	/**
	 * Updates the title of the App.
	 */
	public void updateAppTitle() {
		String title = "Observer UI Settings Editor";
		if (openedDocPath != null) {
			title += "- [" + openedDocPath + "]";
		}
		if (hasUnsavedFileChanges) {
			title += "*";
		}
		primaryStage.setTitle(title);
	}
}
