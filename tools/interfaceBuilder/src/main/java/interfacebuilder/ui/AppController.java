// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui;

import com.ahli.galaxy.game.GameDef;
import com.ahli.util.StringInterner;
import interfacebuilder.JavafxApplication;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.i18n.Messages;
import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.ReplayService;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import interfacebuilder.ui.navigation.NavigationController;
import interfacebuilder.ui.navigation.Notification;
import interfacebuilder.ui.progress.ErrorTabController;
import interfacebuilder.ui.progress.TabPaneController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class AppController implements CleaningForkJoinTaskCleaner {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	private static final Logger logger = LogManager.getLogger(AppController.class);
	private final List<ErrorTabController> errorTabControllers;
	private ForkJoinPool executor;
	private TabPaneController tabPaneController;
	private BaseUiService baseUiService;
	private MpqBuilderService mpqBuilderService;
	private GameService gameService;
	private ConfigService configService;
	private ConfigurableApplicationContext appContext;
	private Stage primaryStage;
	private NavigationController navigationController;
	private ReplayService replayService;
	
	public AppController() {
		errorTabControllers = new ArrayList<>(0);
	}
	
	/**
	 * Prints a message to the message log.
	 *
	 * @param msg
	 * 		the message
	 */
	public static void printErrorLogMessageToGeneral(final String msg) {
		Platform.runLater(() -> {
			try {
				logger.error(msg);
			} catch (final Exception e) {
				logger.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	// Lazy Constructor injection does not work as java-modules requires access to swap out the proxy class with the bean
	// usual Constructor parameter bean injection does not work due to circular dependencies
	@Autowired
	protected void initBeans(
			final ForkJoinPool executor,
			final TabPaneController tabPaneController,
			@Lazy final BaseUiService baseUiService,
			@Lazy final MpqBuilderService mpqBuilderService,
			final GameService gameService,
			final ConfigService configService,
			final ReplayService replayService,
			final ConfigurableApplicationContext appContext) {
		this.executor = executor;
		this.tabPaneController = tabPaneController;
		this.baseUiService = baseUiService;
		this.mpqBuilderService = mpqBuilderService;
		this.gameService = gameService;
		this.configService = configService;
		this.replayService = replayService;
		this.appContext = appContext;
	}
	
	/**
	 * After a short delay, the app attempts to clean up its resources,
	 */
	@Override
	@SuppressWarnings("java:S1215")
	public void tryCleanUp() {
		// new delayed thread is required, else the ForkJoinPool is not finished
		new Thread(() -> {
			// free space of baseUI
			if (executor != null && executor.isQuiescent() && mpqBuilderService != null) {
				logger.debug("Freeing up resources");
				mpqBuilderService.getGameData(Game.SC2).setUiCatalog(null);
				mpqBuilderService.getGameData(Game.HEROES).setUiCatalog(null);
				// GC1 is the default GC and can now release RAM -> actually good to do after a task because we use a
				// lot of RAM for the UIs
				// Weak References survive 3 garbage collections by default
				System.gc();
				System.gc();
				System.gc();
				System.runFinalization();
				// clean up StringInterner's weak references that the GC removed
				logger.trace("string interner size before cleaning: {}", StringInterner::size);
				StringInterner.cleanUpGarbage();
				logger.trace("string interner size after cleaning: {}", StringInterner::size);
			}
		}).start();
	}
	
	@EventListener
	public void onPrimaryStageReadyEvent(final JavafxApplication.PrimaryStageReadyEvent stageReadyEvent) {
		try {
			primaryStage = stageReadyEvent.getStage();
			initUi();
			
			final CommandLineParams startingParams = stageReadyEvent.getStartingParams();
			printVariables(startingParams);
			
			if (startingParams.isHasParamCompilePath()) {
				// command line tool build
				buildStartReplayExit(startingParams);
			} else {
				checkBaseUiUpdate();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initUi() throws IOException {
		// Build Navigation
		final Parent root;
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
	
	public void buildStartReplayExit(final CommandLineParams params) {
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
					startReplayOrQuitOrShowError(params);
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
	
	public void checkBaseUiUpdate() {
		try {
			if (baseUiService.isOutdated(Game.SC2, false)) {
				navigationController.appendNotification(new Notification(Messages.getString(
						"browse.notification.sc2OutOfDate"), NavigationController.BROWSE_TAB, "sc2OutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during SC2 baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, false)) {
				navigationController.appendNotification(new Notification(Messages.getString(
						"browse.notification.heroesOutOfDate"), NavigationController.BROWSE_TAB, "heroesOutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, true)) {
				navigationController.appendNotification(new Notification(Messages.getString(
						"browse.notification.heroesPtrOutOfDate"),
						NavigationController.BROWSE_TAB,
						"heroesPtrOutOfDate"));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes PTR baseUI update check.", e);
		}
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
	 * @param params
	 * 		command line parameters
	 */
	private void startReplayOrQuitOrShowError(final CommandLineParams params) {
		if (params.isWasStartedWithParameters()) {
			if (!anyErrorTrackerEncounteredError()) {
				// start game, launch replay
				runGameWithReplay(params);
				
				// app started with params => potentially close itself
				if (!params.isParamsOriginateFromExternalSource() && primaryStage != null) {
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
			if (ctrl.hasEncounteredError() && ctrl.isErrorPreventsExit()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Attempts to run a game with a replay, if desired.
	 *
	 * @return true if a process was started with a replay parameter; else false
	 */
	public boolean runGameWithReplay(final CommandLineParams params) {
		final boolean isHeroes;
		final String gamePath;
		
		if (!params.isCompileAndRun()) {
			// use the run param
			gamePath = params.getParamRunPath();
			if (gamePath == null) {
				return false;
			}
			isHeroes = gamePath.contains("HeroesSwitcher");
		} else {
			final SettingsIniInterface settings = configService.getIniSettings();
			// compileAndRun is active -> figure out the right game
			final GameDef gameDef;
			if (params.getParamCompilePath().contains(File.separator + "heroes" + File.separator)) {
				// Heroes
				isHeroes = true;
				gameDef = gameService.getGameDef(Game.HEROES);
				final boolean isPtr = baseUiService.isHeroesPtrActive();
				final String supportDir = gameDef.getSupportDirectoryX64();
				final String swicherExe = gameDef.getSwitcherExeNameX64();
				gamePath =
						(isPtr ? settings.getHeroesPtrPath() : settings.getHeroesPath()) + File.separator + supportDir +
								File.separator + swicherExe;
			} else {
				// SC2
				isHeroes = false;
				gameDef = gameService.getGameDef(Game.SC2);
				final boolean is64bit = settings.isSc64bit();
				final String supportDir = is64bit ? gameDef.getSupportDirectoryX64() : gameDef.getSupportDirectoryX32();
				final String swicherExe = is64bit ? gameDef.getSwitcherExeNameX64() : gameDef.getSwitcherExeNameX32();
				gamePath = settings.getSc2Path() + File.separator + supportDir + File.separator + swicherExe;
			}
		}
		logger.info("Game location: {}", gamePath);
		
		final File replay = replayService.getLastUsedOrNewestReplay(isHeroes, configService.getDocumentsPath());
		if (replay != null && replay.exists() && replay.isFile()) {
			logger.info("Starting game with replay: {}", replay.getName());
			try {
				printInfoLogMessageToGeneral("The game starts with a replay now...");
				final String cmd = "cmd /C start \"\" \"" + gamePath + "\" \"" + replay.getAbsolutePath() + "\"";
				Runtime.getRuntime().exec(cmd);
				return true;
			} catch (final IOException e) {
				logger.error("Failed to execute the game launch command.", e);
			}
		} else {
			logger.error("Failed to find any replay.");
		}
		return false;
	}
	
	/**
	 * Clears the errors, so future build attempts can run.
	 */
	private void clearErrorTrackers() {
		for (final ErrorTabController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError() && ctrl.isErrorPreventsExit()) {
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
	public static void printInfoLogMessageToGeneral(final String msg) {
		Platform.runLater(() -> {
			try {
				logger.info(msg);
			} catch (final Exception e) {
				logger.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	@EventListener
	public void onAppClosingEvent(final JavafxApplication.AppClosingEvent appCLosingEvent) {
		logger.info("App is about to shut down.");
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		} else {
			logger.info("Executor was already shut down.");
		}
		try {
			//noinspection ResultOfMethodCallIgnored
			executor.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			logger.error("ERROR: Executor timed out waiting for Worker Theads to terminate. A Thread might run rampage.",
					e);
			Thread.currentThread().interrupt();
		}
		logger.info("App waves Goodbye!");
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabName
	 */
	public void addThreadLoggerTab(
			final String threadName, final String tabName, final boolean errorPreventsExit) {
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
			final ErrorTabController errorTabCtrl =
					new ErrorTabController(newTab, newTxtArea, true, false, errorPreventsExit);
			errorTabCtrl.setRunning(true);
			errorTabControllers.add(errorTabCtrl);
			
			final ScrollPane scrollPane = new ScrollPane(newTxtArea);
			scrollPane.setPannable(true);
			
			// auto-downscrolling
			scrollPane.vvalueProperty().bind(newTxtArea.heightProperty());
			
			newTab.setContent(scrollPane);
			StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new CloseThreadLoggerTabAction(newTab, errorTabCtrl, errorTabControllers));
			contextMenu.getItems().addAll(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			final Tab newTabFinal = newTab;
			Platform.runLater(() -> {
				try {
					getTabPane().getTabs().add(newTabFinal);
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
						errorTabCtrl.setErrorPreventsExit(errorPreventsExit);
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
		return tabPaneController.getTabPane();
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
	 * Returns the primary Stage of the App.
	 *
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	private static final class CloseThreadLoggerTabAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final ErrorTabController controller;
		private final List<ErrorTabController> controllers;
		
		private CloseThreadLoggerTabAction(
				final Tab tab, final ErrorTabController controller, final List<ErrorTabController> controllers) {
			this.tab = tab;
			this.controller = controller;
			this.controllers = controllers;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			tab.setContextMenu(null);
			controllers.remove(controller);
		}
	}
	
	
}
