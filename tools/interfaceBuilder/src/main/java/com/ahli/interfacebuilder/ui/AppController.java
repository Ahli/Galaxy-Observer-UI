// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui;

import com.ahli.galaxy.game.GameDef;
import com.ahli.interfacebuilder.AppClosingEvent;
import com.ahli.interfacebuilder.PrimaryStageReadyEvent;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.ReplayService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import com.ahli.interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.navigation.Notification;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Log4j2
public class AppController {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	@Getter
	private final ForkJoinPool executor;
	private final BaseUiService baseUiService;
	private final MpqBuilderService mpqBuilderService;
	private final GameService gameService;
	private final ConfigService configService;
	private final ConfigurableApplicationContext appContext;
	private final ReplayService replayService;
	private final ProgressController progressController;
	private final PrimaryStageHolder primaryStage;
	private NavigationController navigationController;
	
	public AppController(
			final ForkJoinPool executor,
			final BaseUiService baseUiService,
			final MpqBuilderService mpqBuilderService,
			final GameService gameService,
			final ConfigService configService,
			final ReplayService replayService,
			final ConfigurableApplicationContext appContext,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
		this.executor = executor;
		this.baseUiService = baseUiService;
		this.mpqBuilderService = mpqBuilderService;
		this.gameService = gameService;
		this.configService = configService;
		this.replayService = replayService;
		this.appContext = appContext;
		this.progressController = progressController;
		this.primaryStage = primaryStage;
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
				log.error(msg);
			} catch (final Exception e) {
				log.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	@EventListener
	public void onPrimaryStageReadyEvent(final PrimaryStageReadyEvent stageReadyEvent) {
		try {
			primaryStage.setPrimaryStage(stageReadyEvent.getStage());
			if (primaryStage.hasPrimaryStage()) {
				initUi();
			}
			
			final CommandLineParams startingParams = stageReadyEvent.getStartingParams();
			printVariables(startingParams);
			
			if (startingParams.isHasParamCompilePath()) {
				// command line tool build
				new Thread(() -> buildStartReplayExit(startingParams)).start();
			} else {
				checkBaseUiUpdate();
			}
		} catch (final IOException e) {
			throw new AppControllerException(e);
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
			log.error("Failed to load Navigation.fxml:", e);
			throw new IOException("Failed to load Navigation.fxml.", e);
		}
		final Scene scene = new Scene(root, 1200, 600);
		
		scene.getStylesheets().add(appContext.getResource("classpath:view/application.css").getURI().toString());
		scene.getStylesheets().add(appContext.getResource("classpath:view/textStyles.css").getURI().toString());
		final Stage ps = primaryStage.getPrimaryStage();
		// app icon
		try {
			ps.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png")));
		} catch (final Exception e) {
			final String msg = "Failed to load ahli.png";
			log.error(msg);
			log.trace(msg, e);
		}
		ps.setMaximized(true);
		ps.setScene(scene);
		ps.setTitle(Messages.getString("app.title"));
		
		// Fade animation (to hide white stage background flash)
		ps.setOpacity(0);
		final FadeTransition ft = new FadeTransition(Duration.millis(750), root);
		ft.setFromValue(0);
		ft.setToValue(1.0);
		ft.play();
		
		ps.show();
		ps.setOpacity(1);
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables(final CommandLineParams params) {
		log.info("basePath: {}", configService.getBasePath());
		log.info("documentsPath: {}", configService.getDocumentsPath());
		final String paramCompilePath = params.getParamCompilePath();
		if (paramCompilePath != null) {
			log.info("compile param path: {}", paramCompilePath);
			if (params.isCompileAndRun()) {
				log.info("run after compile: true");
			}
		}
		final String paramRunPath = params.getParamRunPath();
		if (paramRunPath != null) {
			log.info("run param path: {}", paramRunPath);
		}
	}
	
	public void buildStartReplayExit(final CommandLineParams params) {
		try {
			Thread.currentThread().setName("Supervisor");
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			if (primaryStage.hasPrimaryStage()) {
				Platform.runLater(() -> {
					try {
						navigationController.lockNavToProgress();
					} catch (final Exception e) {
						log.fatal(FATAL_ERROR, e);
					}
				});
			}
			
			mpqBuilderService.build(Path.of(params.getParamCompilePath()));
			
			final var executorTmp = getExecutor();
			if (executorTmp != null && executorTmp.awaitQuiescence(15, TimeUnit.MINUTES)) {
				startReplayOrQuitOrShowError(params);
			}
			
			if (primaryStage.hasPrimaryStage()) {
				Platform.runLater(() -> {
					try {
						navigationController.unlockNav();
					} catch (final Exception e) {
						log.fatal(FATAL_ERROR, e);
					}
				});
			}
		} catch (final Exception e) {
			log.fatal(FATAL_ERROR, e);
		}
	}
	
	public void checkBaseUiUpdate() {
		try {
			if (baseUiService.isOutdated(GameType.SC2, false)) {
				if (primaryStage.hasPrimaryStage()) {
					navigationController.appendNotification(new Notification(Messages.getString(
							"browse.notification.sc2OutOfDate"), NavigationController.BROWSE_TAB, "sc2OutOfDate"));
				} else {
					log.warn(Messages.getString("browse.notification.sc2OutOfDate"));
				}
			}
		} catch (final IOException e) {
			log.error("Error during SC2 baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(GameType.SC2, true)) {
				if (primaryStage.hasPrimaryStage()) {
					navigationController.appendNotification(new Notification(
							Messages.getString("browse.notification.sc2PtrOutOfDate"),
							NavigationController.BROWSE_TAB,
							"sc2PtrOutOfDate"));
				} else {
					log.warn(Messages.getString("browse.notification.sc2PtrOutOfDate"));
				}
			}
		} catch (final IOException e) {
			log.error("Error during SC2 PTR baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(GameType.HEROES, false)) {
				if (primaryStage.hasPrimaryStage()) {
					navigationController.appendNotification(new Notification(
							Messages.getString("browse.notification.heroesOutOfDate"),
							NavigationController.BROWSE_TAB,
							"heroesOutOfDate"));
				} else {
					log.warn(Messages.getString("browse.notification.heroesOutOfDate"));
				}
			}
		} catch (final IOException e) {
			log.error("Error during Heroes baseUI update check.", e);
		}
		try {
			if (baseUiService.isOutdated(GameType.HEROES, true)) {
				if (primaryStage.hasPrimaryStage()) {
					navigationController.appendNotification(new Notification(
							Messages.getString("browse.notification.heroesPtrOutOfDate"),
							NavigationController.BROWSE_TAB,
							"heroesPtrOutOfDate"));
				} else {
					log.warn(Messages.getString("browse.notification.heroesPtrOutOfDate"));
				}
			}
		} catch (final IOException e) {
			log.error("Error during Heroes PTR baseUI update check.", e);
		}
	}
	
	/**
	 * Starts a Replay, quits the Application or remains alive to show an error. Action depends on fields.
	 *
	 * @param params
	 * 		command line parameters
	 */
	private void startReplayOrQuitOrShowError(final CommandLineParams params) {
		if (params.isWasStartedWithParameters()) {
			if (!progressController.anyErrorTrackerEncounteredError()) {
				// start game, launch replay
				runGameWithReplay(params);
				
				// app started with params => potentially close itself
				if (!params.isParamsOriginateFromExternalSource() && primaryStage.hasPrimaryStage()) {
					if (!params.isHasParamCompilePath()) {
						Platform.runLater(() -> {
							try {
								// close after 5 seconds, if compiled all and no errors
								final PauseTransition delay = new PauseTransition(Duration.seconds(5));
								delay.setOnFinished(_ -> primaryStage.getPrimaryStage().close());
								delay.play();
							} catch (final Exception e) {
								log.fatal(FATAL_ERROR, e);
							}
						});
					} else {
						// close instantly, if only run or something else
						Platform.runLater(() -> {
							try {
								primaryStage.getPrimaryStage().close();
							} catch (final Exception e) {
								log.fatal(FATAL_ERROR, e);
							}
						});
					}
				}
			} else {
				// clear errors
				progressController.clearErrorTrackers();
			}
		}
		// allow client to exit
		InterProcessCommunicationAppender.sendTerminationSignal();
	}
	
	/**
	 * Attempts to run a game with a replay, if desired.
	 *
	 * @return true if a process was started with a replay parameter; else false
	 */
	public void runGameWithReplay(final CommandLineParams params) {
		final boolean isHeroes;
		final String gamePath;
		
		if (!params.isCompileAndRun()) {
			// use the run param
			gamePath = params.getParamRunPath();
			if (gamePath == null) {
				log.error("Command's game path parameter must not be empty!");
				return;
			}
			final Path path = Path.of(gamePath);
			final var heroes = gameService.getGameDefHeroes();
			final var sc2 = gameService.getGameDefSc2();
			isHeroes = gamePath.endsWith(heroes.supportDirectoryX64() + File.separator + heroes.switcherExeNameX64());
			final boolean isSc2x64 =
					gamePath.endsWith(sc2.supportDirectoryX64() + File.separator + sc2.switcherExeNameX64());
			final boolean isSc2x32 =
					gamePath.endsWith(sc2.supportDirectoryX32() + File.separator + sc2.switcherExeNameX32());
			if ((!isHeroes && !isSc2x64 && !isSc2x32) || !Files.exists(path) || !Files.isExecutable(path)) {
				log.error(
						"Failed to validate game path '{}'! Please provide the path to the Switcher executable.",
						gamePath);
				return;
			}
		} else {
			final SettingsIniInterface settings = configService.getIniSettings();
			// compileAndRun is active -> figure out the right game
			if (params.getParamCompilePath().contains(File.separator + "heroes" + File.separator)) {
				// Heroes
				isHeroes = true;
				final GameDef gameDef = gameService.getGameDefHeroes();
				final boolean isPtr = baseUiService.isPtrActive(gameDef);
				final String supportDir = gameDef.supportDirectoryX64();
				final String switcherExe = gameDef.switcherExeNameX64();
				gamePath =
						(isPtr ? settings.getHeroesPtrPath() : settings.getHeroesPath()) + File.separator + supportDir +
								File.separator + switcherExe;
			} else {
				// SC2
				isHeroes = false;
				final GameDef gameDef = gameService.getGameDefSc2();
				final boolean isPtr = baseUiService.isPtrActive(gameDef);
				final boolean is64bit = isPtr ? settings.isSc2PtrX64() : settings.isSc2X64();
				final String supportDir = is64bit ? gameDef.supportDirectoryX64() : gameDef.supportDirectoryX32();
				final String switcherExe = is64bit ? gameDef.switcherExeNameX64() : gameDef.switcherExeNameX32();
				gamePath = (isPtr ? settings.getSc2PtrPath() : settings.getSc2Path()) + File.separator + supportDir +
						File.separator + switcherExe;
			}
		}
		log.info("Game location: {}", gamePath);
		try {
			final Path replay = replayService.getLastUsedOrNewestReplay(isHeroes, configService.getDocumentsPath());
			if (replay != null && Files.exists(replay) && Files.isRegularFile(replay)) {
				log.info("Starting game with replay: {}", replay);
				try {
					primaryStage.printInfoLogMessageToGeneral("The game starts with a replay now...");
					final String[] cmd = new String[] { "cmd", "/C", "start",
							"\"\" \"" + gamePath + "\" \"" + replay.toAbsolutePath() + "\"" };
					Runtime.getRuntime().exec(cmd);
				} catch (final IOException e) {
					log.error("Failed to execute the game launch command.", e);
				}
			} else {
				log.error("Failed to find any replay.");
			}
		} catch (final IOException e) {
			log.error("Error while finding replay.", e);
		}
	}
	
	@EventListener
	public void onAppClosingEvent(final AppClosingEvent appCLosingEvent) {
		log.info("App is about to shut down.");
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		} else {
			log.info("Executor was already shut down.");
		}
		try {
			//noinspection ResultOfMethodCallIgnored
			executor.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			log.error(
					"ERROR: Executor timed out waiting for Worker Threads to terminate. A Thread might run rampage.",
					e);
			Thread.currentThread().interrupt();
		}
		log.info("App waves Goodbye!");
	}
	
	private static final class AppControllerException extends RuntimeException {
		
		@Serial
		private static final long serialVersionUID = -4067724426665591353L;
		
		private AppControllerException(final Exception e) {
			super(e);
		}
	}
}
