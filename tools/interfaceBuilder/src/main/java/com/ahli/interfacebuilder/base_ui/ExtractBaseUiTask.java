// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.base_ui;

import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTask;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.ErrorTabController;
import com.ahli.interfacebuilder.ui.progress.appenders.Appender;
import javafx.application.Platform;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

public class ExtractBaseUiTask extends CleaningForkJoinTask {
	
	@Serial
	private static final long serialVersionUID = 4718440389513483542L;
	
	private final GameType gameType;
	private final boolean usePtr;
	private final transient Appender[] output;
	private final transient BaseUiService baseUiService;
	private final transient ErrorTabController errorTabController;
	private final transient NavigationController navigationController;
	
	public ExtractBaseUiTask(
			final CleaningForkJoinTaskCleaner cleaner,
			final BaseUiService baseUiService,
			final GameType gameType,
			final boolean usePtr,
			final Appender[] output,
			final ErrorTabController errorTabController,
			final NavigationController navigationController) {
		super(cleaner);
		this.baseUiService = baseUiService;
		this.gameType = gameType;
		this.usePtr = usePtr;
		this.output = output;
		this.errorTabController = errorTabController;
		this.navigationController = navigationController;
	}
	
	@Override
	protected boolean work() {
		final List<ForkJoinTask<Void>> tasks = baseUiService.createExtractionTasks(gameType, usePtr, output);
		
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
		
		for (final var task : tasks) {
			task.fork();
		}
		
		for (final var task : tasks) {
			task.join();
		}
		
		Platform.runLater(() -> errorTabController.setRunning(false));
		return true;
	}
}
