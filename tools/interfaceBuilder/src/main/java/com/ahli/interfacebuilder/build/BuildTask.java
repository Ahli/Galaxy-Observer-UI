// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.build;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTask;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import lombok.extern.log4j.Log4j2;

import java.io.Serial;

@Log4j2
public class BuildTask extends CleaningForkJoinTask {
	
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
			log.error("Error while building.", e);
			return false;
		}
		return true;
	}
}
