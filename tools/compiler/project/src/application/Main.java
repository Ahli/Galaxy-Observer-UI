package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.ahli.galaxy.ComponentsListReader;
import com.ahli.galaxy.DescIndexData;
import com.ahli.galaxy.DescIndexReader;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;

import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.util.ErrorTracker;
import application.util.ErrorTrackerImpl;
import application.util.JarHelper;
import application.util.logger.log4j2plugin.TextAreaAppender;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 * @author Ahli
 * 
 */
public class Main extends Application {
	static Logger LOGGER = LogManager.getLogger(Main.class);
	
	// Components
	private MpqEditorInterface baseMpqInterface = new MpqEditorInterface();
	private SettingsIniInterface settings = new SettingsIniInterface();
	private ReplayFinder replayFinder = new ReplayFinder();
	private ErrorTracker errorTracker = new ErrorTrackerImpl();
	private CompileManager compileManager = new CompileManager(errorTracker);
	
	// data
	private boolean namespaceHeroes = true;
	private String documentsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	private File basePath = null;
	
	// GUI
	private TextArea txtArea = null;
	
	// performance
	private static long startTime;
	
	// Builder
	private HashMap<Integer, Thread> builders = new HashMap<>();
	private int nextFreeBuilderID = 0;
	
	// Command Line Parameter
	private boolean hasParamCompilePath = false;
	private String paramCompilePath = null;
	private boolean compileAndRun = false;
	private String paramRunPath = null;
	
