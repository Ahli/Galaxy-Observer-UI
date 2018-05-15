package application;

import application.baseUi.BaseUiService;
import application.build.MpqBuilderService;
import application.config.ConfigService;
import application.i18n.Messages;
import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.projects.enums.Game;
import application.ui.FXMLSpringLoader;
import application.ui.navigation.NavigationController;
import application.ui.navigation.Notification;
import application.ui.progress.ErrorTabController;
import application.ui.progress.StylizedTextAreaAppender;
import application.ui.progress.TabPaneController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	private static ServerSocket serverSocket;
	private static Thread serverThread = null;
	
	private final List<ErrorTabController> errorTabControllers = new ArrayList<>();
	@Autowired
	private ConfigService config;
	@Autowired
	private ReplayFinder replayFinder;
	@Autowired
	private MpqBuilderService mpqBuilderService;
	@Autowired
	private ThreadPoolExecutor executor;
	
	private ConfigurableApplicationContext appContext;
	
	@Autowired
	private ConfigService configService;
	private NavigationController navigationController;
	
	@Autowired
	private BaseUiService baseUiService;
	private Stage primaryStage = null;
	
	/**
	 * Entry point of the App.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void main(final String[] args) {
		initInterProcessCommunication(args, 12317);
		
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
	 * Initiates the communication between processes.
	 *
	 * @param args
	 * @param port
	 */
	private static void initInterProcessCommunication(final String[] args, final int port) {
		try {
			serverSocket = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
			serverThread = new Thread() {
				@Override
				public void run() {
					Socket clientSocket;
					while (true) {
						try {
							clientSocket = serverSocket.accept();
							try (final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
							     final BufferedReader in = new BufferedReader(
									     new InputStreamReader(clientSocket.getInputStream()))) {
								String inputLine;
								while ((inputLine = in.readLine()) != null) {
									out.println(inputLine);
									logger.info("message from client: " + inputLine);
									final List<String> params =
											Arrays.asList(inputLine.substring(1, inputLine.length() - 1).split(", "));
									getInstance().executeCommand(params);
								}
							}
						} catch (final IOException e) {
							logger.fatal("I/O Exception while waiting for client connections.", e);
						}
					}
				}
			};
			serverThread.setName("IPCserver");
			serverThread.setDaemon(true);
			serverThread.setPriority(Thread.MIN_PRIORITY);
			serverThread.start();
		} catch (final UnknownHostException e) {
			logger.fatal("Could not retrieve localhost address.", e);
			System.exit(1);
		} catch (final IOException e) {
			// port taken, so app is already running
			logger.info("App already running. Passing over command line arguments.");
			
			try (final Socket socket = new Socket(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), port)) {
				final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				// sending parameters
				out.println(Arrays.toString(args));
			} catch (final IOException e1) {
				logger.fatal("Exception while sending parameters to primary instance.", e1);
				System.exit(1);
				return;
			}
			
			System.exit(0);
		}
	}
	
	/**
	 * Executes the specified command line arguments.
	 *
	 * @param paramList
	 */
	private void executeCommand(final List<String> paramList) {
		// TODO
		final CommandLineParams params = new CommandLineParams(paramList.toArray(new String[0]));
		
		if (params.isHasParamCompilePath()) {
			buildStartReplayExit(InterfaceBuilderApp.getInstance().getPrimaryStage(), params);
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
	
	private void buildStartReplayExit(final Stage stage, final CommandLineParams params) {
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setName("Supervisor"); //$NON-NLS-1$
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					final ThreadPoolExecutor executorTmp = getExecutor();
					Platform.runLater(() -> navigationController.lockNavToProgress());
					
					mpqBuilderService.build(params.getParamCompilePath());
					
					while (!executorTmp.getQueue().isEmpty() || executorTmp.getActiveCount() > 0) {
						Thread.sleep(50);
					}
					startReplayOrQuitOrShowError(stage, params);
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
	
	private Stage getPrimaryStage() {
		return primaryStage;
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
	private void startReplayOrQuitOrShowError(final Stage primaryStage, final CommandLineParams params) {
		if (params.isWasStartedWithParameters() && !anyErrorTrackerEncounteredError()) {
			// start game, launch replay
			attemptToRunGameWithReplay(params);
			if (!params.isHasParamCompilePath()) {
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
			isHeroes = gamePath.contains("HeroesSwitcher.exe"); //$NON-NLS-1$
		} else {
			final SettingsIniInterface settings = configService.getIniSettings();
			// compileAndRun is active -> figure out the right game
			if (params.getParamCompilePath().contains(File.separator + "heroes" + File.separator)) { //$NON-NLS-1$
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
	 * Called when the App is starting within the UI Thread.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		Thread.currentThread().setName("UI"); //$NON-NLS-1$
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
		// UI layout: borderPane -center-> tabPane (via layout) -tab_0-> virtualizedScrollPane -> StyleClassedTextArea
		//                       -left-> navigation button bar
		
		// Build Navigation
		final BorderPane root;
		final FXMLLoader loader = new FXMLSpringLoader(appContext);
		try (final InputStream is = appContext.getResource("view/Navigation.fxml").getInputStream()) { //$NON-NLS-1$
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
		primaryStage.setTitle(Messages.getString("app.title"));
		
		// Fade animation (to hide white stage background flash)
		primaryStage.setOpacity(0);
		final FadeTransition ft = new FadeTransition(Duration.millis(750), root);
		ft.setFromValue(0);
		ft.setToValue(1.0);
		ft.play();
		
		primaryStage.show();
		primaryStage.setOpacity(1);
		logger.info("Initializing App..."); //$NON-NLS-1$
		
		hidePreloader();
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables(final CommandLineParams params) {
		logger.info("basePath: " + config.getBasePath()); //$NON-NLS-1$
		logger.info("documentsPath: " + config.getDocumentsPath()); //$NON-NLS-1$
		final String paramCompilePath = params.getParamCompilePath();
		if (paramCompilePath != null) {
			logger.info("compile param path: " + paramCompilePath); //$NON-NLS-1$
			if (params.isCompileAndRun()) {
				logger.info("run after compile: true"); //$NON-NLS-1$
			}
		}
		final String paramRunPath = params.getParamRunPath();
		if (paramRunPath != null) {
			logger.info("run param path: " + paramRunPath); //$NON-NLS-1$
		}
	}
	
	private void checkBaseUiUpdate() {
		try {
			if (baseUiService.isOutdated(Game.SC2, false)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.sc2OutOfDate"), 3));
			}
		} catch (final IOException e) {
			logger.error("Error during SC2 baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, false)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.heroesOutOfDate"), 3));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(Game.HEROES, true)) {
				navigationController.appendNotification(
						new Notification(Messages.getString("browse.notification.heroesPtrOutOfDate"), 3));
			}
		} catch (final IOException e) {
			logger.error("Error during Heroes PTR baseUI update check.", e);
		}
	}
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabTitle
	 */
	public void addThreadLoggerTab(final String threadName, final String tabTitle,
			final boolean errorsDoNotPreventExit) {
		final Tab newTab = new Tab();
		final StyleClassedTextArea newTxtArea = new StyleClassedTextArea();
		final ErrorTabController errorTabCtrl =
				new ErrorTabController(newTab, newTxtArea, true, false, errorsDoNotPreventExit);
		errorTabCtrl.setRunning(true);
		errorTabControllers.add(errorTabCtrl);
		
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane =
				new VirtualizedScrollPane<>(newTxtArea);
		newTab.setContent(virtualizedScrollPane);
		StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
		newTab.setText(tabTitle);
		newTxtArea.setEditable(false);
		
		// context menu with close option
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem closeItem = new MenuItem("Close");
		closeItem.setOnAction(event -> getTabPane().getTabs().remove(newTab));
		contextMenu.getItems().addAll(closeItem);
		newTab.setContextMenu(contextMenu);
		
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
