// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.build;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.threads.CleaningTask;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;

@Log4j2
public class BuildTask extends CleaningTask {
	private final Project project;
	private final boolean useCmdLineSettings;
	private final MpqBuilderService mpqBuilderService;
	private final BaseUiService baseUiService;
	
	public BuildTask(
			@NonNull final Project project,
			final boolean useCmdLineSettings,
			@NonNull final MpqBuilderService mpqBuilderService,
			@NonNull final BaseUiService baseUiService,
			final Game sc2Game,
			final Game heroesGame) {
		super(sc2Game, heroesGame);
		this.project = project;
		this.useCmdLineSettings = useCmdLineSettings;
		this.mpqBuilderService = mpqBuilderService;
		this.baseUiService = baseUiService;
	}
	
	@Override
	public void run() {
		start();
		try {
			final Game game = mpqBuilderService.getGameData(project.getGameType());
			baseUiService.parseBaseUiIfNecessary(game, useCmdLineSettings);
			mpqBuilderService.buildSpecificUI(game, useCmdLineSettings, project);
		} catch (final Exception e) {
			log.error("Error while building.", e);
		} finally {
			end();
		}
	}
}