	/**
	 * Entry point of the App.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		LOGGER.trace("trace log visible");
		LOGGER.debug("debug log visible");
		LOGGER.info("info log visible");
		LOGGER.warn("warn log visible");
		LOGGER.error("error log visible");
		LOGGER.fatal("fatal log visible");
		
		LOGGER.trace("Configuration File of System: " + System.getProperty("log4j.configurationFile"));
		LOGGER.info("Launch arguments: " + Arrays.toString(args));
		
		launch(args);
	}
	
	@Override
	/**
	 * Called when the App is initializing.
	 */
	public void start(Stage primaryStage) {
		// shorter thread name for application
		Thread.currentThread().setName("UI");
		
		initParams();
		
		initGUI(primaryStage);
		
		initPaths();
		
		initSettings(settings);
		
		printVariables();
		
		initMpqInterface(baseMpqInterface);
		
		// // TEST DefaultUICatalog
		// String baseUIpath = basePath.getParent() + File.separator + "baseUI";
		// LOGGER.info("BaseUI path: " + baseUIpath);
		// // UICatalog catalogSC2 = new UICatalog();
		// UICatalog catalogHeroes = new UICatalog();
		//
		// new Thread() {
		// public void run() {
		// try {
		// // catalogSC2.processDescIndex(new File(baseUIpath +
		// // File.separator + "sc2" + File.separator + "mods"
		// // + File.separator + "core.sc2mod" + File.separator +
		// // "base.sc2data" + File.separator + "UI"
		// // + File.separator + "Layout" + File.separator +
		// // "DescIndex.SC2Layout"));
		// catalogHeroes.processDescIndex(
		// new File(baseUIpath + File.separator + "heroes" + File.separator +
		// "mods" + File.separator
		// + "core.stormmod" + File.separator + "base.stormdata" +
		// File.separator + "UI"
		// + File.separator + "Layout" + File.separator +
		// "DescIndex.StormLayout"));
		// catalogHeroes.processDescIndex(
		// new File(basePath.getAbsolutePath() + File.separator + "heroes" +
		// File.separator
		// + "AhliObs.StormInterface" + File.separator + "Base.StormData" +
		// File.separator
		// + "UI" + File.separator + "Layout" + File.separator +
		// "DescIndex.StormLayout"));
		// } catch (ParserConfigurationException | SAXException | IOException e)
		// {
		// LOGGER.error("Error parsing base UI catalog due to a technical
		// problem.", e);
		// e.printStackTrace();
		// } catch (UIException e) {
		// LOGGER.error("Error parsing base UI due to a logical problem.", e);
		// e.printStackTrace();
		// }
		// }
		// }.start();
		// if (true)
		// return;
		
		// WORK WORK WORK
		if (!hasParamCompilePath) {
			// compile all
			try {
				buildGamesUIs("heroes", true);
				buildGamesUIs("sc2", false);
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("IOException while building UIs", e);
				errorTracker.reportErrorEncounter(e);
			}
		} else {
			// build specific file due to param
			boolean isHeroes = paramCompilePath.contains("heroes" + File.separator);
			try {
				buildSpecificUI(new File(paramCompilePath), isHeroes);
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("IOException while building UIs", e);
				errorTracker.reportErrorEncounter(e);
			}
		}
		
		// wait for all threads to finish
		for (Thread buildThread : builders.values()) {
			try {
				buildThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOGGER.error("waiting for build thread to join failed", e);
			}
		}
		primaryStage.setTitle("Compiling Interfaces... done.");
		printLogMessage("All done.");
		
		if (!errorTracker.hasEncounteredError()) {
			// start game, launch replay
			attemptToRunGameWithReplay(paramRunPath, compileAndRun, paramCompilePath);
		}
		
		if (!errorTracker.hasEncounteredError() && !hasParamCompilePath) {
			// close after 5 seconds, if compiled all and no errors
			PauseTransition delay = new PauseTransition(Duration.seconds(5));
			delay.setOnFinished(event -> primaryStage.close());
			delay.play();
		} else if (!errorTracker.hasEncounteredError() && (hasParamCompilePath || paramRunPath != null)) {
			// close instantly, if compiled special and no errors
			primaryStage.close();
		}
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables() {
		LOGGER.info("basePath: " + basePath);
		LOGGER.info("documentsPath: " + documentsPath);
		if (paramCompilePath != null) {
			LOGGER.info("compile param path: " + paramCompilePath);
			if (compileAndRun) {
				LOGGER.info("run after compile: " + compileAndRun);
			}
		}
		if (paramRunPath != null) {
			LOGGER.info("run param path: " + paramRunPath);
		}
	}
	
	/**
	 * Returns an unused thread ID.
	 * 
	 * @return
	 */
	private int getUnusedThreadID() {
		// return val, then increment
		return nextFreeBuilderID++;
	}
	
	/**
	 * Turns App's parameters into variables.
	 */
	private void initParams() {
		// named params
		// e.g. "--paramname=value".
		Parameters params = this.getParameters();
		Map<String, String> namedParams = params.getNamed();
		
		// COMPILE / COMPILERUN PARAM
		// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
		paramCompilePath = namedParams.get("compile");
		compileAndRun = false;
		if (namedParams.get("compileRun") != null) {
			paramCompilePath = namedParams.get("compileRun");
			compileAndRun = true;
		}
		paramCompilePath = cutCompileParamPath(paramCompilePath);
		hasParamCompilePath = (paramCompilePath != null);
		
		// RUN PARAM
		// --run="F:\Games\Heroes of the Storm\Support\HeroesSwitcher.exe"
		paramRunPath = namedParams.get("run");
	}
	
	/**
	 * Initialize Paths
	 */
	private void initPaths() {
		basePath = JarHelper.getJarDir(Main.class);
	}
	
	/**
	 * Initialize GUI
	 */
	private void initGUI(Stage primaryStage) {
		// Build UI
		BorderPane root = new BorderPane();
		txtArea = new TextArea();
		txtArea.setText("Initializing App...\n");
		// log4j2 can print into GUI
		TextAreaAppender.setTextArea(txtArea);
		root.setCenter(txtArea);
		Scene scene = new Scene(root, 1200, 400);
		scene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm()); //$NON-NLS-1$
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("Compiling Interfaces...");
		try {
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		} catch (Exception e) {
			LOGGER.error("Failed to load ahli.png");
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
		new Thread() {
			@Override
			public void run() {
				this.setName(this.getName().replaceFirst("Thread", "GameRunner"));
				boolean isHeroes = false;
				String gamePath = null;
				
				if (!compileAndRun) {
					// use the run param
					if (paramRunPath == null) {
						return;
					}
					gamePath = paramRunPath;
					isHeroes = gamePath.contains("HeroesSwitcher.exe");
				} else {
					// compileAndRun is active -> figure out the right game
					if (paramCompilePath.contains(File.separator + "heroes" + File.separator)) {
						// Heroes
						isHeroes = true;
						if (settings.isPtrActive()) {
							// PTR Heroes
							if (settings.isHeroesPtr64bit()) {
								gamePath = settings.getHeroesPtrPath() + File.separator + "Support64" + File.separator
										+ "HeroesSwitcher_x64.exe";
							} else {
								gamePath = settings.getHeroesPtrPath() + File.separator + "Support" + File.separator
										+ "HeroesSwitcher.exe";
							}
						} else {
							// live Heroes
							if (settings.isHeroes64bit()) {
								gamePath = settings.getHeroesPath() + File.separator + "Support64" + File.separator
										+ "HeroesSwitcher_x64.exe";
							} else {
								gamePath = settings.getHeroesPath() + File.separator + "Support" + File.separator
										+ "HeroesSwitcher.exe";
							}
						}
					} else {
						// SC2
						isHeroes = false;
						if (settings.isSC264bit()) {
							gamePath = settings.getSC2Path() + File.separator + "Support64" + File.separator
									+ "SC2Switcher_x64.exe";
						} else {
							gamePath = settings.getSC2Path() + File.separator + "Support" + File.separator
									+ "SC2Switcher.exe";
						}
					}
				}
				
				File replay = replayFinder.getLastOrNewestReplay(isHeroes, documentsPath);
				if (replay != null && replay.exists() && replay.isFile()) {
					LOGGER.info("Starting game with replay: " + replay.getName());
					String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"";
					LOGGER.debug("executing: " + cmd);
					try {
						Runtime.getRuntime().exec(cmd);
						LOGGER.debug("after Start attempt...");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					LOGGER.warn("Failed to find any replay.");
				}
			}
		}.start();
		
	}
	
	/**
	 * ParamPath might be some layout or folder within the interface, so we cut it
	 * down to the interface base path.
	 * 
	 * @param paramPath
	 * @return
	 */
	private String cutCompileParamPath(String paramPath) {
		String str = paramPath;
		if (str != null) {
			while (str.length() > 0 && !str.endsWith("Interface")) {
				LOGGER.debug("cutting progress: " + str);
				int lastIndex = str.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					str = str.substring(0, lastIndex);
				} else {
					LOGGER.debug("LastIndex is -1 => null compile path");
					return null;
				}
			}
		}
		LOGGER.debug("cutting result: " + str);
		return str;
	}
	
	/**
	 * Build all Interfaces for game subfolders.
	 * 
	 * @param subfolderName
	 * @param isHeroes
	 * @throws IOException
	 */
	public void buildGamesUIs(String subfolderName, boolean isHeroes) throws IOException {
		File dir = new File(basePath.getAbsolutePath() + File.separator + subfolderName);
		namespaceHeroes = isHeroes;
		if (dir.exists() && dir.isDirectory()) {
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					// build UI
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
	 * @throws IOException
	 */
	public void buildSpecificUI(File interfaceFolder, boolean isHeroes) throws IOException {
		if (interfaceFolder.isDirectory()) {
			// build according to param
			final int threadID = getUnusedThreadID();
			
			// create unique cache path
			final MpqEditorInterface threadsMpqInterface = (MpqEditorInterface) baseMpqInterface.clone();
			threadsMpqInterface.setMpqCachePath(baseMpqInterface.getMpqCachePath() + threadID);
			
			Thread buildThread = new Thread() {
				@Override
				public void run() {
					this.setName("Builder" + threadID);
					// work
					try {
						buildFile(interfaceFolder, isHeroes, threadsMpqInterface);
					} catch (IOException e) {
						LOGGER.error("IOException while building UIs", e);
						errorTracker.reportErrorEncounter();
						e.printStackTrace();
					}
				}
			};
			builders.put(threadID, buildThread);
			buildThread.start();
		}
	}
	
	/**
	 * Prints a message to the message log.
	 * 
	 * @param msg
	 */
	public void printLogMessage(String msg) {
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// console.print(msg + "\n");
				txtArea.appendText(msg + "\n");
			}
		});
	}
	
