// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.base_ui;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.threads.CleaningTask;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.ErrorTabController;
import com.ahli.interfacebuilder.ui.progress.appenders.Appender;
import javafx.application.Platform;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtractBaseUiTask extends CleaningTask {
	private final GameType gameType;
	private final boolean usePtr;
	private final Appender[] output;
	private final BaseUiService baseUiService;
	private final ErrorTabController errorTabController;
	private final NavigationController navigationController;
	
	public ExtractBaseUiTask(
			@NonNull final BaseUiService baseUiService,
			@NonNull final GameType gameType,
			final boolean usePtr,
			@NonNull final Appender[] output,
			@NonNull final ErrorTabController errorTabController,
			@NonNull final NavigationController navigationController,
			final Game sc2Game,
			final Game heroesGame) {
		super(sc2Game, heroesGame);
		this.baseUiService = baseUiService;
		this.gameType = gameType;
		this.usePtr = usePtr;
		this.output = output;
		this.errorTabController = errorTabController;
		this.navigationController = navigationController;
	}
	
	@Override
	public void run() {
		start();
		try {
			final List<Runnable> tasks = baseUiService.createExtractionTasks(gameType, usePtr, output);
			
			Platform.runLater(() -> {
				final String notificationId;
				if (gameType == GameType.SC2) {
					notificationId = "sc2OutOfDate";
				} else if (usePtr && gameType == GameType.HEROES) {
					notificationId = "heroesPtrOutOfDate";
				} else if (gameType == GameType.HEROES) {
					notificationId = "heroesOutOfDate";
				} else {
					return;
				}
				navigationController.closeNotification(notificationId);
			});
			
			try (final ExecutorService e = Executors.newVirtualThreadPerTaskExecutor()) {
				tasks.forEach(e::submit);
			}
			
			Platform.runLater(() -> errorTabController.setRunning(false));
		} finally {
			end();
		}
	}
}
