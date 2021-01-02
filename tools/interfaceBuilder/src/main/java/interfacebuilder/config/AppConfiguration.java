// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.mpq.MpqEditorInterface;
import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.base_ui.DiscCacheService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compile.CompileService;
import interfacebuilder.compress.GameService;
import interfacebuilder.integration.FileService;
import interfacebuilder.integration.JarHelper;
import interfacebuilder.integration.ReplayFinder;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.integration.kryo.KryoService;
import interfacebuilder.projects.ProjectJpaRepository;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.threads.CleaningForkJoinPool;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import interfacebuilder.threads.SpringForkJoinWorkerThreadFactory;
import interfacebuilder.ui.AppController;
import interfacebuilder.ui.navigation.NavigationController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


@Configuration
public class AppConfiguration {
	
	@Bean
	protected GameData sc2BaseGameData() {
		return new GameData(sc2GameDef());
	}
	
	@Bean
	protected GameDef sc2GameDef() {
		return new SC2GameDef();
	}
	
	@Bean
	protected GameData heroesBaseGameData() {
		return new GameData(heroesGameDef());
	}
	
	@Bean
	protected GameDef heroesGameDef() {
		return new HeroesGameDef();
	}
	
	@Bean
	protected MpqEditorInterface mpqEditorInterface() {
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
		return JarHelper.getJarDir(InterfaceBuilderApp.class);
	}
	
	@Bean
	protected CompileService compileService() {
		return new CompileService();
	}
	
	@Bean
	protected ProjectService projectService(
			final ProjectJpaRepository projectJpaRepository,
			@Lazy final MpqBuilderService mpqBuilderService,
			final NavigationController navigationController) {
		return new ProjectService(projectJpaRepository, mpqBuilderService, navigationController);
	}
	
	@Bean
	protected MpqBuilderService mpqBuilderService(
			final ConfigService configService,
			final CompileService compileService,
			final FileService fileService,
			final ProjectService projectService,
			final BaseUiService baseUiService,
			final GameData sc2BaseGameData,
			final GameData heroesBaseGameData,
			final ForkJoinPool forkJoinPool,
			final AppController appController) {
		return new MpqBuilderService(
				configService,
				compileService,
				fileService,
				projectService,
				baseUiService,
				sc2BaseGameData,
				heroesBaseGameData,
				forkJoinPool,
				appController);
	}
	
	@Bean
	protected ReplayFinder replayService() {
		return new ReplayFinder();
	}
	
	@Bean
	protected ForkJoinPool forkJoinPool(final CleaningForkJoinTaskCleaner cleaner) {
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
	protected AppController appController() {
		return new AppController();
	}
	
	@Bean
	protected BaseUiService baseUiService(
			final ConfigService configService,
			final FileService fileService,
			final DiscCacheService discCacheService,
			final KryoService kryoService,
			final AppController appController,
			final GameService gameService) {
		return new BaseUiService(configService, gameService, fileService, discCacheService, kryoService, appController);
	}
	
	@Bean
	protected GameService gameService(final ConfigService configService) {
		return new GameService(configService);
	}
	
	@Bean
	protected FileService fileService() {
		return new FileService();
	}
	
	@Bean
	protected DiscCacheService discCacheService(final ConfigService configService, final KryoService kryoService) {
		return new DiscCacheService(configService, kryoService);
	}
	
	@Bean
	protected KryoService kryoService() {
		return new KryoService();
	}
	
	@Bean
	protected ConfigService configService(
			final String raceId,
			final String consoleSkinId,
			final File cascExtractorExeFile,
			final SettingsIniInterface settingsIniInterface) {
		final Path tmpPath = tempDirectory();
		final Path basePath = basePath();
		return new ConfigService(
				mpqCachePath(tmpPath),
				basePath,
				documentsPath(),
				mpqEditorPath(basePath),
				settingsIniInterface,
				raceId,
				consoleSkinId,
				baseUiPath(basePath),
				cascExtractorExeFile,
				cachePath(),
				miningTempPath(tmpPath));
	}
	
	protected Path documentsPath() {
		return FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
	}
	
	protected Path baseUiPath(final Path basePath) {
		return basePath.getParent().resolve("baseUI");
	}
	
	protected Path cachePath() {
		return Path.of(System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "cache");
	}
	
	protected Path miningTempPath(final Path tempDirectory) {
		return tempDirectory.resolve("ObserverInterfaceBuilder" + File.separator + "_Mining");
	}
	
	@Bean
	protected SettingsIniInterface settingsIniInterface() {
		return new SettingsIniInterface(iniSettingsPath(basePath()));
	}
	
	protected Path iniSettingsPath(final Path basePath) {
		return basePath.getParent().resolve("settings.ini");
	}
	
	@Bean
	protected String raceId() {
		return "Terr";
	}
	
	@Bean
	protected String consoleSkinId() {
		return "ClassicTerran";
	}
	
	@Bean
	protected File cascExtractorExeFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CascExtractor.exe");
	}
}
