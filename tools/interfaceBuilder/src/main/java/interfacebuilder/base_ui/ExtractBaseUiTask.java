package interfacebuilder.base_ui;

import interfacebuilder.projects.enums.Game;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.ui.progress.ErrorTabController;
import interfacebuilder.ui.progress.appender.Appender;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

public class ExtractBaseUiTask extends CleaningForkJoinTask {
	
	private final Game game;
	private final boolean usePtr;
	private final Appender[] output;
	private final BaseUiService baseUiService;
	private final ErrorTabController errorTabController;
	
	public ExtractBaseUiTask(final BaseUiService baseUiService, final Game game, final boolean usePtr,
			final Appender[] output, final ErrorTabController errorTabController) {
		this.baseUiService = baseUiService;
		this.game = game;
		this.usePtr = usePtr;
		this.output = output;
		this.errorTabController = errorTabController;
	}
	
	@Override
	protected boolean work() {
		final List<ForkJoinTask<Void>> tasks = baseUiService.extract(game, usePtr, output);
		
		for (final var task : tasks) {
			task.fork();
		}
		
		for (final var task : tasks) {
			task.join();
		}
		
		Platform.runLater(() -> {
			errorTabController.setRunning(false);
		});
		return true;
	}
}
