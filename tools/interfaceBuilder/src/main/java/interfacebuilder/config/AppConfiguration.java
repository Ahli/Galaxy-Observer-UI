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
import interfacebuilder.projects.ProjectService;
import interfacebuilder.threads.CleaningForkJoinPool;
import interfacebuilder.threads.SpringForkJoinWorkerThreadFactory;
import interfacebuilder.ui.AppController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	protected Path documentsPath() {
		return FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
	}
	
	@Bean
	protected Path cachePath() {
		return Path.of(System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "cache");
	}
	
	@Bean
	protected MpqEditorInterface mpqEditorInterface() {
		return new MpqEditorInterface(mpqCachePath(), mpqEditorPath());
	}
	
	@Bean
	protected Path mpqCachePath() {
		return Path.of(tempDirectory().toString(), "ObserverInterfaceBuilder", "_ExtractedMpq");
	}
	
	@Bean
	protected Path mpqEditorPath() {
		return Path.of(basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
				"mpq" +
				File.separator + "MPQEditor.exe");
	}
	
	@Bean
	protected Path tempDirectory() {
		return Path.of(System.getProperty("java.io.tmpdir"));
	}
	
	@Bean
	protected Path basePath() {
		return JarHelper.getJarDir(InterfaceBuilderApp.class);
	}
	
	@Bean
	protected Path miningTempPath() {
		return tempDirectory().resolve("ObserverInterfaceBuilder" + File.separator + "_Mining");
	}
	
	@Bean
	protected File cascExtractorExeFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CascExtractor.exe");
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
	protected MpqBuilderService mpqBuilderService() {
		return new MpqBuilderService();
	}
	
	@Bean
	protected ConfigService configService() {
		return new ConfigService();
	}
	
	@Bean
	protected CompileService compileService() {
		return new CompileService();
	}
	
	@Bean
	protected ProjectService projectService() {
		return new ProjectService();
	}
	
	@Bean
	protected FileService fileService() {
		return new FileService();
	}
	
	@Bean
	protected ReplayFinder replayService() {
		return new ReplayFinder();
	}
	
	@Bean
	protected ForkJoinPool forkJoinPool() {
		final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		return new CleaningForkJoinPool(maxThreads, new SpringForkJoinWorkerThreadFactory(), null, true, maxThreads,
				256, 1, null, 5_000L, TimeUnit.MILLISECONDS, appController());
	}
	
	@Bean
	protected AppController appController() {
		return new AppController();
	}
	
	@Bean
	protected SettingsIniInterface settingsIniInterface() {
		return new SettingsIniInterface(iniSettingsPath());
	}
	
	@Bean
	protected Path iniSettingsPath() {
		return basePath().getParent().resolve("settings.ini");
	}
	
	@Bean
	protected Path baseUiPath() {
		return basePath().getParent().resolve("baseUI");
	}
	
	@Bean
	protected GameService gameService() {
		return new GameService();
	}
	
	@Bean
	protected BaseUiService baseUiService() {
		return new BaseUiService();
	}
	
	@Bean
	protected DiscCacheService discCacheService() {
		return new DiscCacheService();
	}
	
	@Bean
	protected KryoService kryoService() {
		return new KryoService();
	}
}