	/**
	 * Builds MPQ Archive File. Run this in its own thread!
	 * 
	 * Conditions: - Specified MpqInterface requires a unique cache path for
	 * multithreading.
	 * 
	 * @param file
	 *            folder location
	 * @param isHeroes
	 *            set to true, if Heroes
	 * @param mpqi
	 *            MpqInterface with unique cache path
	 * @throws IOException
	 */
	private void buildFile(File file, boolean isHeroes, MpqEditorInterface mpqi) throws IOException {
		String buildPath = documentsPath + File.separator;
		
		LOGGER.info("Starting to build file: " + file.getPath());
		
		if (isHeroes) {
			buildPath += "Heroes of the Storm";
		} else {
			buildPath += "StarCraft II";
		}
		buildPath += File.separator + "Interfaces";
		
		// get and create cache
		File cache = new File(mpqi.getMpqCachePath());
		if (!cache.exists() && !cache.mkdirs()) {
			errorTracker.reportErrorEncounter();
			String msg = "Unable to create cache directory.";
			LOGGER.error(msg);
			throw new IOException(msg);
		}
		
		int cacheClearAttempts = 0;
		y: for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} else {
				// success
				break y;
			}
		}
		if (cacheClearAttempts > 100) {
			String msg = "Cache could not be cleared";
			LOGGER.error(msg);
			return;
		}
		
		// put files into cache
		int copyAttempts = 0;
		x: for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				copyFolder(file, cache);
				LOGGER.debug("Copy Folder took " + copyAttempts + " attempts to succeed.");
				break x;
			} catch (FileSystemException e) {
				if (copyAttempts == 0) {
					LOGGER.warn("Attempt to copy directory failed.", e);
				} else if (copyAttempts >= 100) {
					errorTracker.reportErrorEncounter();
					String msg = "Unable to copy directory after 100 copy attempts: " + e.getMessage();
					LOGGER.error(msg, e);
					e.printStackTrace();
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					LOGGER.error("Thread sleep was interrupted with exception.", e);
				}
			} catch (IOException e) {
				errorTracker.reportErrorEncounter();
				String msg = "Unable to copy directory";
				LOGGER.error(msg, e);
				e.printStackTrace();
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			String msg = "Above code did not throw exception about copy attempt threshold reached.";
			LOGGER.error(msg);
			errorTracker.reportErrorEncounter();
			throw new IOException(msg);
		}
		
		// do stuff
		LOGGER.debug("retrieving componentList");
		File componentListFile = mpqi.getComponentListFile();
		LOGGER.debug("retrieving descIndex - set path and clear");
		
		DescIndexData descIndex = new DescIndexData(mpqi);
		
		try {
			descIndex.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.error("unable to read DescIndex path", e);
			errorTracker.reportErrorEncounter();
			e.printStackTrace();
		}
		
		LOGGER.debug("retrieving descIndex - get cached file");
		File descIndexFile = mpqi.getFileFromMpq(descIndex.getDescIndexIntPath());
		LOGGER.debug("adding layouts from descIndexFile: " + descIndexFile.getAbsolutePath());
		try {
			descIndex.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, false));
		} catch (SAXException | ParserConfigurationException | IOException | MpqException e) {
			LOGGER.error("unable to read Layout paths", e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
		}
		
		LOGGER.info("Compiling... " + file.getName());
		
		// perform checks/improvements on code
		compileManager.compile(descIndex);
		
		LOGGER.info("Building... " + file.getName());
		
		try {
			boolean protectMPQ = isHeroes ? settings.isHeroesProtectMPQ() : settings.isSC2ProtectMPQ();
			mpqi.buildMpq(buildPath, file.getName(), protectMPQ, settings.isBuildUnprotectedToo());
		} catch (IOException | InterruptedException e) {
			LOGGER.error("unable to construct final Interface file", e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
		} catch (Exception e1) {
			LOGGER.error("caught an unexpected Exception", e1);
			errorTracker.reportErrorEncounter(e1);
			e1.printStackTrace();
		}
	}
	
	/**
	 * Is called when the App is closing.
	 */
	@Override
	public void stop() {
		LOGGER.info("App is about to shut down.");
		baseMpqInterface.clearCacheExtractedMpq();
		LOGGER.info("App waves Goodbye!");
		long executionTime = (System.currentTimeMillis() - startTime);
		LOGGER.info("Execution time: " + String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(executionTime),
				TimeUnit.MILLISECONDS.toSeconds(executionTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))));
	}
	
	/**
	 * Initializes the MPQ Interface.
	 * 
	 * @param mpqi
	 */
	private void initMpqInterface(MpqEditorInterface mpqi) {
		
		mpqi.setMpqEditorPath(basePath.getParent() + File.separator + "tools" + File.separator + "plugins"
				+ File.separator + "mpq" + File.separator + "MPQEditor.exe");
	}
	
	/**
	 * Initializes the Settings File Interface.
	 * 
	 * @param settings
	 *            settings to initialize
	 */
	private void initSettings(SettingsIniInterface settings) {
		settings.setSettingsFilePath(basePath.getParent() + File.separator + "settings.ini");
		try {
			settings.readSettingsFromFile();
		} catch (FileNotFoundException e) {
			LOGGER.error("settings file could not be found", e);
			errorTracker.reportErrorEncounter(e);
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
	 * Copies a folder.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	private static void copyFolder(File source, File target) throws IOException {
		if (source.isDirectory()) {
			// create folder if not existing
			if (!target.exists() && !target.mkdir()) {
				String msg = "Could not create directory " + target.getAbsolutePath();
				LOGGER.error(msg);
				throw new IOException(msg);
			}
			
			// copy all contained files recursively
			String[] fileList = source.list();
			if (fileList == null) {
				String msg = "Source directory's files returned null";
				LOGGER.error(msg);
				throw new IOException(msg);
			}
			for (String file : fileList) {
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
	
}
