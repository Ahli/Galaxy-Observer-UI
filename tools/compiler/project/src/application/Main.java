package application;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
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
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;

import application.controller.ErrorTabPaneController;
import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.util.JarHelper;
import application.util.logger.log4j2plugin.StylizedTextAreaAppender;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Interface Compiler and Builder Application.
 * 
 * @author Ahli
 */
public final class Main extends Application {
	
	private static Logger logger = LogManager.getLogger(Main.class);
	private ThreadPoolExecutor executor;
	
	// Components
	private MpqEditorInterface baseMpqInterface = null;
	private SettingsIniInterface settings = null;
	private final ReplayFinder replayFinder = new ReplayFinder();
	private final CompileManager compileManager = new CompileManager();
	
	// data
	private final String documentsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	private File basePath = null;
	private final GameData gameSC2 = new GameData(new SC2GameDef());
	private final GameData gameHeroes = new GameData(new HeroesGameDef());
	
	// GUI
	// private StyleClassedTextArea txtArea = null;
	private TabPane tabPane = null;
	private boolean userPreventedAutomaticClosing = false;
	private final List<ErrorTabPaneController> errorTabControllers = new ArrayList<>();
	
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
		logger.trace("trace log visible"); //$NON-NLS-1$
		logger.debug("debug log visible"); //$NON-NLS-1$
		logger.info("info log visible"); //$NON-NLS-1$
		logger.warn("warn log visible"); //$NON-NLS-1$
		logger.error("error log visible"); //$NON-NLS-1$
		logger.fatal("fatal log visible"); //$NON-NLS-1$
		
		logger.trace("Configuration File of System: " + System.getProperty("log4j.configurationFile")); //$NON-NLS-1$ //$NON-NLS-2$
		logger.info("Launch arguments: " + Arrays.toString(args)); //$NON-NLS-1$
		
