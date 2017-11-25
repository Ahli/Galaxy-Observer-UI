package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.xml.sax.SAXException;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReader;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.galaxy.ui.UICatalog;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;

import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.util.ErrorTracker;
import application.util.ErrorTrackerImpl;
import application.util.JarHelper;
import application.util.logger.log4j2plugin.StylizedTextAreaAppender;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Interface Compiler and Builder Application.
 * 
 * @author Ahli
 * 
 */
public final class Main extends Application {
	
	private static Logger logger = LogManager.getLogger(Main.class);
	private ThreadPoolExecutor executor;
	
	// Components
	private MpqEditorInterface baseMpqInterface = null;
	private SettingsIniInterface settings = null;
	private final ReplayFinder replayFinder = new ReplayFinder();
	private final ErrorTracker errorTracker = new ErrorTrackerImpl();
	private final CompileManager compileManager = new CompileManager(errorTracker);
	
	// data
	private final String documentsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	private File basePath = null;
	private final GameData gameSC2 = new GameData(new SC2GameDef());
	private final GameData gameHeroes = new GameData(new HeroesGameDef());
	
	// GUI
	private StyleClassedTextArea txtArea = null;
	private TabPane tabPane = null;
	
	// performance
	private static long startTime;
	
	// Command Line Parameter
	private boolean hasParamCompilePath = false;
	private String paramCompilePath = null;
	private boolean compileAndRun = false;
	private String paramRunPath = null;
	
	/**
	 * Entry point of the App.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		startTime = System.currentTimeMillis();
		logger.trace("trace log visible");
		logger.debug("debug log visible");
		logger.info("info log visible");
		logger.warn("warn log visible");
		logger.error("error log visible");
		logger.fatal("fatal log visible");
		
		logger.trace("Configuration File of System: " + System.getProperty("log4j.configurationFile"));
		logger.info("Launch arguments: " + Arrays.toString(args));
		
		launch(args);
	}
	
	/**
	 * Called when the App is initializing.
	 */
	@Override
	public void start(final Stage primaryStage) {
		Thread.currentThread().setName("UI");
		
		initThreadPool();
		
		initParams();
		
		initGUI(primaryStage);
		
		initPaths();
		
		printVariables();
		
		initSettings(null);
		
		initMpqInterface();
		
		// compile interfaces and run game
		executeWorkPipeline(primaryStage);
	}
	
