// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.util.StringInterner;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.AppConfiguration;
import interfacebuilder.config.ConfigService;
import interfacebuilder.config.FxmlConfiguration;
import interfacebuilder.i18n.Messages;
import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.InterProcessCommunication;
import interfacebuilder.integration.ReplayFinder;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.FXMLSpringLoader;
import interfacebuilder.ui.navigation.NavigationController;
import interfacebuilder.ui.navigation.Notification;
import interfacebuilder.ui.progress.ErrorTabController;
import interfacebuilder.ui.progress.TabPaneController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Interface Compiler and Builder Application.
 *
 * @author Ahli
 */

@EnableAutoConfiguration (excludeName = { // exclude based on beans in context on runtime
		"org.springframework.boot.autoconfigure.aop.AopAutoConfiguration", // not required
		//"org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration", // required for Resources
		//"org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration", // req for Resources
		"org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
		"org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration",
		//"org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration", // required for JPA Repository
		"org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration",
		//"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
		"org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
		//"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration",
		"org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
		//"org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration" })
@Import ({ AppConfiguration.class, FxmlConfiguration.class })
public class InterfaceBuilderApp extends Application {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	public static final int INTER_PROCESS_COMMUNICATION_PORT = 12317;
	private static final Logger logger = LogManager.getLogger(InterfaceBuilderApp.class);
	public static boolean javaFxInitialized;
	private static InterfaceBuilderApp instance;
	private final List<ErrorTabController> errorTabControllers;
	//	static {
	//				System.setProperty("log4j2.debug", "true");
	//	}
	@Autowired
	private ReplayFinder replayFinder;
	@Autowired
	private MpqBuilderService mpqBuilderService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private BaseUiService baseUiService;
	@Autowired
	private GameService gameService;
	@Autowired
	private ForkJoinPool executor;
	private ConfigurableApplicationContext appContext;
	private Stage primaryStage;
	private NavigationController navigationController;
	
	public InterfaceBuilderApp() {
		errorTabControllers = new ArrayList<>(0);
	}
	
	/**
	 * Entry point of the App.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void init(final String[] args) {
		if (InterProcessCommunication.initInterProcessCommunication(args, INTER_PROCESS_COMMUNICATION_PORT)) {
			// this is the server
			logger.trace("System's Log4j2 Configuration File: {}", () -> System.getProperty("log4j.configurationFile"));
			logger.info("Launch arguments: {}", () -> Arrays.toString(args));
			logger.info("Max Heap Space: {}mb.", Runtime.getRuntime().maxMemory() / 1_048_576L);
			
			// set preloader while booting javafx
			System.setProperty("javafx.preloader", AppPreloader.class.getCanonicalName());
			
			// initialize JavaFX
			launch(args);
		}
		// else: this is the client and communication with the server ended already -> exit
	}
	
	/**
	 * After a short delay, the app attempts to clean up its resources,
	 */
	public static void tryCleanUp() {
		// new delayed thread is required, else the ForkJoinPool is not finished
		Platform.runLater(() -> {
			final InterfaceBuilderApp instance = InterfaceBuilderApp.getInstance();
			// free space of baseUI
			if (instance != null && instance.executor != null && instance.mpqBuilderService != null &&
					instance.executor.isQuiescent()) {
				logger.info("Freeing up resources");
				instance.mpqBuilderService.getGameData(Game.SC2).setUiCatalog(null);
				instance.mpqBuilderService.getGameData(Game.HEROES).setUiCatalog(null);
				// GC1 is the default GC and can now release RAM -> actually good to do after a task because we use a
				// lot of RAM for the UIs
				// Weak References survive 3 garbage collections by default
				for (int i = 0; i < 3; ++i) {
					System.gc();
					System.runFinalization();
				}
				try {
					Thread.sleep(200);
					// clean up StringInterner's weak references that the GC removed
					logger.trace("string interner size before cleaning: {}", StringInterner::size);
					StringInterner.cleanUpGarbage();
					logger.trace("string interner size after cleaning: {}", StringInterner::size);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
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
	 * Returns the primary Stage of the App.
	 *
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	@Override
	public void init() {
		// initialize Spring
		try {
			appContext =
					SpringApplication.run(InterfaceBuilderApp.class, getParameters().getRaw().toArray(new String[0]));
		} catch (final IllegalStateException e) {
			logger.fatal(e);
			throw e;
		}
		// trigger autowiring of this JavaFX-created instance
		final AutowireCapableBeanFactory autowireCapableBeanFactory = appContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);
		autowireCapableBeanFactory.initializeBean(this, getClass().getName());
	}
	
	/**
	 * Called when the App is starting within the UI Thread.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		if (instance == null) {
			instance = this;
			logger.trace("Application singleton successful.");
		} else {
			logger.error("Application cannot be started multiple times.");
			throw new ExceptionInInitializerError("Application cannot be started multiple times.");
		}
		
		javaFxInitialized = true;
		Thread.currentThread().setName("UI");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		this.primaryStage = primaryStage;
		
		final CommandLineParams startingParams = initParams();
		
		initGUI(primaryStage);
		
		printVariables(startingParams);
		
		// command line tool build
		if (startingParams.isHasParamCompilePath()) {
			buildStartReplayExit(primaryStage, startingParams);
		} else {
			navigationController.clickHome();
			
			checkBaseUiUpdate();
		}
	}
	
	/**
	 * Turns App's parameters into variables.
	 */
	private CommandLineParams initParams() {
		return new CommandLineParams(getParameters());
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
		// Build Navigation
		final BorderPane root;
		final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
		try {
			root = loader.load("classpath:view/Navigation.fxml");
			navigationController = loader.getController();
		} catch (final IOException e) {
			logger.error("Failed to load Navigation.fxml:", e);
			throw new IOException("Failed to load Navigation.fxml.", e);
		}
		final Scene scene = new Scene(root, 1200, 600);
		
		scene.getStylesheets().add(appContext.getResource("classpath:view/application.css").getURI().toString());
		scene.getStylesheets().add(appContext.getResource("classpath:view/textStyles.css").getURI().toString());
		
		// app icon
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png")));
		} catch (final Exception e) {
			final String msg = "Failed to load ahli.png";
			logger.error(msg);
			logger.trace(msg, e);
		}
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle(Messages.getString("app.title"));
		
		// Fade animation (to hide white stage background flash)
		primaryStage.setOpacity(0);
		final FadeTransition ft = new FadeTransition(Duration.millis(750), root);
		ft.setFromValue(0);
		ft.setToValue(1.0);
		ft.play();
		
		primaryStage.show();
		primaryStage.setOpacity(1);
		logger.info("Initializing App...");
		
		hidePreloader();
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables(final CommandLineParams params) {
		logger.info("basePath: {}", configService.getBasePath());
		logger.info("documentsPath: {}", configService.getDocumentsPath());
		final String paramCompilePath = params.getParamCompilePath();
		if (paramCompilePath != null) {
			logger.info("compile param path: {}", paramCompilePath);
			if (params.isCompileAndRun()) {
				logger.info("run after compile: true");
			}
		}
		final String paramRunPath = params.getParamRunPath();
		if (paramRunPath != null) {
			logger.info("run param path: {}", paramRunPath);
		}
	}
	
	public void buildStartReplayExit(final Stage stage, final CommandLineParams params) {
		new Thread(() -> {
			try {
				Thread.currentThread().setName("Supervisor");
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				
				Platform.runLater(() -> {
					try {
						navigationController.lockNavToProgress();
					} catch (final Exception e) {
						logger.fatal(FATAL_ERROR, e);
					}
				});
				
				mpqBuilderService.build(params.getParamCompilePath());
				
				final var executorTmp = getExecutor();
				if (executorTmp != null && executorTmp.awaitQuiescence(15, TimeUnit.MINUTES)) {
					startReplayOrQuitOrShowError(stage, params);
				}
				
				Platform.runLater(() -> {
					try {
						navigationController.unlockNav();
					} catch (final Exception e) {
						logger.fatal(FATAL_ERROR, e);
					}
				});
			} catch (final Exception e) {
				logger.fatal(FATAL_ERROR, e);
			} finally {
				InterProcessCommunicationAppender.sendTerminationSignal();
			}
		}).start();
	}
	
	private void checkBaseUiUpdate() {
		try {
			if (baseUiService.isOutdated(Game.SC2, false)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.sc2OutOfDate"),
								NavigationController.BROWSE_TAB, "sc2OutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during SC2 baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, false)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.heroesOutOfDate"),
								NavigationController.BROWSE_TAB, "heroesOutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, true)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.heroesPtrOutOfDate"),
								NavigationController.BROWSE_TAB, "heroesPtrOutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes PTR baseUI update check.", e);
		}
	}
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	/**
	 * @return the executor
	 */
	public ForkJoinPool getExecutor() {
		return executor;
	}
	
	/**
	 * Starts a Replay, quits the Application or remains alive to show an error. Action depends on fields.
	 *
	 * @param primaryStage
	 * 		app's main stage
	 */
	private void startReplayOrQuitOrShowError(final Stage primaryStage, final CommandLineParams params) {
		if (params.isWasStartedWithParameters()) {
			if (!anyErrorTrackerEncounteredError()) {
				// start game, launch replay
				attemptToRunGameWithReplay(params);
				
				// app started with params => potentially close itself
				if (!params.isParamsOriginateFromExternalSource()) {
					if (!params.isHasParamCompilePath()) {
						Platform.runLater(() -> {
							try {
								// close after 5 seconds, if compiled all and no errors
								final PauseTransition delay = new PauseTransition(Duration.seconds(5));
								delay.setOnFinished(event -> primaryStage.close());
								delay.play();
							} catch (final Exception e) {
								logger.fatal(FATAL_ERROR, e);
							}
						});
					} else {
						// close instantly, if only run or something else
						Platform.runLater(() -> {
							try {
								primaryStage.close();
							} catch (final Exception e) {
								logger.fatal(FATAL_ERROR, e);
							}
						});
					}
				}
			} else {
				// clear errors
				clearErrorTrackers();
			}
		}
		// allow client to exit
		InterProcessCommunicationAppender.sendTerminationSignal();
	}
	
	/**
	 * @return
	 */
	private boolean anyErrorTrackerEncounteredError() {
		for (final ErrorTabController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError() && !ctrl.isErrorsDoNotPreventExit()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Attempts to run a game with a replay, if desired.
	 */
	public void attemptToRunGameWithReplay(final CommandLineParams params) {
		final boolean isHeroes;
		final String gamePath;
		
		if (!params.isCompileAndRun()) {
			// use the run param
			gamePath = params.getParamRunPath();
			if (gamePath == null) {
				return;
			}
			isHeroes = gamePath.contains("HeroesSwitcher");
		} else {
			final SettingsIniInterface settings = configService.getIniSettings();
			// compileAndRun is active -> figure out the right game
			final GameDef gameDef;
			if (params.getParamCompilePath().contains(File.separator + "heroes" + File.separator)) {
				// Heroes
				isHeroes = true;
				gameDef = gameService.getNewGameDef(Game.HEROES);
				final boolean isPtr = baseUiService.isHeroesPtrActive();
				final String supportDir = gameDef.getSupportDirectoryX64();
				final String swicherExe = gameDef.getSwitcherExeNameX64();
				gamePath =
						(isPtr ? settings.getHeroesPtrPath() : settings.getHeroesPath()) + File.separator + supportDir +
								File.separator + swicherExe;
			} else {
				// SC2
				isHeroes = false;
				gameDef = gameService.getNewGameDef(Game.SC2);
				final boolean is64bit = settings.isSc64bit();
				final String supportDir = is64bit ? gameDef.getSupportDirectoryX64() : gameDef.getSupportDirectoryX32();
				final String swicherExe = is64bit ? gameDef.getSwitcherExeNameX64() : gameDef.getSwitcherExeNameX32();
				gamePath = settings.getSc2Path() + File.separator + supportDir + File.separator + swicherExe;
			}
		}
		logger.info("Game location: {}", gamePath);
		
		final File replay = replayFinder.getLastUsedOrNewestReplay(isHeroes, configService.getDocumentsPath());
		if (replay != null && replay.exists() && replay.isFile()) {
			logger.info("Starting game with replay: {}", replay.getName());
			final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"";
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (final IOException e) {
				logger.error("Failed to execute the game launch command.", e);
			}
		} else {
			logger.error("Failed to find any replay.");
		}
		printInfoLogMessageToGeneral("The game starts with a replay now...");
	}
	
	/**
	 * Clears the errors, so future build attempts can run.
	 */
	private void clearErrorTrackers() {
		for (final ErrorTabController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError() && !ctrl.isErrorsDoNotPreventExit()) {
				ctrl.clearError(false);
			}
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
				logger.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabName
	 */
	public void addThreadLoggerTab(final String threadName, final String tabName,
			final boolean errorsDoNotPreventExit) {
		final ObservableList<Tab> tabs = getTabPane().getTabs();
		Tab newTab = null;
		
		// re-use existing tab with that name
		for (final Tab tab : tabs) {
			if (tab.getText().equals(tabName)) {
				newTab = tab;
				break;
			}
		}
		if (newTab == null) {
			// CASE: new tab
			newTab = new Tab(tabName);
			final TextFlow newTxtArea = new TextFlow();
			newTxtArea.getStyleClass().add("styled-text-area");
			final ErrorTabController errorTabCtrl =
					new ErrorTabController(newTab, newTxtArea, true, false, errorsDoNotPreventExit);
			errorTabCtrl.setRunning(true);
			errorTabControllers.add(errorTabCtrl);
			
			final ScrollPane scrollPane = new ScrollPane(newTxtArea);
			scrollPane.getStyleClass().add("virtualized-scroll-pane");
			newTab.setContent(scrollPane);
			StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			final WeakReference<ErrorTabController> controllerRef = new WeakReference<>(errorTabCtrl);
			final WeakReference<Tab> newTabFinal = new WeakReference<>(newTab);
			closeItem.setOnAction(event -> {
				final Tab t = newTabFinal.get();
				if (t != null) {
					getTabPane().getTabs().remove(t);
				}
				final ErrorTabController c = controllerRef.get();
				if (c != null) {
					errorTabControllers.remove(c);
				}
			});
			contextMenu.getItems().addAll(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			Platform.runLater(() -> {
				try {
					final Tab t = newTabFinal.get();
					if (t != null) {
						getTabPane().getTabs().add(t);
					}
				} catch (final Exception e) {
					logger.fatal(FATAL_ERROR, e);
				}
			});
		} else {
			// CASE: recycle existing Tab
			final ErrorTabController errorTabCtrl = getErrorTabController(tabName);
			if (errorTabCtrl != null) {
				StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
				Platform.runLater(() -> {
					try {
						errorTabCtrl.setErrorsDoNotPreventExit(errorsDoNotPreventExit);
						errorTabCtrl.clearError(false);
						errorTabCtrl.clearWarning(false);
						errorTabCtrl.setRunning(true);
					} catch (final Exception e) {
						logger.fatal(FATAL_ERROR, e);
					}
				});
			}
		}
	}
	
	/**
	 * @return
	 */
	public TabPane getTabPane() {
		return TabPaneController.getInstance().getTabPane();
	}
	
	/**
	 * @param tabName
	 * @return
	 */
	public ErrorTabController getErrorTabController(final String tabName) {
		for (final var ctrl : errorTabControllers) {
			if (ctrl != null) {
				final Tab tab = ctrl.getTab();
				if (tab != null && tabName.equals(tab.getText())) {
					// found the correct one
					return ctrl;
				}
			}
		}
		return null;
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
				logger.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	/**
	 * Is called when the App is closing.
	 */
	@Override
	public void stop() {
		logger.info("App is about to shut down.");
		try {
			InterProcessCommunication.close();
		} catch (final IOException e) {
			logger.error("ERROR: Server Socket had exception when closing.", e);
		}
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		} else {
			logger.info("Executor was already shut down.");
		}
		try {
			executor.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			logger.error(
					"ERROR: Executor timed out waiting for Worker Theads to terminate. A Thread might run rampage.", e);
			Thread.currentThread().interrupt();
		}
		logger.info("App waves Goodbye!");
		appContext.stop();
	}
	
	public NavigationController getNavigationController() {
		return navigationController;
	}
}
