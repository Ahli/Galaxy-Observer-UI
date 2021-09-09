// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.build;

import com.ahli.galaxy.game.Game;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.projects.Project;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serial;

public class BuildTask extends CleaningForkJoinTask {
	
	private static final Logger logger = LogManager.getLogger(BuildTask.class);
	
	@Serial
	private static final long serialVersionUID = 1114165634840947017L;
	
	private final transient Project project;
	private final boolean useCmdLineSettings;
	private final transient MpqBuilderService mpqBuilderService;
	private final transient BaseUiService baseUiService;
	
	public BuildTask(
			final CleaningForkJoinTaskCleaner cleaner,
			final Project project,
			final boolean useCmdLineSettings,
			final MpqBuilderService mpqBuilderService,
			final BaseUiService baseUiService) {
		super(cleaner);
		this.project = project;
		this.useCmdLineSettings = useCmdLineSettings;
		this.mpqBuilderService = mpqBuilderService;
		this.baseUiService = baseUiService;
	}
	
	@Override
	protected boolean work() {
		try {
			final Game game = mpqBuilderService.getGameData(project.getGameType());
			baseUiService.parseBaseUiIfNecessary(game, useCmdLineSettings);
			mpqBuilderService.buildSpecificUI(game, useCmdLineSettings, project);
		} catch (final Exception e) {
			logger.error("Error while building.", e);
			return false;
		}
		return true;
	}
}
