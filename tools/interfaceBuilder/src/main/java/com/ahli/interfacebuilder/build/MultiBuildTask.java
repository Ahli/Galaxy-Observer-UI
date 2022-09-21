// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.build;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.threads.CleaningTask;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class MultiBuildTask extends CleaningTask {
	private final List<Project> projects;
	private final boolean useCmdLineSettings;
	private final MpqBuilderService mpqBuilderService;
	private final BaseUiService baseUiService;
	
	public MultiBuildTask(
			@NonNull final List<Project> projects,
			final boolean useCmdLineSettings,
			@NonNull final MpqBuilderService mpqBuilderService,
			@NonNull final BaseUiService baseUiService,
			final Game sc2Game,
			final Game heroesGame) {
		super(sc2Game, heroesGame);
		this.projects = projects;
		this.useCmdLineSettings = useCmdLineSettings;
		this.mpqBuilderService = mpqBuilderService;
		this.baseUiService = baseUiService;
	}
	
	@Override
	public void run() {
		final long startTime = System.currentTimeMillis();
		start();
		try {
			// needs to limit threads to limit RAM usage and allow first thread to arrive as fast as possible at the bottleneck
			final int maxConcurrentThreads = 2;
			try (final ExecutorService e = Executors.newVirtualThreadPerTaskExecutor()) {
				for (final BuildTask task : projects.stream().map(this::createTask).toList()) {
					while (CleaningTask.getRunningTaskCount() > maxConcurrentThreads) {
						Thread.sleep(100);
					}
					e.submit(task);
					Thread.sleep(100);
				}
			}
		} catch (final InterruptedException e) {
			log.error("Interrupted while building and submitting BuildTasks.", e);
			Thread.currentThread().interrupt();
		} catch (final Exception e) {
			log.error("Error while building and submitting BuildTasks.", e);
		} finally {
			end();
		}
		final long executionTime = (System.currentTimeMillis() - startTime);
		log.info("Building all projects took {}ms.", executionTime);
	}
	
	private BuildTask createTask(final Project project) {
		return new BuildTask(project, useCmdLineSettings, mpqBuilderService, baseUiService, null, null);
	}
}
