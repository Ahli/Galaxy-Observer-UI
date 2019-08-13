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
import interfacebuilder.ui.progress.StylizedTextAreaAppenderThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
	protected String documentsPath() {
		return new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	}
	
	@Bean
	protected String cachePath() {
		return System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "cache";
	}
	
	@Bean
	protected MpqEditorInterface mpqEditorInterface() {
		return new MpqEditorInterface(mpqCachePath(), mpqEditorPath());
	}
	
	@Bean
	protected String mpqCachePath() {
		return tempDirectory() + "ObserverInterfaceBuilder" + File.separator + "_ExtractedMpq";
	}
	
	@Bean
	protected String mpqEditorPath() {
		return basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator + "mpq" +
				File.separator + "MPQEditor.exe";
	}
	
	@Bean
	protected String tempDirectory() {
		return System.getProperty("java.io.tmpdir");
	}
	
	@Bean
	protected File basePath() {
		return JarHelper.getJarDir(InterfaceBuilderApp.class);
	}
	
	@Bean
	protected File cascExtractorConfigFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CASCConsole.exe.config");
	}
	
	@Bean
	protected File cascExtractorConsoleExeFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CASCConsole.exe");
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
	protected StylizedTextAreaAppenderThreadPoolExecutor threadPoolExecutor() {
		final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		final StylizedTextAreaAppenderThreadPoolExecutor executor =
				new StylizedTextAreaAppenderThreadPoolExecutor(maxThreads, maxThreads, 5000L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), null);
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}
	
	@Bean
	protected SettingsIniInterface settingsIniInterface() {
		return new SettingsIniInterface(iniSettingsPath());
	}
	
	@Bean
	protected String iniSettingsPath() {
		return basePath().getParent() + File.separator + "settings.ini";
	}
	
	@Bean
	protected String baseUiPath() {
		return basePath().getParent() + File.separator + "baseUI";
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
