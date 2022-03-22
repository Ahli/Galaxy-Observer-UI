// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.config;

import com.ahli.galaxy.game.Game;
import com.ahli.galaxy.game.GameDef;
import com.ahli.interfacebuilder.SpringBootApplication;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.base_ui.DiscCacheService;
import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.compile.CompileService;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.integration.JarHelper;
import com.ahli.interfacebuilder.integration.ReplayService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import com.ahli.interfacebuilder.integration.kryo.KryoService;
import com.ahli.interfacebuilder.projects.ProjectEntity;
import com.ahli.interfacebuilder.projects.ProjectJpaRepository;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import com.ahli.interfacebuilder.threads.SpringForkJoinWorkerThreadFactory;
import com.ahli.interfacebuilder.threads.TaskCleaner;
import com.ahli.interfacebuilder.ui.AppController;
import com.ahli.interfacebuilder.ui.PrimaryStageHolder;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import com.ahli.interfacebuilder.ui.progress.TabPaneController;
import com.ahli.mpq.MpqEditorInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Configuration
@EntityScan(basePackageClasses = { ProjectEntity.class, RuleSet.class })
@EnableJpaRepositories(basePackageClasses = ProjectJpaRepository.class)
@Log4j2
public class AppConfiguration {
	
	protected AppConfiguration() {
	}
	
	@Bean
	PrimaryStageHolder primaryStageHolder() {
		return new PrimaryStageHolder();
	}
	
	@Bean
	protected Game sc2Game() {
		log.debug("init bean: sc2BaseGame");
		return new Game(GameDef.buildSc2GameDef());
	}
	
	@Bean
	protected Game heroesGame() {
		log.debug("init bean: heroesBaseGame");
		return new Game(GameDef.buildHeroesGameDef());
	}
	
	@Bean
	protected MpqEditorInterface mpqEditorInterface() {
		log.debug("init bean: mpqEditorInterface");
		return new MpqEditorInterface(mpqCachePath(tempDirectory()), mpqEditorPath(basePath()));
	}
	
	protected Path mpqCachePath(final Path tempDirectory) {
		return Path.of(tempDirectory.toString(), "ObserverInterfaceBuilder", "_ExtractedMpq");
	}
	
	protected Path tempDirectory() {
		return Path.of(System.getProperty("java.io.tmpdir"));
	}
	
	protected Path mpqEditorPath(final Path basePath) {
		return Path.of(
				basePath.getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator + "mpq" +
						File.separator + "MPQEditor.exe");
	}
	
	protected Path basePath() {
		return JarHelper.getJarDir(SpringBootApplication.class);
	}
	
	@Bean
	protected ReplayService replayService(final FileService fileService) {
		log.debug("init bean replayService");
		return new ReplayService(fileService);
	}
	
	@Bean
	protected CompileService compileService(final GameService gameService) {
		log.debug("init bean: compileService");
		return new CompileService(gameService);
	}
	
	@Bean
	protected ProjectService projectService(final ProjectJpaRepository projectJpaRepository) {
		log.debug("init bean: projectService");
		return new ProjectService(projectJpaRepository);
	}
	
	@Bean
	protected CleaningForkJoinTaskCleaner taskCleaner(final Game sc2Game, final Game heroesGame) {
		log.debug("init bean: taskCleaner");
		return new TaskCleaner(sc2Game, heroesGame);
	}
	
	@Bean
	protected ProgressController progressController(final TabPaneController tabPaneController) {
		log.debug("init bean: progressController");
		return new ProgressController(tabPaneController);
	}
	
	@Bean
	protected MpqBuilderService mpqBuilderService(
			final ConfigService configService,
			final CompileService compileService,
			final FileService fileService,
			final ProjectService projectService,
			final BaseUiService baseUiService,
			// Spring uses the parameter name as a qualifier
			final Game sc2Game,
			final Game heroesGame,
			final CleaningForkJoinPool executor,
			final NavigationController navigationController,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
		log.debug("init bean: mpqBuilderService");
		return new MpqBuilderService(
				configService,
				compileService,
				fileService,
				projectService,
				baseUiService,
				sc2Game,
				heroesGame,
				executor,
				navigationController,
				progressController,
				primaryStage);
	}
	