		// try {
		// Thread.sleep(5000);
		// } catch (final InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		
		launch(args);
	}
	
	/**
	 * Called when the App is initializing.
	 */
	@Override
	public void start(final Stage primaryStage) {
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
		
		final int maxThreads = Math.max(1, Math.min(numberOfProcessors / 2, 1));
		executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					@Override
					public Thread newThread(final Runnable runnable) {
						final Thread t = new Thread(runnable);
						t.setPriority(Thread.NORM_PRIORITY);
						return t;
					}
				}) {
					
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
		};
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
		return basePath.getParent() + File.separator + "baseUI" + File.separator + gameDef.getNameHandle(); //$NON-NLS-1$
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
			startWorkOnGame(gameHeroes, path);
		}
		
		if (workSC2) {
			startWorkOnGame(gameSC2, path);
		}
		
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setName("Supervisor"); //$NON-NLS-1$
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				try {
					final ThreadPoolExecutor executor = getExecutor();
					while (executor.getQueue().size() > 0 || executor.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					setStageTitle("Compiling completed.", primaryStage); //$NON-NLS-1$
					startReplayOrQuitOrShowError(primaryStage);
					executor.shutdown();
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					final long executionTime = (System.currentTimeMillis() - startTime);
					logger.info("Execution time: " + String.format("%d min, %d sec", //$NON-NLS-1$ //$NON-NLS-2$
							TimeUnit.MILLISECONDS.toMinutes(executionTime),
							TimeUnit.MILLISECONDS.toSeconds(executionTime)
									- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))));
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							errorTabControllers.get(0).setRunning(false);
						}
					});
				} catch (final InterruptedException e) {
					logger.trace("INTERRUPT: Execution time measuring Thread was interrupted."); //$NON-NLS-1$
				}
			}
		}.start();
		
	}
	
	/**
	 * @param game
	 * @param path
	 */
	private void startWorkOnGame(final GameData game, final String path) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					addThreadLoggerTab(Thread.currentThread().getName(), game.getGameDef().getNameHandle() + "UI"); //$NON-NLS-1$
					
					parseDefaultUI(game);
					
					buildGamesInterfaceFiles(game, path);
				} catch (final InterruptedException e) {
					logger.trace("INTERRUPT: Building " + game.getGameDef().getNameHandle() //$NON-NLS-1$
							+ " interface files was interrupted."); //$NON-NLS-1$
				}
			}
		});
	}
	
	/**
	 * @param gameHeroes2
	 * @param path
	 * @throws InterruptedException
	 */
	private void buildGamesInterfaceFiles(final GameData game, final String path) throws InterruptedException {
		try {
			if (hasParamCompilePath) {
				buildSpecificUI(new File(path), game);
			} else {
				buildGamesUIs(game.getGameDef().getNameHandle(), game);
			}
		} catch (final IOException e) {
			logger.error("ERROR while building a UI for " + game.getGameDef().getName() + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		}
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
	 * @throws InterruptedException
	 */
	private void parseDefaultUI(final GameData game) throws InterruptedException {
		printLogMessageToGeneral("Starting to parse base " + game.getGameDef().getName() + " UI."); //$NON-NLS-1$ //$NON-NLS-2$
		
		final UICatalog uiCatalog = game.getUiCatalog();
		final String gameDir = getBaseUiPath(game.getGameDef()) + File.separator
				+ game.getGameDef().getModsSubDirectory();
		try {
			
			for (final String modOrDir : game.getGameDef().getCoreModsOrDirectories()) {
				
				final File directory = new File(gameDir + File.separator + modOrDir);
				
				final Collection<File> descIndexFiles = FileUtils.listFiles(directory,
						new WildcardFileFilter("DescIndex.*Layout"), TrueFileFilter.INSTANCE); //$NON-NLS-1$
				logger.info("number of descIndexFiles found: " + descIndexFiles.size()); //$NON-NLS-1$
				
				for (final File descIndexFile : descIndexFiles) {
					logger.info("parsing descIndexFile '" + descIndexFile.getPath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					uiCatalog.processDescIndex(descIndexFile, game.getGameDef().getDefaultRaceId());
				}
			}
			
			uiCatalog.clearDomParser();
			
		} catch (final SAXException | IOException | ParserConfigurationException | UIException e) {
			logger.error("ERROR parsing base UI catalog for '" + game.getGameDef().getName() + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		}
		final String msg = "Finished parsing base UI for " + game.getGameDef().getName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
		logger.info(msg);
		printLogMessageToGeneral(msg);
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
		if (!anyErrorTrackerEncounteredError()) {
			// start game, launch replay
			attemptToRunGameWithReplay();
			if (!userPreventedAutomaticClosing) {
				if (!hasParamCompilePath) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							// close after 5 seconds, if compiled all and no errors
							final PauseTransition delay = new PauseTransition(Duration.seconds(5));
							delay.setOnFinished(new EventHandler<ActionEvent>() {
								@Override
								public void handle(final ActionEvent event) {
									if (!isUserPreventedAutomaticClosing()) {
										primaryStage.close();
									}
								}
							});
							delay.play();
						}
					});
				} else {
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
		boolean isHeroes = false;
		String gamePath = null;
		
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
				if (settings.isPtrActive()) {
					// PTR Heroes
					if (settings.isHeroesPtr64bit()) {
						gamePath = settings.getHeroesPtrPath() + File.separator + "Support64" + File.separator //$NON-NLS-1$
								+ "HeroesSwitcher_x64.exe"; //$NON-NLS-1$
					} else {
						gamePath = settings.getHeroesPtrPath() + File.separator + "Support" + File.separator //$NON-NLS-1$
								+ "HeroesSwitcher.exe"; //$NON-NLS-1$
					}
				} else {
					// live Heroes
					if (settings.isHeroes64bit()) {
						gamePath = settings.getHeroesPath() + File.separator + "Support64" + File.separator //$NON-NLS-1$
								+ "HeroesSwitcher_x64.exe"; //$NON-NLS-1$
					} else {
						gamePath = settings.getHeroesPath() + File.separator + "Support" + File.separator //$NON-NLS-1$
								+ "HeroesSwitcher.exe"; //$NON-NLS-1$
					}
				}
			} else {
				// SC2
				isHeroes = false;
				if (settings.isSC264bit()) {
					gamePath = settings.getSC2Path() + File.separator + "Support64" + File.separator //$NON-NLS-1$
							+ "SC2Switcher_x64.exe"; //$NON-NLS-1$
				} else {
					gamePath = settings.getSC2Path() + File.separator + "Support" + File.separator + "SC2Switcher.exe"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		logger.info("Game location: " + gamePath); //$NON-NLS-1$
		
		final File replay = replayFinder.getLastUsedOrNewestReplay(isHeroes, documentsPath);
		if (replay != null && replay.exists() && replay.isFile()) {
			logger.info("Starting game with replay: " + replay.getName()); //$NON-NLS-1$
			final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			logger.trace("executing: " + cmd); //$NON-NLS-1$
			try {
				Runtime.getRuntime().exec(cmd);
				logger.trace("after Start attempt..."); //$NON-NLS-1$
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.error("Failed to find any replay."); //$NON-NLS-1$
		}
		printLogMessageToGeneral("The game starts with a replay now..."); //$NON-NLS-1$
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
				logger.info("run after compile: " + compileAndRun); //$NON-NLS-1$
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
		
		// COMPILE / COMPILERUN PARAM
		// --compile="D:\GalaxyObsUI\dev\heroes\AhliObs.StormInterface"
		paramCompilePath = namedParams.get("compile"); //$NON-NLS-1$
		compileAndRun = false;
		if (namedParams.get("compileRun") != null) { //$NON-NLS-1$
			paramCompilePath = namedParams.get("compileRun"); //$NON-NLS-1$
			compileAndRun = true;
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
		
		final StyleClassedTextArea txtArea = new StyleClassedTextArea();
		txtArea.setEditable(false);
		// log4j2 can print into GUI
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(txtArea);
		
		final Scene scene = new Scene(root, 1200, 400);
		scene.getStylesheets().add(Main.class.getResource("view/application.css").toExternalForm()); //$NON-NLS-1$
		scene.getStylesheets().add(Main.class.getResource("view/textStyles.css").toExternalForm()); //$NON-NLS-1$
		
		final FXMLLoader loader = new FXMLLoader();
		// loader.setResources(Messages.getBundle());
		try (InputStream is = Main.class.getResourceAsStream("view/TabsLayout.fxml");) { //$NON-NLS-1$
			tabPane = (TabPane) loader.load(is); // $NON-NLS-1$
		} catch (final IOException e) {
			logger.error("failed to load TabsLayout.fxml"); //$NON-NLS-1$
			e.printStackTrace();
		}
		root.setCenter(tabPane);
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		tab.setContent(virtualizedScrollPane);
		
		final ErrorTabPaneController errorTabCtrl = new ErrorTabPaneController(tab, txtArea, false, true);
		errorTabCtrl.setRunning(true);
		errorTabControllers.add(errorTabCtrl);
		StylizedTextAreaAppender.setGeneralController(errorTabCtrl);
		
		try {
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		} catch (final Exception e) {
			logger.error("Failed to load ahli.png"); //$NON-NLS-1$
		}
		
		// mouse click detection to prevent auto-closing
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				setUserPreventedAutomaticClosing(true);
			}
		});
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Compiling Interfaces..."); //$NON-NLS-1$
		primaryStage.show();
		logger.info("Initializing App..."); //$NON-NLS-1$
	}
	
	/**
	 * @param b
	 */
	public void setUserPreventedAutomaticClosing(final boolean b) {
		userPreventedAutomaticClosing = b;
	}
	
	/**
	 * @return the userPreventedAutomaticClosing
	 */
	public boolean isUserPreventedAutomaticClosing() {
		return userPreventedAutomaticClosing;
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 * 
	 * @param threadId
	 */
	private void addThreadLoggerTab(final String threadName, final String tabTitle) {
		final Tab newTab = new Tab();
		final StyleClassedTextArea newTxtArea = new StyleClassedTextArea();
		final ErrorTabPaneController errorTabCtrl = new ErrorTabPaneController(newTab, newTxtArea, true, false);
		errorTabCtrl.setRunning(true);
		errorTabControllers.add(errorTabCtrl);
		
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(
				newTxtArea);
		newTab.setContent(virtualizedScrollPane);
		StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
		newTab.setText(tabTitle);
		newTxtArea.setEditable(false);
		
		// runlater needs to appear below the edits above, else it might be added before
		// which results in UI edits not in UI thread -> error
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getTabPane().getTabs().add(newTab);
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
			while (str.length() > 0 && !str.endsWith("Interface")) { //$NON-NLS-1$
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
		logger.trace("cutting result: " + str); //$NON-NLS-1$
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
			if (!executor.isShutdown()) {
				// create tasks for the worker pool
				executor.execute(new Runnable() {
					@Override
					public void run() {
						addThreadLoggerTab(Thread.currentThread().getName(), interfaceFolder.getName());
						// create unique cache path
						final MpqEditorInterface threadsMpqInterface = (MpqEditorInterface) getBaseMpqInterface()
								.deepCopy();
						final long threadId = Thread.currentThread().getId();
						threadsMpqInterface.setMpqCachePath(getBaseMpqInterface().getMpqCachePath() + threadId);
						
						// work
						try {
							buildFile(interfaceFolder, game, threadsMpqInterface);
							threadsMpqInterface.clearCacheExtractedMpq();
						} catch (final InterruptedException e) {
							logger.trace("INTERRUPT: building a File was interrupted."); //$NON-NLS-1$
						} catch (final IOException e) {
							logger.error("ERROR: Exception while building UIs.", e); //$NON-NLS-1$
							e.printStackTrace();
						}
					}
				});
			} else {
				logger.error("ERROR: Executor shut down. Skipping building a UI..."); //$NON-NLS-1$
			}
		} else {
			logger.error("ERROR: Can't build UI from file '" + interfaceFolder + "', expected a directory."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Prints a message to the message log.
	 * 
	 * @param msg
	 *            the message
	 */
	public void printLogMessageToGeneral(final String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				logger.info(msg);
			}
		});
	}
	
	/**
	 * Builds MPQ Archive File. Run this in its own thread! Conditions: - Specified
	 * MpqInterface requires a unique cache path for multithreading.
	 * 
	 * @param sourceFile
	 *            folder location
	 * @param game
	 *            set to true, if Heroes
	 * @param mpqi
	 *            MpqInterface with unique cache path
	 * @throws IOException
	 *             when something goes wrong
	 * @throws InterruptedException
	 */
	private void buildFile(final File sourceFile, final GameData game, final MpqEditorInterface mpqi)
			throws IOException, InterruptedException {
		printLogMessageToGeneral(sourceFile.getName() + " started construction.");
		
		final GameDef gameDef = game.getGameDef();
		
		String targetPath = documentsPath + File.separator;
		targetPath += gameDef.getDocumentsGameDirectoryName();
		targetPath += File.separator + gameDef.getDocumentsInterfaceSubdirectoryName();
		
		// do stuff
		final ModData mod = new ModData(game);
		mod.setSourcePath(sourceFile);
		mod.setTargetPath(new File(targetPath));
		
		// get and create cache
		final File cache = new File(mpqi.getMpqCachePath());
		if (!cache.exists() && !cache.mkdirs()) {
			final String msg = "Unable to create cache directory."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		int cacheClearAttempts = 0;
		y: for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} else {
				// success
				break y;
			}
		}
		if (cacheClearAttempts > 100) {
			final String msg = "ERROR: Cache could not be cleared"; //$NON-NLS-1$
			logger.error(msg);
			return;
		}
		mod.setMpqCachePath(cache);
		// put files into cache
		int copyAttempts = 0;
		x: for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				copyFileOrFolder(sourceFile, cache);
				logger.trace("Copy Folder took " + copyAttempts + " attempts to succeed."); //$NON-NLS-1$ //$NON-NLS-2$
				break x;
			} catch (final FileSystemException e) {
				if (copyAttempts == 0) {
					logger.warn("Attempt to copy directory failed.", e); //$NON-NLS-1$
				} else if (copyAttempts >= 100) {
					final String msg = "Unable to copy directory after 100 copy attempts: " + e.getMessage(); //$NON-NLS-1$
					logger.error(msg, e);
					e.printStackTrace();
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} catch (final IOException e) {
				final String msg = "Unable to copy directory"; //$NON-NLS-1$
				logger.error(msg, e);
				e.printStackTrace();
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Above code did not throw exception about copy attempt threshold reached."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		
		logger.trace("retrieving componentList"); //$NON-NLS-1$
		final File componentListFile = mpqi.getComponentListFile();
		mod.setComponentListFile(componentListFile);
		logger.trace("retrieving descIndex - set path and clear"); //$NON-NLS-1$
		
		final DescIndexData descIndexData = new DescIndexData(mpqi);
		mod.setDescIndexData(descIndexData);
		
		try {
			descIndexData.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile, gameDef));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path."; //$NON-NLS-1$
			logger.error(msg, e);
			e.printStackTrace();
			throw new IOException(msg, e);
		}
		
		logger.trace("retrieving descIndex - get cached file"); //$NON-NLS-1$
		final File descIndexFile = mpqi.getFileFromMpq(descIndexData.getDescIndexIntPath());
		logger.trace("adding layouts from descIndexFile: " + descIndexFile.getAbsolutePath()); //$NON-NLS-1$
		try {
			descIndexData.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, false));
		} catch (SAXException | ParserConfigurationException | IOException | MpqException e) {
			logger.error("unable to read Layout paths", e); //$NON-NLS-1$
			e.printStackTrace();
		}
		
		logger.info("Compiling... " + sourceFile.getName()); //$NON-NLS-1$
		
		// perform checks/improvements on code
		compileManager.compile(mod, "Terr"); //$NON-NLS-1$
		
		logger.info("Building... " + sourceFile.getName()); //$NON-NLS-1$
		
		try {
			final boolean protectMPQ = game.getGameDef() instanceof HeroesGameDef ? settings.isHeroesProtectMPQ()
					: settings.isSC2ProtectMPQ();
			mpqi.buildMpq(targetPath, sourceFile.getName(), protectMPQ, settings.isBuildUnprotectedToo());
			logger.info("Finished building... " + sourceFile.getName()); //$NON-NLS-1$
		} catch (final IOException | MpqException e) {
			logger.error("ERROR: unable to construct final Interface file.", e); //$NON-NLS-1$
			e.printStackTrace();
		}
		printLogMessageToGeneral(sourceFile.getName() + " finished construction.");
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
					"ERROR: Executor timed out waiting for Worker Theads to terminate. A Thread might run rampage.", e); //$NON-NLS-1$
		}
		baseMpqInterface.clearCacheExtractedMpq();
		logger.info("App waves Goodbye!"); //$NON-NLS-1$
	}
	
	/**
	 * Initializes the MPQ Interface.
	 */
	private void initMpqInterface() {
		final String tempDirectory = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		final String cachePath = tempDirectory + "ObserverInterfaceBuilder" + File.separator + "_ExtractedMpq"; //$NON-NLS-1$ //$NON-NLS-2$
		baseMpqInterface = new MpqEditorInterface(cachePath);
		baseMpqInterface.setMpqEditorPath(basePath.getParent() + File.separator + "tools" + File.separator + "plugins" //$NON-NLS-1$ //$NON-NLS-2$
				+ File.separator + "mpq" + File.separator + "MPQEditor.exe"); //$NON-NLS-1$ //$NON-NLS-2$
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
		SettingsIniInterface settings = optionalSettingsToLoad;
		if (optionalSettingsToLoad == null) {
			final String settingsFilePath = basePath.getParent() + File.separator + "settings.ini"; //$NON-NLS-1$
			settings = new SettingsIniInterface(settingsFilePath);
		}
		try {
			settings.readSettingsFromFile();
		} catch (final FileNotFoundException e) {
			logger.error("settings file could not be found", e); //$NON-NLS-1$
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
		return tabPane;
	}
	
}
