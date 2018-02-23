package application;

import application.build.MpqBuilderService;
import application.config.ConfigService;
import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.ui.navigation.NavigationController;
import application.ui.progress.ErrorTabController;
import application.ui.progress.StylizedTextAreaAppender;
import application.ui.progress.TabPaneController;
import application.util.FXMLSpringLoader;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Interface Compiler and Builder Application.
 *
 * @author Ahli
 */

@SpringBootApplication
public class InterfaceBuilderApp extends Application {
	
	private static final Logger logger = LogManager.getLogger();
	
	private static InterfaceBuilderApp instance = null;
	private final List<ErrorTabController> errorTabControllers = new ArrayList<>();
	
	@Autowired
	private ConfigService config;
	
	@Autowired
	private ReplayFinder replayFinder;
	
	@Autowired
	private MpqBuilderService mpqBuilderService;
	
	@Autowired
	private ThreadPoolExecutor executor;
	
	// Command Line Parameter
	private boolean hasParamCompilePath = false;
	private String paramCompilePath = null;
	private boolean compileAndRun = false;
	private String paramRunPath = null;
	private boolean wasStartedWithParameters;
	
	private ConfigurableApplicationContext appContext;
	
	@Autowired
	private ConfigService configService;
	private NavigationController navigationController;
	
	/**
	 * Entry point of the App.
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
		
		logger.trace("Configuration File of System: {}",
				() -> System.getProperty("log4j.configurationFile")); //$NON-NLS-1$ //$NON-NLS-2$
		logger.info("Launch arguments: " + Arrays.toString(args)); //$NON-NLS-1$
		logger.info("Max Heap Space: " + Runtime.getRuntime().maxMemory() / 1048576L + "mb.");
		
		System.setProperty("javafx.preloader", AppPreloader.class.getCanonicalName());
		launch(args);
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
	 * Called when the App is starting within the UI Thread.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		Thread.currentThread().setName("UI"); //$NON-NLS-1$
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		initParams();
		
		initGUI(primaryStage);
		
		printVariables();
		
		// command line tool build
		if (hasParamCompilePath) {
			buildStartReplayExit(primaryStage);
		} else {
			navigationController.clickHome();
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
		final FXMLLoader loader = new FXMLSpringLoader(appContext);
		try (InputStream is = appContext.getResource("view/Navigation.fxml").getInputStream()) { //$NON-NLS-1$
			root = loader.load(is);
			navigationController = loader.getController();
		} catch (final IOException | NullPointerException e) {
			logger.error("Failed to load Navigation.fxml:", e); //$NON-NLS-1$
			throw new IOException("Failed to load Navigation.fxml.", e);
		}
		final Scene scene = new Scene(root, 1200, 600);
		
		scene.getStylesheets().add(appContext.getResource("view/application.css").getURI().toString()); //$NON-NLS-1$
		scene.getStylesheets().add(appContext.getResource("view/textStyles.css").getURI().toString()); //$NON-NLS-1$
		
		// app icon
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		} catch (final Exception e) {
			logger.error("Failed to load ahli.png"); //$NON-NLS-1$
		}
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Interface Builder"); //$NON-NLS-1$
		
		// Fade animation (to hide white stage background flash)
		primaryStage.setOpacity(0);
		final FadeTransition ft = new FadeTransition(Duration.millis(750), root);
		ft.setFromValue(0);
		ft.setToValue(1.0);
		ft.play();
		
		primaryStage.show();
		primaryStage.setOpacity(1);
		logger.info("Initializing App..."); //$NON-NLS-1$
		
		// hide apps splash screen image
		if (!GraphicsEnvironment.isHeadless()) {
			final SplashScreen splash = SplashScreen.getSplashScreen();
			if (splash != null) {
				splash.close();
			}
		}
		
		hidePreloader();
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables() {
		logger.info("basePath: " + config.getBasePath()); //$NON-NLS-1$
		logger.info("documentsPath: " + config.getDocumentsPath()); //$NON-NLS-1$
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
	
	private void buildStartReplayExit(final Stage stage) {
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setName("Supervisor"); //$NON-NLS-1$
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					final ThreadPoolExecutor executorTmp = getExecutor();
					Platform.runLater(() -> navigationController.lockNavToProgress());
					
					mpqBuilderService.build(paramCompilePath);
					
					while (!executorTmp.getQueue().isEmpty() || executorTmp.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					startReplayOrQuitOrShowError(stage);
					Platform.runLater(() -> navigationController.unlockNav());
					executorTmp.shutdown();
					executorTmp.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (final Exception e) {
					logger.fatal("FATAL ERROR: ", e);
				}
			}
		}.start();
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
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	/**
	 * @return the executor
	 */
	public ThreadPoolExecutor getExecutor() {
		return executor;
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
		for (final ErrorTabController ctrl : errorTabControllers) {
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
			final SettingsIniInterface settings = configService.getIniSettings();
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
		
		final File replay = replayFinder.getLastUsedOrNewestReplay(isHeroes, config.getDocumentsPath());
		if (replay != null && replay.exists() && replay.isFile()) {
			logger.info("Starting game with replay: " + replay.getName()); //$NON-NLS-1$
			final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() +
					"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// logging.trace("executing: " + cmd); //$NON-NLS-1$
			try {
				Runtime.getRuntime().exec(cmd);
				// logging.trace("after Start attempt..."); //$NON-NLS-1$
			} catch (final IOException e) {
				logger.error("Failed to execute the game launch command.", e); //$NON-NLS-1$
			}
		} else {
			logger.error("Failed to find any replay."); //$NON-NLS-1$
		}
		printInfoLogMessageToGeneral("The game starts with a replay now..."); //$NON-NLS-1$
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
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabTitle
	 */
	public void addThreadLoggerTab(final String threadName, final String tabTitle) {
		final Tab newTab = new Tab();
		final StyleClassedTextArea newTxtArea = new StyleClassedTextArea();
		final ErrorTabController errorTabCtrl = new ErrorTabController(newTab, newTxtArea, true, false);
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
	 * @return
	 */
	public TabPane getTabPane() {
		return TabPaneController.getInstance().getTabPane();
	}
	
	
	@Override
	public void init() {
		if (instance == null) {
			instance = this;
			logger.trace("Application singleton successfull.");
		} else {
			logger.error("Application cannot be started multiple times.");
			throw new ExceptionInInitializerError("Application cannot be started multiple times.");
		}
		// start Spring
		appContext = SpringApplication.run(InterfaceBuilderApp.class, getParameters().getRaw().toArray(new String[0]));
		// trigger autowiring of this JavaFX-created instance
		final AutowireCapableBeanFactory autowireCapableBeanFactory = appContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);
		autowireCapableBeanFactory.initializeBean(this, getClass().getName());
		
	}
	
	/**
	 * Adds an ErrorTabController.
	 *
	 * @param errorTabCtrl
	 */
	public void addErrorTabController(final ErrorTabController errorTabCtrl) {
		errorTabControllers.add(errorTabCtrl);
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
		logger.info("App waves Goodbye!"); //$NON-NLS-1$
		appContext.stop();
	}
	
	public ConfigurableApplicationContext getAppContext() {
		return appContext;
	}
	
	public NavigationController getNavigationController() {
		return navigationController;
	}
}
