package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.xml.sax.SAXException;

import application.integration.SettingsInterface;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 * 
 * @author Ahli
 *
 */
public class Main extends Application {
	private MpqInterface mpqi;
	private SettingsInterface settings = new SettingsInterface();
	private DescIndexData descIndex = new DescIndexData(this);
	private boolean namespaceHeroes = true;
	private String documentsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	private File basePath = null;
	public final static String errorLine = "#########################################";
	private TextArea txtArea = null;
	private boolean encounteredError = false;

	@Override
	/**
	 * Called when the App is initializing.
	 */
	public void start(Stage primaryStage) {
		try {
			clearErrorEncounter();


			// {
			// // TEST
			// String paramRunPath = "F:" + File.separator + "Spiele" +
			// File.separator + "Heroes of the Storm"
			// + File.separator + "Support" + File.separator +
			// "HeroesSwitcher.exe";
			// boolean isHeroes = paramRunPath.contains("HeroesSwitcher.exe");
			// File replay = getNewestReplay(isHeroes);
			// if (replay != null) {
			// System.out.println("LOAD REPLAY INTO GAME");
			// System.out.println("cmd /C start \"\" \"" + paramRunPath + "\"
			// \"" +
			// replay.getAbsolutePath() + "\"");
			//
			// Runtime.getRuntime()
			// .exec("cmd /C start \"\" \"" + paramRunPath + "\" \"" +
			// replay.getAbsolutePath() + "\"");
			// System.out.println("AFTER EXEC");
			// }
			// if (true) {
			// return;
			// }
			// }

			// named params
			// e.g. "--paramname=value".
			Parameters params = this.getParameters();
			Map<String, String> namedParams = params.getNamed();
			// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
			String paramCompilePath = null;
			paramCompilePath = namedParams.get("compile");
			boolean compileAndRun = false;
			if (namedParams.get("compileRun") != null) {
				paramCompilePath = namedParams.get("compileRun");
				compileAndRun = true;
			}
			paramCompilePath = cutCompileParamPath(paramCompilePath);
			boolean hasParamCompilePath = (paramCompilePath != null);

			// run parameter (most likely not used)
			// --run="F:\Spiele\Heroes of the Storm\Support\HeroesSwitcher.exe"
			String paramRunPath = namedParams.get("run");

			BorderPane root = new BorderPane();
			primaryStage.setTitle("Compiling Interfaces...");

			txtArea = new TextArea();
			txtArea.setText("Initializing App...");

			root.setCenter(txtArea);
			Scene scene = new Scene(root, 1200, 400);
			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

			basePath = null;
			// basePath = new
			// File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			basePath = getJarDir(Main.class);
			initSettings(settings);
			
			// output data
			System.out.println("basePath: " + basePath);
			System.out.println("documentsPath: " + documentsPath);
			addLogMessage("basePath: " + basePath);
			addLogMessage("documentsPath: " + documentsPath);
			if (paramCompilePath != null) {
				addLogMessage("compile param path: " + paramCompilePath);
				System.out.println("compile param path: " + paramCompilePath);
				if (compileAndRun) {
					addLogMessage("run after compile: " + compileAndRun);
					System.out.println("run after compile: " + compileAndRun);
				}
			}
			if (paramRunPath != null) {
				addLogMessage("run param path: " + paramRunPath);
				System.out.println("run param path: " + paramRunPath);
			}

			// init MPQ stuff
			mpqi = new MpqInterface();
			initMpqInterface(mpqi);

			// WORK WORK WORK
			if (!hasParamCompilePath) {
				// compile all
				buildGamesUIs("heroes", true);
				buildGamesUIs("sc2", false);
			} else {
				// compile param
				boolean isHeroes = paramCompilePath.contains("heroes" + File.separator);
				buildSpecificUI(new File(paramCompilePath), isHeroes);
			}

			primaryStage.setTitle("Compiling Interfaces... done.");
			addLogMessage("All done.");

			if (!hasEncounteredError()) {
				// start game, launch replay
				attemptToRunGameWithReplay(paramRunPath, compileAndRun, paramCompilePath);
			}

			if (!hasEncounteredError() && !hasParamCompilePath) {
				// close after 5 seconds, if compiled all and no errors
				PauseTransition delay = new PauseTransition(Duration.seconds(5));
				delay.setOnFinished(event -> primaryStage.close());
				delay.play();
			} else if (!hasEncounteredError() && (hasParamCompilePath || paramRunPath != null)) {
				// close instantly, if compiled special and no errors
				primaryStage.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempt to run the Game with the newest Replay file.
	 * 
	 * @param paramRunPath
	 * @param paramCompilePath
	 * @param compileAndRun
	 */
	private void attemptToRunGameWithReplay(String paramRunPath, boolean compileAndRun, String paramCompilePath) {
		boolean isHeroes = false;
		String gamePath = null;

		if (!compileAndRun) {
			// use the run param
			gamePath = paramRunPath;
			isHeroes = paramRunPath.contains("HeroesSwitcher.exe");
		} else {
			// compileAndRun is active -> figure out the right game
			if(paramCompilePath.contains(File.separator+"heroes"+File.separator)){
				// Heroes
				isHeroes = true;
				if(settings.isPtrActive()){
					// PTR Heroes
					if(settings.isHeroesPtr64bit()){
						gamePath = settings.getHeroesPtrPath()+File.separator+"Support64"+File.separator+"HeroesSwitcher_x64.exe";
					}
					else {
						gamePath = settings.getHeroesPtrPath()+File.separator+"Support"+File.separator+"HeroesSwitcher.exe";
					}
				} else {
					// live Heroes
					if(settings.isHeroes64bit()){
						gamePath = settings.getHeroesPath()+File.separator+"Support64"+File.separator+"HeroesSwitcher_x64.exe";
					}
					else {
						gamePath = settings.getHeroesPath()+File.separator+"Support"+File.separator+"HeroesSwitcher.exe";
					}
				}
			} else {
				// SC2
				isHeroes = false;
				if(settings.isSC264bit()){
					gamePath = settings.getSC2Path()+File.separator+"Support64"+File.separator+"SC2Switcher_x64.exe";
				}
				else {
					gamePath = settings.getSC2Path()+File.separator+"Support"+File.separator+"SC2Switcher.exe";
				}
			}
		}

		if (gamePath != null) {
			File replay = getNewestReplay(isHeroes);
			if (replay != null) {
				System.out.println("Starting game with replay...");
				System.out.println("cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"");
				try {
					Runtime.getRuntime()
							.exec("cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"");
					System.out.println("After Start attempt...");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Param path might be some layout or folder within the interface, so we cut
	 * it down to the interface base path.
	 * 
	 * @param paramPath
	 * @return
	 */
	private String cutCompileParamPath(String paramPath) {
		String str = paramPath;
		if (str != null) {
			while (str.length() > 0 && !str.endsWith("Interface")) {
				System.out.println("cutting progress: " + str);
				int lastIndex = str.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					str = str.substring(0, lastIndex);
				} else {
					System.out.println("LastIndex is -1 => null compile path");
					return null;
				}
			}
		}
		System.out.println("cutting result: " + str);
		return str;
	}

	/**
	 * Returns the newest Replay file for the game.
	 * 
	 * @param isHeroes
	 * @return
	 */
	private File getNewestReplay(boolean isHeroes) {
		String basePath = documentsPath + File.separator;
		String[] extensions = null;
		if (isHeroes) {
			basePath += "Heroes of the Storm";
			extensions = new String[] { "StormReplay" };
		} else {
			basePath += "StarCraft II";
			extensions = new String[] { "SC2Replay" };
		}
		basePath += File.separator + "Accounts";
		System.out.println(basePath);
		// FAILS for some reason
		// boolean recursive = true;
		// Collection<File> allReplays = FileUtils.listFiles(new File(basePath),
		// extensions, recursive);
		Collection<File> allReplays = FileUtils.listFiles(new File(basePath), TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		System.out.println("# Replays found: " + allReplays.size());

		long newestDate = Long.MIN_VALUE;
		File newestReplay = null;
		for (File curReplay : allReplays) {
			// check extension of file
			// System.out.println(curReplay.getName());
			// System.out.println(FilenameUtils.getExtension(curReplay.getName()));
			if (curReplay.isFile() && FilenameUtils.getExtension(curReplay.getName()).equalsIgnoreCase(extensions[0])) {
				// check date
				long curDate = curReplay.lastModified();
				// System.out.println(curDate);
				if (curDate > newestDate) {
					newestDate = curDate;
					newestReplay = curReplay;
				}
			}
		}
		if (newestReplay != null) {
			System.out.println("newest Replay: " + newestReplay.getName());
		}
		return newestReplay;
	}

	/**
	 * Build all Interfaces for game subfolders.
	 * 
	 * @param subfolderName
	 * @param isHeroes
	 */
	public void buildGamesUIs(String subfolderName, boolean isHeroes) {
		File dir = new File(basePath.getAbsolutePath() + File.separator + subfolderName);
		namespaceHeroes = isHeroes;
		if (dir.exists() && dir.isDirectory()) {
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					buildSpecificUI(child, isHeroes);
				}
			}
		}
	}

	/**
	 * Build a specific Interface.
	 * 
	 * @param folder
	 * @param isHeroes
	 */
	public void buildSpecificUI(File interfaceFolder, boolean isHeroes) {
		if (interfaceFolder.isDirectory()) {
			buildFile(interfaceFolder, isHeroes);
		}
	}

	/**
	 * Entry point of the App
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Launch arguments: " + Arrays.toString(args));
		launch(args);
	}

	/**
	 * Add a message to the message log.
	 * 
	 * @param msg
	 */
	public void addLogMessage(String msg) {
		txtArea.setText(txtArea.getText() + "\n" + msg);
	}

	/**
	 * Builds MPQ Archive File.
	 * 
	 * @param file
	 * @param isHeroes
	 */
	@SuppressWarnings("static-access")
	private void buildFile(File file, boolean isHeroes) {
		String buildPath = documentsPath + File.separator;

		System.out.println("Starting to build file: " + file.getPath());

		if (isHeroes) {
			buildPath += "Heroes of the Storm";
		} else {
			buildPath += "StarCraft II";
		}
		buildPath += File.separator + "Interfaces";

		// get and create cache
		File cache = new File(mpqi.getMpqCachePath());
		cache.mkdirs();

		int cacheClearAttempts = 0;
		y: for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				try {
					Thread.currentThread().sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} else {
				// success
				break y;
			}
		}
		if (cacheClearAttempts > 100) {
			System.out.println("ERROR: Cache could not be cleared");
			addLogMessage(errorLine);
			addLogMessage("Cache could not be cleared...");
			addLogMessage(errorLine);
			return;
		}

		// put files into cache
		int copyAttempts = 0;
		x: for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				copyFolder(file, cache);
				System.out.println("Copy Folder took " + copyAttempts + " attempts to succeed.");
				break x;
			} catch (FileSystemException e) {
				if (copyAttempts == 0) {
					e.printStackTrace();
					// System.out.println("ERROR unable to copy directory:\n " +
					// e + "\n " + e.getMessage() + "\n "
					// + e.getLocalizedMessage() + "\n " + e.getCause());
				} else if (copyAttempts == 100) {
					e.printStackTrace();
					reportErrorEncounter();
					addLogMessage(errorLine);
					addLogMessage("ERROR unable to copy directory:\n    " + e + "\n    " + e.getMessage() + "\n    "
							+ e.getLocalizedMessage() + "\n    " + e.getCause());
					addLogMessage(errorLine);
				}
				// sleep and hope the file gets released soon
				try {
					Thread.currentThread().sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
				reportErrorEncounter();
				addLogMessage(errorLine);
				addLogMessage("ERROR unable to copy directory:\n    " + e + "\n    " + e.getMessage() + "\n    "
						+ e.getLocalizedMessage() + "\n    " + e.getCause());
				addLogMessage(errorLine);
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			return;
		}

		// do stuff
		System.out.println("retrieving componentList");
		File componentListFile = mpqi.getComponentListFile();
		System.out.println("retrieving descIndex - set path and clear");
		try {
			descIndex.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			reportErrorEncounter();
			addLogMessage(errorLine);
			addLogMessage("ERROR unable to read DescIndex path:\n    " + e + "\n    " + e.getMessage() + "\n    "
					+ e.getLocalizedMessage() + "\n    " + e.getCause());
			addLogMessage(errorLine);
		}

		System.out.println("retrieving descIndex - get cached file");
		File descIndexFile = mpqi.getCachedFile(descIndex.getDescIndexIntPath());
		System.out.println("adding layouts from descIndexFile: " + descIndexFile.getAbsolutePath());
		try {
			descIndex.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile));
		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace();
			reportErrorEncounter();
			addLogMessage(errorLine);
			addLogMessage("ERROR unable to read Layout paths:\n    " + e + "\n    " + e.getMessage() + "\n    "
					+ e.getLocalizedMessage() + "\n    " + e.getCause());
			addLogMessage(errorLine);
		}

		addLogMessage("Compiling... " + file.getName());

		// perform checks/improvements on code
		compile();

		addLogMessage("Building... " + file.getName());

		try {
			boolean protectMPQ = isHeroes ? settings.isHeroesProtectMPQ() : settings.isSC2ProtectMPQ();
			mpqi.buildMpq(buildPath, file.getName(), protectMPQ, settings.isBuildUnprotectedToo());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			reportErrorEncounter();
			addLogMessage(errorLine);
			addLogMessage("ERROR unable to construct final Interface file:\n    " + e + "\n    " + e.getMessage()
					+ "\n    " + e.getLocalizedMessage() + "\n    " + e.getCause());
			addLogMessage(errorLine);
		} catch (Exception e1) {
			System.out.println("ERROR: caught the following unexpected Exception...");
			reportErrorEncounter();
			addLogMessage(errorLine);
			addLogMessage("ERROR unable to construct final Interface file:\n    " + e1 + "\n    " + e1.getMessage()
					+ "\n    " + e1.getLocalizedMessage() + "\n    " + e1.getCause());
			addLogMessage(errorLine);
			e1.printStackTrace();
		}
	}

	/**
	 * Returns the Mpq Interface.
	 * 
	 * @return
	 */
	public MpqInterface getMpqInterface() {
		return mpqi;
	}

	/**
	 * Is called when the App is closing.
	 */
	public void stop() {
		System.out.println("App is about to shut down.");
		mpqi.clearCacheExtractedMpq();
		System.out.println("App waves Goodbye!");
	}

	/**
	 * Initializes the MPQ Interface.
	 * 
	 * @param mpqi
	 */
	private void initMpqInterface(MpqInterface mpqi) {

		mpqi.setMpqEditorPath(basePath.getParent() + File.separator + "tools" + File.separator + "plugins"
				+ File.separator + "mpq" + File.separator + "MPQEditor.exe");

	}

	/**
	 * Initializes the Settings File Interface.
	 * 
	 * @param settings
	 */
	private void initSettings(SettingsInterface settings) {
		settings.setSettingsFilePath(basePath.getParent() + File.separator + "settings.ini");
		try {
			settings.readSettingsFromFile();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Settings file could not be found.");
			e.printStackTrace();
		}
	}

	/**
	 * Returns true, if it belongs to Heroes of the Storm, false otherwise.
	 * 
	 * @return
	 */
	public boolean isHeroesFile() {
		return namespaceHeroes;
	}

	/**
	 * Returns the DescIndex data of the opened document.
	 * 
	 * @return
	 */
	public DescIndexData getDescIndexData() {
		return descIndex;
	}

	/**
	 * Compiles and updates the data in the cache.
	 */
	public void compile() {
		try {
			// manage order of layout files in DescIndex
			descIndex.orderLayoutFiles();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			addLogMessage(errorLine);
			reportErrorEncounter();
			addLogMessage("ERROR compiling:\n    " + e + "\n    " + e.getMessage() + "\n    " + e.getLocalizedMessage()
					+ "\n    " + e.getCause());
			addLogMessage(errorLine);
		}
		descIndex.persistDescIndexFile();
	}

	/**
	 * Copies a folder.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	private static void copyFolder(File source, File target) throws IOException {
		if (source.isDirectory()) {
			// create folder if not existing
			if (!target.exists()) {
				target.mkdir();
			}

			// copy all contained files recursively
			for (String file : source.list()) {
				File srcFile = new File(source, file);
				File destFile = new File(target, file);
				// Recursive function call
				copyFolder(srcFile, destFile);
			}
		} else {
			// copy the file
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * from stackoverflow because why doesn't java have this functionality? It's
	 * not like nobody would need that or it is trivial to create...
	 * 
	 * @param aclass
	 * @return
	 */
	public File getJarDir(Class<Main> aclass) {
		System.out.println("_FINDING JAR'S PATH");

		// ATTEMPT #1
		File f = new File(System.getProperty("java.class.path"));
		File dir = f.getAbsoluteFile().getParentFile();
		String str = dir.toString();
		System.out.println("Attempt#1 java.class.path: " + str);
		addLogMessage("Attempt#1 java.class.path: " + str);

		// check if started in eclipse
		if (str.contains("tools" + File.separator + "compiler" + File.separator + "project" + File.separator + "target"
				+ File.separator + "classes;")) {
			// get current working directory
			URI uri = new File(".").toURI();
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			// addLogMessage("METHOD TEST: " + new File(".").toURI());

			str = uri.getPath();
			System.out.println("_URI path:" + str);
			addLogMessage("_URI path:" + str);

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
				str += "/testEnv/baseUI/";
				System.out.println("_Assuming Test Environment: " + str);
				addLogMessage("_Assuming Test Environment: " + str);
			}

		}
		System.out.println("_RESULT PATH: " + str);
		addLogMessage("_RESULT PATH: " + str);

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

	public boolean hasEncounteredError() {
		return encounteredError;
	}

	public void reportErrorEncounter() {
		this.encounteredError = true;
	}

	private void clearErrorEncounter() {
		this.encounteredError = false;
	}
}
