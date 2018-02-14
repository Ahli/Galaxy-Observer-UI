package application;

import application.controller.ErrorTabPaneController;
import application.controller.NavigationController;
import application.controller.TabPaneController;
import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.util.JarHelper;
import application.util.logger.log4j2plugin.StylizedTextAreaAppender;
import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReader;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Interface Compiler and Builder Application.
 *
 * @author Ahli
 */
public final class InterfaceBuilderApp extends Application {
	
	private static final Logger logger = LogManager.getLogger();
	private static long startTime;
	
	private static InterfaceBuilderApp instance = null;
	private final ReplayFinder replayFinder = new ReplayFinder();
	private final CompileManager compileManager = new CompileManager();
	private final String documentsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	private final GameData gameSC2 = new GameData(new SC2GameDef());
	private final List<ErrorTabPaneController> errorTabControllers = new ArrayList<>();
	private ThreadPoolExecutor executor;
	private MpqEditorInterface baseMpqInterface = null;
	private SettingsIniInterface settings = null;
	private File basePath = null;
	private GameData gameHeroes = new GameData(new HeroesGameDef());
	// Command Line Parameter
	private boolean hasParamCompilePath = false;
	private String paramCompilePath = null;
	private boolean compileAndRun = false;
	private String paramRunPath = null;
	private NavigationController navController;
	private boolean wasStartedWithParameters;
	