	@Bean
	protected CleaningForkJoinPool cleaningForkJoinPool(final CleaningForkJoinTaskCleaner cleaner) {
		log.debug("init bean: forkJoinPool");
		final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		return new CleaningForkJoinPool(
				maxThreads,
				new SpringForkJoinWorkerThreadFactory(),
				null,
				true,
				maxThreads,
				256,
				1,
				null,
				5_000L,
				TimeUnit.MILLISECONDS,
				cleaner);
	}
	
	@Bean
	protected AppController appController(
			final ForkJoinPool executor,
			final BaseUiService baseUiService,
			final MpqBuilderService mpqBuilderService,
			final GameService gameService,
			final ConfigService configService,
			final ReplayService replayService,
			final ConfigurableApplicationContext appContext,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
		log.debug("init bean: appController");
		return new AppController(
				executor,
				baseUiService,
				mpqBuilderService,
				gameService,
				configService,
				replayService,
				appContext,
				progressController,
				primaryStage);
	}
	
	@Bean
	protected BaseUiService baseUiService(
			final ConfigService configService,
			final FileService fileService,
			final DiscCacheService discCacheService,
			final KryoService kryoService,
			final GameService gameService,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
		log.debug("init bean: baseUiService");
		return new BaseUiService(
				configService,
				gameService,
				fileService,
				discCacheService,
				kryoService,
				progressController,
				primaryStage);
	}
	
	@Bean
	protected GameService gameService(final ConfigService configService) {
		
		log.debug("init bean: gameService");
		return new GameService(configService);
	}
	
	@Bean
	protected FileService fileService() {
		
		
		log.debug("init bean: fileService");
		return new FileService();
	}
	
	@Bean
	protected DiscCacheService discCacheService(final ConfigService configService, final KryoService kryoService) {
		log.debug("init bean: discCacheService");
		return new DiscCacheService(configService, kryoService);
	}
	
	@Bean
	protected KryoService kryoService() {
		
		log.debug("init bean: kryoService");
		return new KryoService();
	}
	
	@Bean
	protected ConfigService configService(
			final SettingsIniInterface settingsIniInterface) {
		log.debug("init bean: configService");
		final Path tmpPath = tempDirectory();
		final Path basePath = basePath();
		return new ConfigService(
				mpqCachePath(tmpPath),
				basePath,
				documentsPath(),
				mpqEditorPath(basePath),
				settingsIniInterface,
				raceId(),
				consoleSkinId(),
				baseUiPath(basePath),
				cascExtractorExeFile(),
				cachePath(),
				miningTempPath(tmpPath));
	}
	
	protected Path documentsPath() {
		return FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
	}
	
	protected String raceId() {
		return "Terr";
	}
	
	protected String consoleSkinId() {
		return "ClassicTerran";
	}
	
	protected Path baseUiPath(final Path basePath) {
		return basePath.getParent().resolve("baseUI");
	}
	
	protected File cascExtractorExeFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CascExtractor.exe");
	}
	
	protected Path cachePath() {
		return Path.of(System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "cache");
	}
	
	protected Path miningTempPath(final Path tempDirectory) {
		return tempDirectory.resolve("ObserverInterfaceBuilder" + File.separator + "_Mining");
	}
	
	@Bean
	protected SettingsIniInterface settingsIniInterface() {
		
		log.debug("init bean: settingsIniInterface");
		return new SettingsIniInterface(iniSettingsPath(basePath()));
	}
	
	protected Path iniSettingsPath(final Path basePath) {
		return basePath.getParent().resolve("settings.ini");
	}
}