	/**
	 * Initializes the ThreadPool.
	 */
	private void initThreadPool() {
		final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		logger.info("detected processor count: " + numberOfProcessors);
		// test sleepytasks:
		// durations/thread# for my 4cpu/8threads: 18, 14, 12, 15, 15, 17, 16, 19, .,14
		// test new:
		// durations/thread# for my 4cpu/8threads: 15, 12, 15, 15, 15, 15, 14, 14, .,14
		// after frame attribute cloning fix:
		// durations/thread# for my 4cpu/8threads: 16, 13, 16, 16
		// after memory optimizations:
		// durations/thread# for my 4cpu/8threads: 13, 11, 11, 10, 10, 10, 10, 10
		
		// test with proper compression of mpqeditor
		// durations/thread# for my 4cpu/8threads: 16, 13, 12, 12, 12,... 12
		
		final int maxThreads = Math.max(1, Math.min(numberOfProcessors, 3));
		executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), runnable -> {
					final Thread t = new Thread(runnable);
					t.setDaemon(true);
					t.setName(t.getName().replaceFirst("Thread", "W"));
					final String title = t.getName();
					addThreadLoggerTab(t.getId(), title);
					return t;
				});
		executor.allowCoreThreadTimeOut(true);
	}
	
	/**
	 * Returns the path the a game's base UI folder.
	 * 
	 * @param gameDef
	 *            the game
	 * @return path to the baseUI folder of the specified game
	 */
	private String getBaseUiPath(final GameDef gameDef) {
		return basePath.getParent() + File.separator + "baseUI" + File.separator + gameDef.getNameHandle();
	}
	
	/**
	 * Starts the Application's work thread.
	 * 
	 * @param primaryStage
	 *            app's main stage
	 */
	private void executeWorkPipeline(final Stage primaryStage) {
		final String path = hasParamCompilePath ? paramCompilePath : basePath.getAbsolutePath();
		
		final boolean workHeroes = pathContainsCompileableForGame(path, gameHeroes);
		final boolean workSC2 = pathContainsCompileableForGame(path, gameSC2);
		
		if (workHeroes) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					buildGamesInterfaceFiles(gameHeroes, path);
				}
			});
		}
		
		if (workSC2) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					buildGamesInterfaceFiles(gameSC2, path);
				}
			});
		}
		
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setName("TimeKeeper");
				try {
					while (executor.getQueue().size() > 0 || executor.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					setStageTitle("Compiling completed.", primaryStage);
					startReplayOrQuitOrShowError(primaryStage);
					executor.shutdown();
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					final long executionTime = (System.currentTimeMillis() - startTime);
					logger.info("Execution time: " + String.format("%d min, %d sec",
							TimeUnit.MILLISECONDS.toMinutes(executionTime),
							TimeUnit.MILLISECONDS.toSeconds(executionTime)
									- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))));
				} catch (final InterruptedException e) {
					logger.error("ERROR: execution time measuring thread was interrupted.");
					errorTracker.reportErrorEncounter(e);
				}
			}
		}.start();
		
	}
	
	/**
	 * 
	 * @param gameHeroes2
	 * @param path
	 */
	private void buildGamesInterfaceFiles(final GameData game, final String path) {
		printLogMessage("Starting to parse base " + game.getGameDef().getName() + " UI.");
		parseDefaultUI(game);
		try {
			if (hasParamCompilePath) {
				buildSpecificUI(new File(path), game);
			} else {
				buildGamesUIs(game.getGameDef().getNameHandle(), game);
			}
		} catch (final IOException e) {
			logger.error("ERROR while building a UI for " + game.getGameDef().getName() + ": " + e);
			e.printStackTrace();
		}
		printLogMessage("Finished constructing UIs for " + game.getGameDef().getName() + ".");
	}
	
	/**
	 * Returns whether the specified path contains a compilable file for the
	 * specified game.
	 * 
	 * @param path
	 * @param game
	 * @return
	 */
	private boolean pathContainsCompileableForGame(final String path, final GameData game) {
		
		// final String fileFilter = "DescIndex" +
		// game.getGameDef().getLayoutFileEnding();
		// final Collection<File> descIndexFiles = FileUtils.listFiles(new File(path),
		// new WildcardFileFilter(fileFilter),
		// TrueFileFilter.INSTANCE);
		// return !descIndexFiles.isEmpty();
		
		final String[] extensions = new String[1];
		extensions[0] = game.getGameDef().getLayoutFileEnding();
		final IOFileFilter filter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
		final Iterator<File> iter = FileUtils.iterateFiles(new File(path), filter, TrueFileFilter.INSTANCE);
		
		while (iter.hasNext()) {
			if (iter.next().isFile()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Parses the default UI of the specified game.
	 * 
	 * @param game
	 *            game whose default UI is parsed
	 */
	private void parseDefaultUI(final GameData game) {
		final UICatalog uiCatalog = game.getUiCatalog();
		final String gameDir = getBaseUiPath(game.getGameDef()) + File.separator
				+ game.getGameDef().getModsSubDirectory();
		try {
			
			for (final String modOrDir : game.getGameDef().getCoreModsOrDirectories()) {
				
				final File directory = new File(gameDir + File.separator + modOrDir);
				
				final Collection<File> descIndexFiles = FileUtils.listFiles(directory,
						new WildcardFileFilter("DescIndex.*Layout"), TrueFileFilter.INSTANCE);
				logger.info("number of descIndexFiles found: " + descIndexFiles.size());
				
				for (final File descIndexFile : descIndexFiles) {
					logger.info("parsing descIndexFile '" + descIndexFile.getPath() + "'");
					uiCatalog.processDescIndex(descIndexFile, game.getGameDef().getDefaultRaceId());
				}
			}
			logger.info("Finished parsing base UI for " + game.getGameDef().getName());
			// } catch (ParserConfigurationException | SAXException | IOException |
			// UIException e) {
		} catch (final Exception e) {
			logger.error("ERROR parsing base UI catalog for '" + game.getGameDef().getName() + "'.", e);
			e.printStackTrace();
			errorTracker.reportErrorEncounter(e);
		}
	}
	
	/**
	 * Sets title of stage.
	 * 
	 * @param title
	 *            the new title
	 * @param stage
	 *            stage receiving the title
	 */
	private void setStageTitle(final String title, final Stage stage) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setTitle(title);
			}
		});
	}
	
	/**
	 * Starts a Replay, quits the Application or remains alive to show an error.
	 * Action depends on fields.
	 *
	 * @param primaryStage
	 *            app's main stage
	 */
	private void startReplayOrQuitOrShowError(final Stage primaryStage) {
		if (!errorTracker.hasEncounteredError()) {
			// start game, launch replay
			attemptToRunGameWithReplay(paramRunPath, compileAndRun, paramCompilePath);
			if (!hasParamCompilePath) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// close after 5 seconds, if compiled all and no errors
						final PauseTransition delay = new PauseTransition(Duration.seconds(5));
						delay.setOnFinished(event -> primaryStage.close());
						delay.play();
					}
				});
			} else if (hasParamCompilePath || paramRunPath != null) {
				// close instantly, if compiled special and no errors
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						primaryStage.close();
					}
					
				});
			}
		}
	}
	
	/**
	 * Attempt to run the Game with the newest Replay file.
	 *
	 * @param paramRunPath
	 *            path to the replay file.
	 * @param compileAndRun
	 *            setting whether it compiles and runs or only runs
	 * @param paramCompilePath
	 *            compile path
	 */
	private void attemptToRunGameWithReplay(final String paramRunPath, final boolean compileAndRun,
			final String paramCompilePath) {
		
		executor.execute(new Runnable() {
			@Override
			public void run() {
				// manage thread
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
				printLogMessage("Game location: " + gamePath);
				
				final File replay = replayFinder.getLastUsedOrNewestReplay(isHeroes, documentsPath);
				if (replay != null && replay.exists() && replay.isFile()) {
					logger.info("Starting game with replay: " + replay.getName());
					final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"";
					logger.trace("executing: " + cmd);
					try {
						Runtime.getRuntime().exec(cmd);
						logger.trace("after Start attempt...");
					} catch (final IOException e) {
						e.printStackTrace();
						errorTracker.reportErrorEncounter(e);
					}
				} else {
					logger.error("Failed to find any replay.");
				}
				printLogMessage("Finished to start the game with a replay.");
			}
		});
		
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables() {
		logger.info("basePath: " + basePath);
		logger.info("documentsPath: " + documentsPath);
		if (paramCompilePath != null) {
			logger.info("compile param path: " + paramCompilePath);
			if (compileAndRun) {
				logger.info("run after compile: " + compileAndRun);
			}
		}
		if (paramRunPath != null) {
			logger.info("run param path: " + paramRunPath);
		}
	}
	
	/**
	 * Turns App's parameters into variables.
	 */
	private void initParams() {
		// named params
		// e.g. "--paramname=value".
		final Parameters params = getParameters();
		final Map<String, String> namedParams = params.getNamed();
		
		// COMPILE / COMPILERUN PARAM
		// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
		paramCompilePath = namedParams.get("compile");
		compileAndRun = false;
		if (namedParams.get("compileRun") != null) {
			paramCompilePath = namedParams.get("compileRun");
			compileAndRun = true;
		}
		paramCompilePath = getInterfaceRootFromPath(paramCompilePath);
		hasParamCompilePath = (paramCompilePath != null);
		
		// RUN PARAM
		// --run="F:\Games\Heroes of the Storm\Support\HeroesSwitcher.exe"
		paramRunPath = namedParams.get("run");
	}
	
	/**
	 * Initialize Paths.
	 */
	private void initPaths() {
		basePath = JarHelper.getJarDir(Main.class);
	}
	
	/**
	 * Initialize GUI.
	 * 
	 * @param primaryStage
	 *            the main App's Stage
	 */
	private void initGUI(final Stage primaryStage) {
		// Build UI
		final BorderPane root = new BorderPane();
		
		txtArea = new StyleClassedTextArea();
		txtArea.setEditable(false);
		// log4j2 can print into GUI
		StylizedTextAreaAppender.setTextArea(txtArea);
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(txtArea);
		
		final Scene scene = new Scene(root, 1200, 400);
		scene.getStylesheets().add(Main.class.getResource("view/application.css").toExternalForm()); //$NON-NLS-1$
		scene.getStylesheets().add(Main.class.getResource("view/textStyles.css").toExternalForm()); //$NON-NLS-1$
		
		final FXMLLoader loader = new FXMLLoader();
		// loader.setResources(Messages.getBundle());
		try (InputStream is = Main.class.getResourceAsStream("view/TabsLayout.fxml");) {
			tabPane = (TabPane) loader.load(is); // $NON-NLS-1$
		} catch (final IOException e) {
			logger.error("failed to load TabsLayout.fxml");
			e.printStackTrace();
			errorTracker.reportErrorEncounter(e);
		}
		root.setCenter(tabPane);
		final ObservableList<Tab> tabs = tabPane.getTabs();
		tabs.get(0).setContent(virtualizedScrollPane);
		
		try {
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		} catch (final Exception e) {
			logger.error("Failed to load ahli.png");
			errorTracker.reportErrorEncounter(e);
		}
		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("Compiling Interfaces...");
		printLogMessage("Initializing App...");
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 * 
	 * @param threadId
	 */
	private void addThreadLoggerTab(final long threadId, final String tabTitle) {
		final Tab newTab = new Tab();
		final StyleClassedTextArea newTxtArea = new StyleClassedTextArea();
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(
				newTxtArea);
		newTab.setContent(virtualizedScrollPane);
		StylizedTextAreaAppender.setSpecialTextArea(newTxtArea, threadId);
		newTab.setText(tabTitle);
		newTxtArea.setEditable(false);
		// runlater needs to appear below the edits above, else it might be added before
		// resulting in UI edits not in UI thread
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				tabPane.getTabs().add(newTab);
			}
		});
	}
	
	/**
	 * ParamPath might be some layout or folder within the interface, so it is cut
	 * down to the interface base path.
	 * 
	 * @param paramPath
	 *            application's compileParam's value
	 * @return shortens the path to the Interface root folder
	 */
	private String getInterfaceRootFromPath(final String paramPath) {
		String str = paramPath;
		if (str != null) {
			while (str.length() > 0 && !str.endsWith("Interface")) {
				// logger.trace("cutting progress: " + str);
				final int lastIndex = str.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					str = str.substring(0, lastIndex);
				} else {
					// logger.trace("LastIndex is -1 => null compile path");
					return null;
				}
			}
		}
		logger.trace("cutting result: " + str);
		return str;
	}
	
	/**
	 * Build all Interfaces for a game's subdirectory.
	 * 
	 * @param subfolderName
	 *            name from the base path to the directory whose interfaces are
	 *            built.
	 * @param gameData
	 *            the game information
	 * @throws IOException
	 *             when something goes wrong
	 */
	public void buildGamesUIs(final String subfolderName, final GameData gameData) throws IOException {
		final File dir = new File(basePath.getAbsolutePath() + File.separator + subfolderName);
		if (dir.exists() && dir.isDirectory()) {
			final File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (final File child : directoryListing) {
					buildSpecificUI(child, gameData);
				}
			}
		}
		
	}
	
	/**
	 * Builds a specific Interface in a Build Thread.
	 * 
	 * @param interfaceFolder
	 *            folder of the interface file to be built
	 * @param game
	 *            the game information about the file
	 */
	public void buildSpecificUI(final File interfaceFolder, final GameData game) {
		if (interfaceFolder.isDirectory()) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					// create unique cache path
					final MpqEditorInterface threadsMpqInterface = (MpqEditorInterface) baseMpqInterface.clone();
					final long threadId = Thread.currentThread().getId();
					threadsMpqInterface.setMpqCachePath(baseMpqInterface.getMpqCachePath() + threadId);
					
					// work
					try {
						buildFile(interfaceFolder, game, threadsMpqInterface);
						threadsMpqInterface.clearCacheExtractedMpq();
					} catch (final IOException e) {
						logger.error("IOException while building UIs", e);
						errorTracker.reportErrorEncounter();
						e.printStackTrace();
					}
				}
			});
		} else {
			logger.error("Can't build UI from file '" + interfaceFolder + "', expected a directory.");
		}
	}
	
	/**
	 * Prints a message to the message log.
	 * 
	 * @param msg
	 *            the message
	 */
	public void printLogMessage(final String msg) {
		logger.info(msg);
	}
	
	/**
	 * Builds MPQ Archive File. Run this in its own thread!
	 * 
	 * Conditions: - Specified MpqInterface requires a unique cache path for
	 * multithreading.
	 * 
	 * @param sourceFile
	 *            folder location
	 * @param game
	 *            set to true, if Heroes
	 * @param mpqi
	 *            MpqInterface with unique cache path
	 * @throws IOException
	 *             when something goes wrong
	 */
	private void buildFile(final File sourceFile, final GameData game, final MpqEditorInterface mpqi)
			throws IOException {
		String targetPath = documentsPath + File.separator;
		targetPath += game.getGameDef().getDocumentsGameDirectoryName();
		targetPath += File.separator + game.getGameDef().getDocumentsInterfaceSubdirectoryName();
		
		// do stuff
		final ModData mod = new ModData(game);
		mod.setSourcePath(sourceFile);
		mod.setTargetPath(new File(targetPath));
		
		// get and create cache
		final File cache = new File(mpqi.getMpqCachePath());
		if (!cache.exists() && !cache.mkdirs()) {
			errorTracker.reportErrorEncounter();
			final String msg = "Unable to create cache directory.";
			logger.error(msg);
			throw new IOException(msg);
		}
		int cacheClearAttempts = 0;
		y: for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e1) {
					e1.printStackTrace();
					errorTracker.reportErrorEncounter(e1);
				}
			} else {
				// success
				break y;
			}
		}
		if (cacheClearAttempts > 100) {
			final String msg = "Cache could not be cleared";
			logger.error(msg);
			return;
		}
		mod.setMpqCachePath(cache);
		// put files into cache
		int copyAttempts = 0;
		x: for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				copyFileOrFolder(sourceFile, cache);
				logger.trace("Copy Folder took " + copyAttempts + " attempts to succeed.");
				break x;
			} catch (final FileSystemException e) {
				if (copyAttempts == 0) {
					logger.warn("Attempt to copy directory failed.", e);
				} else if (copyAttempts >= 100) {
					errorTracker.reportErrorEncounter();
					final String msg = "Unable to copy directory after 100 copy attempts: " + e.getMessage();
					logger.error(msg, e);
					e.printStackTrace();
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e1) {
					logger.error("Thread sleep was interrupted with exception.", e);
					errorTracker.reportErrorEncounter(e);
				}
			} catch (final IOException e) {
				errorTracker.reportErrorEncounter();
				final String msg = "Unable to copy directory";
				logger.error(msg, e);
				errorTracker.reportErrorEncounter(e);
				e.printStackTrace();
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Above code did not throw exception about copy attempt threshold reached.";
			logger.error(msg);
			errorTracker.reportErrorEncounter();
			throw new IOException(msg);
		}
		
		logger.trace("retrieving componentList");
		final File componentListFile = mpqi.getComponentListFile();
		mod.setComponentListFile(componentListFile);
		logger.trace("retrieving descIndex - set path and clear");
		
		final DescIndexData descIndexData = new DescIndexData(mpqi);
		mod.setDescIndexData(descIndexData);
		
		try {
			descIndexData.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path.";
			logger.error(msg, e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
			throw new IOException(msg, e);
		}
		
		logger.trace("retrieving descIndex - get cached file");
		final File descIndexFile = mpqi.getFileFromMpq(descIndexData.getDescIndexIntPath());
		logger.trace("adding layouts from descIndexFile: " + descIndexFile.getAbsolutePath());
		try {
			descIndexData.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, false));
		} catch (SAXException | ParserConfigurationException | IOException | MpqException e) {
			logger.error("unable to read Layout paths", e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
		}
		
		logger.info("Compiling... " + sourceFile.getName());
		
		// perform checks/improvements on code
		compileManager.compile(mod, "Terr");
		
		logger.info("Building... " + sourceFile.getName());
		
		try {
			final boolean protectMPQ = game.getGameDef() instanceof HeroesGameDef ? settings.isHeroesProtectMPQ()
					: settings.isSC2ProtectMPQ();
			mpqi.buildMpq(targetPath, sourceFile.getName(), protectMPQ, settings.isBuildUnprotectedToo());
			logger.info("Finished building... " + sourceFile.getName());
		} catch (IOException | InterruptedException e) {
			logger.error("ERROR: unable to construct final Interface file", e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
		} catch (final Exception e1) {
			logger.error("ERROR: caught an unexpected Exception", e1);
			errorTracker.reportErrorEncounter(e1);
			e1.printStackTrace();
		}
	}
	
	/**
	 * Is called when the App is closing.
	 */
	@Override
	public void stop() {
		logger.info("App is about to shut down.");
		baseMpqInterface.clearCacheExtractedMpq();
		executor.shutdown();
		try {
			if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
				logger.info("App waves Goodbye!");
			} else {
				logger.error("ERROR: Executor timed out waiting for termination.");
			}
		} catch (final InterruptedException e) {
			logger.error("ERROR while shutting down executor.", e);
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Initializes the MPQ Interface.
	 */
	private void initMpqInterface() {
		final String tempDirectory = System.getProperty("java.io.tmpdir");
		final String cachePath = tempDirectory + "ObserverInterfaceBuilder" + File.separator + "_ExtractedMpq";
		baseMpqInterface = new MpqEditorInterface(cachePath);
		baseMpqInterface.setMpqEditorPath(basePath.getParent() + File.separator + "tools" + File.separator + "plugins"
				+ File.separator + "mpq" + File.separator + "MPQEditor.exe");
	}
	
	/**
	 * Initializes the Settings File Interface. It either uses the specified
	 * SettingsIniInterface or loads them from the default location.
	 * 
	 * @param optionalSettingsToLoad
	 *            optional SettingsIniInterface, set to null to load from default
	 *            location
	 */
	private void initSettings(final SettingsIniInterface optionalSettingsToLoad) {
		SettingsIniInterface settings;
		if (optionalSettingsToLoad == null) {
			final String settingsFilePath = basePath.getParent() + File.separator + "settings.ini";
			settings = new SettingsIniInterface(settingsFilePath);
		} else {
			settings = optionalSettingsToLoad;
		}
		try {
			settings.readSettingsFromFile();
		} catch (final FileNotFoundException e) {
			logger.error("settings file could not be found", e);
			errorTracker.reportErrorEncounter(e);
			e.printStackTrace();
		}
		this.settings = settings;
	}
	
	/**
	 * Copies a file or folder.
	 * 
	 * @param source
	 *            source location
	 * @param target
	 *            target location
	 * @throws IOException
	 *             when something goes wrong
	 */
	private static void copyFileOrFolder(final File source, final File target) throws IOException {
		if (source.isDirectory()) {
			// create folder if not existing
			if (!target.exists() && !target.mkdir()) {
				final String msg = "Could not create directory " + target.getAbsolutePath();
				logger.error(msg);
				throw new IOException(msg);
			}
			
			// copy all contained files recursively
			final String[] fileList = source.list();
			if (fileList == null) {
				final String msg = "Source directory's files returned null";
				logger.error(msg);
				throw new IOException(msg);
			}
			for (final String file : fileList) {
				final File srcFile = new File(source, file);
				final File destFile = new File(target, file);
				// Recursive function call
				copyFileOrFolder(srcFile, destFile);
			}
		} else {
			// copy the file
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
}