	/**
	 * Entry point of the App.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void main(final String[] args) {
		startTime = System.currentTimeMillis();
		logger.trace("trace log visible"); //$NON-NLS-1$
		logger.debug("debug log visible"); //$NON-NLS-1$
		logger.info("info log visible"); //$NON-NLS-1$
		logger.warn("warn log visible"); //$NON-NLS-1$
		logger.error("error log visible"); //$NON-NLS-1$
		logger.fatal("fatal log visible"); //$NON-NLS-1$
		
		logger.trace("Configuration File of System: {}",
				() -> System.getProperty("log4j.configurationFile")); //$NON-NLS-1$ //$NON-NLS-2$
		logger.info("Launch arguments: " + Arrays.toString(args)); //$NON-NLS-1$
		logger.info("Max Heap Space: " + Runtime.getRuntime().maxMemory() / 1048576L + "mb.");
		
		launch(args);
	}
	
	/**
	 * Copies a file or folder.
	 *
	 * @param source
	 * 		source location
	 * @param target
	 * 		target location
	 * @throws IOException
	 * 		when something goes wrong
	 */
	private static void copyFileOrFolder(final File source, final File target) throws IOException {
		if (source.isDirectory()) {
			// create folder if not existing
			if (!target.exists() && !target.mkdir()) {
				final String msg = "Could not create directory " + target.getAbsolutePath(); //$NON-NLS-1$
				logger.error(msg);
				throw new IOException(msg);
			}
			
			// copy all contained files recursively
			final String[] fileList = source.list();
			if (fileList == null) {
				final String msg = "Source directory's files returned null"; //$NON-NLS-1$
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
	
	/**
	 * Returns the instance of the builder App
	 *
	 * @return
	 */
	public static InterfaceBuilderApp getInstance() {
		return instance;
	}
	
	/**
	 * Called when the App is initializing.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		if (instance == null) {
			instance = this;
			logger.trace("Application singleton successfull.");
		} else {
			logger.error("Application cannot be started multiple times.");
			throw new ExceptionInInitializerError("Application cannot be started multiple times.");
		}
		
		Thread.currentThread().setName("UI"); //$NON-NLS-1$
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
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
		logger.info("detected processor count: " + numberOfProcessors); //$NON-NLS-1$
		
		final int maxThreads = Math.max(1, numberOfProcessors / 2);
		executor = new MyThreadPoolExecutor(maxThreads, maxThreads, 5000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(), new MyThreadFactory());
		executor.allowCoreThreadTimeOut(true);
	}
	
	/**
	 * Returns the path the a game's base UI folder.
	 *
	 * @param gameDef
	 * 		the game
	 * @return path to the baseUI folder of the specified game
	 */
	private String getBaseUiPath(final GameDef gameDef) {
		return basePath.getParent() + File.separator + "baseUI" + File.separator +
				gameDef.getNameHandle(); //$NON-NLS-1$
	}
	
	/**
	 * Starts the Application's work thread.
	 *
	 * @param primaryStage
	 * 		app's main stage
	 */
	private void executeWorkPipeline(final Stage primaryStage) {
		navController.lockNavToProgress();
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setName("Supervisor"); //$NON-NLS-1$
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					final ThreadPoolExecutor executorTmp = getExecutor();
					final String path =
							isHasParamCompilePath() ? getParamCompilePath() : getBasePath().getAbsolutePath();
					
					final boolean workHeroes = pathContainsCompileableForGame(path, gameHeroes);
					if (workHeroes) {
						startWorkOnGame(gameHeroes, path);
					}
					
					// wait until all UIs were build to save memory
					while (!executorTmp.getQueue().isEmpty() || executorTmp.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					gameHeroes = null;
					
					final boolean workSC2 = pathContainsCompileableForGame(path, gameSC2);
					if (workSC2) {
						startWorkOnGame(gameSC2, path);
					}
					
					while (!executorTmp.getQueue().isEmpty() || executorTmp.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					setStageTitle("Compiling completed.", primaryStage); //$NON-NLS-1$
					startReplayOrQuitOrShowError(primaryStage);
					executorTmp.shutdown();
					executorTmp.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					final long executionTime = (System.currentTimeMillis() - startTime);
					logger.info("Execution time: " + String.format("%d min, %d sec", //$NON-NLS-1$ //$NON-NLS-2$
							TimeUnit.MILLISECONDS.toMinutes(executionTime),
							TimeUnit.MILLISECONDS.toSeconds(executionTime) -
									TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))));
					Platform.runLater(() -> {
						try {
							getErrorTabControllers().get(0).setRunning(false);
							navController.unlockNav();
						} catch (final Exception e) {
							logger.fatal("FATAL ERROR: ", e);
						}
					});
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (final Exception e) {
					logger.fatal("FATAL ERROR: ", e);
				}
			}
		}.start();
	}
	
	/**
	 * @return the basePath
	 */
	public File getBasePath() {
		return basePath;
	}
	
	/**
	 * @return the hasParamCompilePath
	 */
	public boolean isHasParamCompilePath() {
		return hasParamCompilePath;
	}
	
	/**
	 * @return the paramCompilePath
	 */
	public String getParamCompilePath() {
		return paramCompilePath;
	}
	
	/**
	 * @return the errorTabControllers
	 */
	public List<ErrorTabPaneController> getErrorTabControllers() {
		return errorTabControllers;
	}
	
	/**
	 * @param game
	 * @param path
	 */
	private void startWorkOnGame(final GameData game, final String path) {
		executor.execute(() -> {
			try {
				addThreadLoggerTab(Thread.currentThread().getName(),
						game.getGameDef().getNameHandle() + "UI"); //$NON-NLS-1$
				
				if (isRequiredToParseDefaultUi()) {
					parseDefaultUI(game);
				}
				
				buildGamesInterfaceFiles(game, path);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	/**
	 * @param game
	 * @param path
	 */
	private void buildGamesInterfaceFiles(final GameData game, final String path) {
		if (hasParamCompilePath) {
			buildSpecificUI(new File(path), game);
		} else {
			buildGamesUIs(game.getGameDef().getNameHandle(), game);
		}
	}
	
	/**
	 * Returns whether the specified path contains a compilable file for the specified game.
	 *
	 * @param path
	 * @param game
	 * @return
	 */
	private boolean pathContainsCompileableForGame(final String path, final GameData game) {
		
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
	 * 		game whose default UI is parsed
	 * @throws InterruptedException
	 */
	private void parseDefaultUI(final GameData game) throws InterruptedException {
		printInfoLogMessageToGeneral(
				"Starting to parse base " + game.getGameDef().getName() + " UI."); //$NON-NLS-1$ //$NON-NLS-2$
		
		final UICatalog uiCatalog = game.getUiCatalog();
		final String gameDir =
				getBaseUiPath(game.getGameDef()) + File.separator + game.getGameDef().getModsSubDirectory();
		try {
			
			for (final String modOrDir : game.getGameDef().getCoreModsOrDirectories()) {
				
				final File directory = new File(gameDir + File.separator + modOrDir);
				
				final Collection<File> descIndexFiles = FileUtils
						.listFiles(directory, new WildcardFileFilter("DescIndex.*Layout"),
								TrueFileFilter.INSTANCE); //$NON-NLS-1$
				logger.info("number of descIndexFiles found: " + descIndexFiles.size()); //$NON-NLS-1$
				
				for (final File descIndexFile : descIndexFiles) {
					logger.info("parsing descIndexFile '" + descIndexFile.getPath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					uiCatalog.processDescIndex(descIndexFile, game.getGameDef().getDefaultRaceId());
				}
			}
			
			uiCatalog.clearParser();
			
		} catch (final SAXException | IOException | ParserConfigurationException | UIException e) {
			logger.error("ERROR parsing base UI catalog for '" + game.getGameDef().getName() + "'.",
					e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		final String msg =
				"Finished parsing base UI for " + game.getGameDef().getName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
		logger.info(msg);
		printInfoLogMessageToGeneral(msg);
	}
	
	/**
	 * Sets title of stage.
	 *
	 * @param title
	 * 		the new title
	 * @param stage
	 * 		stage receiving the title
	 */
	private void setStageTitle(final String title, final Stage stage) {
		Platform.runLater(() -> {
			try {
				stage.setTitle(title);
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	/**
	 * Starts a Replay, quits the Application or remains alive to show an error. Action depends on fields.
	 *
	 * @param primaryStage
	 * 		app's main stage
	 */
	private void startReplayOrQuitOrShowError(final Stage primaryStage) {
		if (wasStartedWithParameters && !anyErrorTrackerEncounteredError()) {
			// start game, launch replay
			attemptToRunGameWithReplay();
			if (!hasParamCompilePath) {
				Platform.runLater(() -> {
					try {
						// close after 5 seconds, if compiled all and no errors
						final PauseTransition delay = new PauseTransition(Duration.seconds(5));
						delay.setOnFinished(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								primaryStage.close();
							}
						});
						delay.play();
					} catch (final Exception e) {
						logger.fatal("FATAL ERROR: ", e);
					}
				});
			} else {
				// close instantly, if compiled special
				Platform.runLater(() -> {
					try {
						primaryStage.close();
					} catch (final Exception e) {
						logger.fatal("FATAL ERROR: ", e);
					}
				});
			}
		}
	}
	
	/**
	 * @return
	 */
	private boolean anyErrorTrackerEncounteredError() {
		for (final ErrorTabPaneController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Attempts to run a game with a replay, if desired.
	 */
	public void attemptToRunGameWithReplay() {
		final boolean isHeroes;
		final String gamePath;
		
		if (!compileAndRun) {
			// use the run param
			if (paramRunPath == null) {
				return;
			}
			gamePath = paramRunPath;
			isHeroes = gamePath.contains("HeroesSwitcher.exe"); //$NON-NLS-1$
		} else {
			// compileAndRun is active -> figure out the right game
			if (paramCompilePath.contains(File.separator + "heroes" + File.separator)) { //$NON-NLS-1$
				// Heroes
				isHeroes = true;
				if (settings.isHeroesPtrActive()) {
					// PTR Heroes
					if (settings.isHeroesPtr64bit()) {
						gamePath = settings.getHeroesPtrPath() + File.separator + "Support64" + File.separator
								//$NON-NLS-1$
								+ "HeroesSwitcher_x64.exe"; //$NON-NLS-1$
					} else {
						gamePath =
								settings.getHeroesPtrPath() + File.separator + "Support" + File.separator //$NON-NLS-1$
										+ "HeroesSwitcher.exe"; //$NON-NLS-1$
					}
				} else {
					// live Heroes
					if (settings.isHeroes64bit()) {
						gamePath =
								settings.getHeroesPath() + File.separator + "Support64" + File.separator //$NON-NLS-1$
										+ "HeroesSwitcher_x64.exe"; //$NON-NLS-1$
					} else {
						gamePath = settings.getHeroesPath() + File.separator + "Support" + File.separator //$NON-NLS-1$
								+ "HeroesSwitcher.exe"; //$NON-NLS-1$
					}
				}
			} else {
				// SC2
				isHeroes = false;
				if (settings.isSc64bit()) {
					gamePath = settings.getSc2Path() + File.separator + "Support64" + File.separator //$NON-NLS-1$
							+ "SC2Switcher_x64.exe"; //$NON-NLS-1$
				} else {
					gamePath = settings.getSc2Path() + File.separator + "Support" + File.separator +
							"SC2Switcher.exe"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		logger.info("Game location: " + gamePath); //$NON-NLS-1$
		
		final File replay = replayFinder.getLastUsedOrNewestReplay(isHeroes, documentsPath);
		if (replay != null && replay.exists() && replay.isFile()) {
			logger.info("Starting game with replay: " + replay.getName()); //$NON-NLS-1$
			final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() +
					"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// logger.trace("executing: " + cmd); //$NON-NLS-1$
			try {
				Runtime.getRuntime().exec(cmd);
				// logger.trace("after Start attempt..."); //$NON-NLS-1$
			} catch (final IOException e) {
				logger.error("Failed to execute the game launch command.", e); //$NON-NLS-1$
			}
		} else {
			logger.error("Failed to find any replay."); //$NON-NLS-1$
		}
		printInfoLogMessageToGeneral("The game starts with a replay now..."); //$NON-NLS-1$
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables() {
		logger.info("basePath: " + basePath); //$NON-NLS-1$
		logger.info("documentsPath: " + documentsPath); //$NON-NLS-1$
		if (paramCompilePath != null) {
			logger.info("compile param path: " + paramCompilePath); //$NON-NLS-1$
			if (compileAndRun) {
				logger.info("run after compile: true"); //$NON-NLS-1$
			}
		}
		if (paramRunPath != null) {
			logger.info("run param path: " + paramRunPath); //$NON-NLS-1$
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
		wasStartedWithParameters = !namedParams.isEmpty();
		
		// COMPILE / COMPILERUN PARAM
		// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
		paramCompilePath = namedParams.get("compileRun");
		if (paramCompilePath != null) { //$NON-NLS-1$
			compileAndRun = true;
		} else {
			paramCompilePath = namedParams.get("compile"); //$NON-NLS-1$
			compileAndRun = false;
		}
		paramCompilePath = getInterfaceRootFromPath(paramCompilePath);
		hasParamCompilePath = (paramCompilePath != null);
		
		// RUN PARAM
		// --run="F:\Games\Heroes of the Storm\Support\HeroesSwitcher.exe"
		paramRunPath = namedParams.get("run"); //$NON-NLS-1$
	}
	
	/**
	 * Initialize Paths.
	 */
	private void initPaths() {
		basePath = JarHelper.getJarDir(getClass());
	}
	
	/**
	 * Initialize GUI.
	 *
	 * @param primaryStage
	 * 		the main App's Stage
	 * @throws IOException
	 * 		when loading the UI definition fails
	 */
	private void initGUI(final Stage primaryStage) throws IOException {
		// UI layout: borderPane -center-> tabPane (via layout) -tab_0-> virtualizedScrollPane -> StyleClassedTextArea
		//                       -left-> navigation button bar
		
		// Build Navigation
		final BorderPane root;
		final FXMLLoader loader = new FXMLLoader();
		try (InputStream is = getClass().getResourceAsStream("view/Navigation.fxml")) { //$NON-NLS-1$
			root = loader.load(is);
			navController = loader.getController();
		} catch (final IOException e) {
			logger.error("Failed to load Navigation.fxml:", e); //$NON-NLS-1$
			throw new IOException("Failed to load Navigation.fxml.", e);
		}
		final Scene scene = new Scene(root, 1200, 600);
		scene.getStylesheets().add(getClass().getResource("view/application.css").toExternalForm()); //$NON-NLS-1$
		scene.getStylesheets().add(getClass().getResource("view/textStyles.css").toExternalForm()); //$NON-NLS-1$
		
		// app icon
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		} catch (final Exception e) {
			logger.error("Failed to load ahli.png"); //$NON-NLS-1$
		}
		
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Compiling Interfaces..."); //$NON-NLS-1$
		primaryStage.show();
		logger.info("Initializing App..."); //$NON-NLS-1$
	}
	
	/**
	 * Adds an ErrorTabController.
	 *
	 * @param errorTabCtrl
	 */
	public void addErrorTabController(final ErrorTabPaneController errorTabCtrl) {
		errorTabControllers.add(errorTabCtrl);
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabTitle
	 */
	private void addThreadLoggerTab(final String threadName, final String tabTitle) {
		final Tab newTab = new Tab();
		final StyleClassedTextArea newTxtArea = new StyleClassedTextArea();
		final ErrorTabPaneController errorTabCtrl = new ErrorTabPaneController(newTab, newTxtArea, true, false);
		errorTabCtrl.setRunning(true);
		errorTabControllers.add(errorTabCtrl);
		
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane =
				new VirtualizedScrollPane<>(newTxtArea);
		newTab.setContent(virtualizedScrollPane);
		StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
		newTab.setText(tabTitle);
		newTxtArea.setEditable(false);
		
		// runlater needs to appear below the edits above, else it might be added before
		// which results in UI edits not in UI thread -> error
		Platform.runLater(() -> {
			try {
				getTabPane().getTabs().add(newTab);
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	/**
	 * ParamPath might be some layout or folder within the interface, so it is cut down to the interface base path.
	 *
	 * @param str
	 * 		application's compileParam's value
	 * @return shortens the path to the Interface root folder
	 */
	private String getInterfaceRootFromPath(String str) {
		if (str != null) {
			while (str.length() > 0 && !str.endsWith("Interface")) { //$NON-NLS-1$
				final int lastIndex = str.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					str = str.substring(0, lastIndex);
				} else {
					return null;
				}
			}
		}
		return str;
	}
	
	/**
	 * Build all Interfaces for a game's subdirectory.
	 *
	 * @param subfolderName
	 * 		name from the base path to the directory whose interfaces are built.
	 * @param gameData
	 * 		the game information
	 */
	public void buildGamesUIs(final String subfolderName, final GameData gameData) {
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
	 * 		folder of the interface file to be built
	 * @param game
	 * 		the game information about the file
	 */
	public void buildSpecificUI(final File interfaceFolder, final GameData game) {
		if (interfaceFolder.isDirectory()) {
			if (!executor.isShutdown()) {
				// create tasks for the worker pool
				executor.execute(() -> {
					try {
						addThreadLoggerTab(Thread.currentThread().getName(), interfaceFolder.getName());
						// create unique cache path
						final MpqEditorInterface threadsMpqInterface =
								(MpqEditorInterface) getBaseMpqInterface().deepCopy();
						final long threadId = Thread.currentThread().getId();
						threadsMpqInterface.setMpqCachePath(getBaseMpqInterface().getMpqCachePath() + threadId);
						
						// work
						final boolean compressXml;
						final int compressMpqSetting;
						final boolean buildUnprotectedToo;
						final boolean repairLayoutOrder;
						final boolean verifyLayout;
						final boolean verifyXml;
						if (wasStartedWithParameters) {
							compressXml = settings.isCmdLineCompressXml();
							compressMpqSetting = settings.getCmdLineCompressMpq();
							buildUnprotectedToo = settings.isCmdLineBuildUnprotectedToo();
							repairLayoutOrder = settings.isCmdLineRepairLayoutOrder();
							verifyLayout = settings.isCmdLineVerifyLayout();
							verifyXml = settings.isCmdLineVerifyXml();
						} else {
							compressXml = settings.isGuiCompressXml();
							compressMpqSetting = settings.getGuiCompressMpq();
							buildUnprotectedToo = settings.isGuiBuildUnprotectedToo();
							repairLayoutOrder = settings.isGuiRepairLayoutOrder();
							verifyLayout = settings.isGuiVerifyLayout();
							verifyXml = settings.isGuiVerifyXml();
						}
						buildFile(interfaceFolder, game, threadsMpqInterface, compressXml, compressMpqSetting,
								buildUnprotectedToo, repairLayoutOrder, verifyLayout, verifyXml);
						threadsMpqInterface.clearCacheExtractedMpq();
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					} catch (final IOException e) {
						logger.error("ERROR: Exception while building UIs.", e); //$NON-NLS-1$
					} catch (final Exception e) {
						logger.fatal("FATAL ERROR: ", e);
					}
				});
			} else {
				logger.error("ERROR: Executor shut down. Skipping building a UI..."); //$NON-NLS-1$
			}
		} else {
			logger.error("ERROR: Can't build UI from file '" + interfaceFolder +
					"', expected a directory."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Prints a message to the message log.
	 *
	 * @param msg
	 * 		the message
	 */
	public void printInfoLogMessageToGeneral(final String msg) {
		Platform.runLater(() -> {
			try {
				logger.info(msg);
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	/**
	 * Prints a message to the message log.
	 *
	 * @param msg
	 * 		the message
	 */
	public void printErrorLogMessageToGeneral(final String msg) {
		Platform.runLater(() -> {
			try {
				logger.error(msg);
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	
	/**
	 * Builds MPQ Archive File. Run this in its own thread! Conditions: - Specified MpqInterface requires a unique cache
	 * path for multithreading.
	 *
	 * @param sourceFile
	 * 		folder location
	 * @param game
	 * 		the game data with game definition
	 * @param mpqi
	 * 		MpqInterface with unique cache path
	 * @param compressXml
	 * @param compressMpq
	 * @param buildUnprotectedToo
	 * @param repairLayoutOrder
	 * @param verifyLayout
	 * @param verifyXml
	 * @throws IOException
	 * 		when something goes wrong
	 * @throws InterruptedException
	 */
	private void buildFile(final File sourceFile, final GameData game, final MpqEditorInterface mpqi,
			final boolean compressXml, final int compressMpq, final boolean buildUnprotectedToo,
			final boolean repairLayoutOrder, final boolean verifyLayout, final boolean verifyXml)
			throws IOException, InterruptedException {
		printInfoLogMessageToGeneral(sourceFile.getName() + " started construction.");
		
		final GameDef gameDef = game.getGameDef();
		
		final String targetPath =
				documentsPath + File.separator + gameDef.getDocumentsGameDirectoryName() + File.separator +
						gameDef.getDocumentsInterfaceSubdirectoryName();
		
		// init mod data
		final ModData mod = new ModData(game);
		mod.setSourcePath(sourceFile);
		final File targetFile = new File(targetPath);
		mod.setTargetPath(targetFile);
		
		// get and create cache
		final File cache = new File(mpqi.getMpqCachePath());
		if (!cache.exists() && !cache.mkdirs()) {
			final String msg = "Unable to create cache directory."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		int cacheClearAttempts;
		for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} else {
				// success
				break;
			}
		}
		if (cacheClearAttempts > 100) {
			final String msg = "ERROR: Cache could not be cleared"; //$NON-NLS-1$
			logger.error(msg);
			return;
		}
		mod.setMpqCachePath(cache);
		// put files into cache
		int copyAttempts;
		for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				copyFileOrFolder(sourceFile, cache);
				// logger.trace("Copy Folder took " + copyAttempts + " attempts to succeed.");
				// //$NON-NLS-1$ //$NON-NLS-2$
				break;
			} catch (final FileSystemException e) {
				if (copyAttempts == 0) {
					logger.warn("Attempt to copy directory failed.", e); //$NON-NLS-1$
				} else if (copyAttempts >= 100) {
					final String msg =
							"Unable to copy directory after 100 copy attempts: " + e.getMessage(); //$NON-NLS-1$
					logger.error(msg, e);
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} catch (final IOException e) {
				final String msg = "Unable to copy directory"; //$NON-NLS-1$
				logger.error(msg, e);
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Above code did not throw exception about copy attempt threshold reached."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		
		// logger.trace("retrieving componentList"); //$NON-NLS-1$
		final File componentListFile = mpqi.getComponentListFile();
		mod.setComponentListFile(componentListFile);
		// logger.trace("retrieving descIndex - set path and clear"); //$NON-NLS-1$
		
		final DescIndexData descIndexData = new DescIndexData(mpqi);
		mod.setDescIndexData(descIndexData);
		
		try {
			descIndexData.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile, gameDef));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path."; //$NON-NLS-1$
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
		
		// logger.trace("retrieving descIndex - get cached file"); //$NON-NLS-1$
		final File descIndexFile = mpqi.getFileFromMpq(descIndexData.getDescIndexIntPath());
		// logger.trace("adding layouts from descIndexFile: " +
		// descIndexFile.getAbsolutePath()); //$NON-NLS-1$
		try {
			descIndexData.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, false));
		} catch (SAXException | ParserConfigurationException | IOException | MpqException e) {
			logger.error("unable to read Layout paths", e); //$NON-NLS-1$
		}
		
		logger.info("Compiling... " + sourceFile.getName()); //$NON-NLS-1$
		
		// perform checks/improvements on code
		compileManager.compile(mod, "Terr", repairLayoutOrder, verifyLayout, verifyXml); //$NON-NLS-1$
		
		logger.info("Building... " + sourceFile.getName()); //$NON-NLS-1$
		
		try {
			mpqi.buildMpq(targetPath, sourceFile.getName(), compressXml, getCompressionModeOfSetting(compressMpq),
					buildUnprotectedToo);
			
			logger.info("Finished building... " + sourceFile.getName() + ". Size: " +
					(new File(targetPath + File.separator + sourceFile.getName()).length() / 1024) + " " + "kb");
			//$NON-NLS-1$
			printInfoLogMessageToGeneral(sourceFile.getName() + " finished construction.");
		} catch (final IOException | MpqException e) {
			logger.error("ERROR: unable to construct final Interface file.", e); //$NON-NLS-1$
			printErrorLogMessageToGeneral(sourceFile.getName() + " could not be created.");
		}
	}
	
	/**
	 * @param compressMpqSetting
	 * @return
	 */
	private MpqEditorCompression getCompressionModeOfSetting(final int compressMpqSetting) {
		switch (compressMpqSetting) {
			case 0:
				return MpqEditorCompression.NONE;
			case 1:
				return MpqEditorCompression.BLIZZARD_SC2_HEROES;
			case 2:
				return MpqEditorCompression.CUSTOM;
			case 3:
				return MpqEditorCompression.SYSTEM_DEFAULT;
			default:
				throw new IllegalArgumentException("Unsupported mpq compression mode.");
		}
	}
	
	/**
	 * Is called when the App is closing.
	 */
	@Override
	public void stop() {
		logger.info("App is about to shut down."); //$NON-NLS-1$
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		}
		try {
			executor.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			logger.error(
					"ERROR: Executor timed out waiting for Worker Theads to terminate. A Thread might run rampage.",
					e); //$NON-NLS-1$
			Thread.currentThread().interrupt();
		}
		baseMpqInterface.clearCacheExtractedMpq();
		logger.info("App waves Goodbye!"); //$NON-NLS-1$
	}
	
	/**
	 * Initializes the MPQ Interface.
	 */
	private void initMpqInterface() {
		final String tempDirectory = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		final String cachePath = tempDirectory + "ObserverInterfaceBuilder" + File.separator +
				"_ExtractedMpq"; //$NON-NLS-1$ //$NON-NLS-2$
		baseMpqInterface = new MpqEditorInterface(cachePath);
		baseMpqInterface.setMpqEditorPath(
				basePath.getParent() + File.separator + "tools" + File.separator + "plugins" //$NON-NLS-1$ //$NON-NLS-2$
						+ File.separator + "mpq" + File.separator + "MPQEditor.exe"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Initializes the Settings File Interface. It either uses the specified SettingsIniInterface or loads them from the
	 * default location.
	 *
	 * @param optionalSettingsToLoad
	 * 		optional SettingsIniInterface, set to null to load from default location
	 */
	private void initSettings(final SettingsIniInterface optionalSettingsToLoad) {
		SettingsIniInterface settings2 = optionalSettingsToLoad;
		if (optionalSettingsToLoad == null) {
			final String settingsFilePath = basePath.getParent() + File.separator + "settings.ini"; //$NON-NLS-1$
			settings2 = new SettingsIniInterface(settingsFilePath);
		}
		try {
			settings2.readSettingsFromFile();
		} catch (final IOException e) {
			logger.error("Settings file could not be read.", e); //$NON-NLS-1$
		}
		settings = settings2;
	}
	
	/**
	 * @return the executor
	 */
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	/**
	 * @return the gameSC2
	 */
	public GameData getGameSC2() {
		return gameSC2;
	}
	
	/**
	 * @return the gameHeroes
	 */
	public GameData getGameHeroes() {
		return gameHeroes;
	}
	
	/**
	 * @return the baseMpqInterface
	 */
	public MpqEditorInterface getBaseMpqInterface() {
		return baseMpqInterface;
	}
	
	/**
	 * @return
	 */
	public TabPane getTabPane() {
		return TabPaneController.getInstance().getTabPane();
	}
	
	/**
	 * Returns a reference to the ini settings in effect.
	 *
	 * @return
	 */
	public SettingsIniInterface getIniSettings() {
		return settings;
	}
	
	/**
	 * @return
	 */
	public boolean isRequiredToParseDefaultUi() {
		return wasStartedWithParameters ? settings.isCmdLineVerifyLayout() : settings.isGuiVerifyLayout();
	}
	
	/**
	 * Worker Thread Factory for the Thread Pool.
	 *
	 * @author Ahli
	 */
	private static final class MyThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(final Runnable runnable) {
			final Thread t = new Thread(runnable);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
	
	private static final class MyThreadPoolExecutor extends ThreadPoolExecutor {
		
		public MyThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
				final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		}
		
		@Override
		protected void beforeExecute(final Thread t, final Runnable r) {
			super.beforeExecute(t, r);
			// altering the thread name allows the logger to use the correct controller
			// -> log message is always put into the correct ID
			t.setName(t.getId() + "_" + r.hashCode());
		}
		
		@Override
		protected void afterExecute(final Runnable r, final Throwable t) {
			super.afterExecute(r, t);
			StylizedTextAreaAppender.finishedWork(Thread.currentThread().getName());
		}
		
	}
	
}
