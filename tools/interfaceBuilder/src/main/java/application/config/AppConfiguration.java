package application.config;

import application.InterfaceBuilderApp;
import application.baseUi.BaseUiService;
import application.baseUi.DiscCacheService;
import application.build.MpqBuilderService;
import application.compile.CompileService;
import application.compress.GameService;
import application.integration.FileService;
import application.integration.JarHelper;
import application.integration.ReplayFinder;
import application.integration.SettingsIniInterface;
import application.projects.ProjectService;
import application.ui.progress.StylizedTextAreaAppenderThreadPoolExecutor;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.mpq.MpqEditorInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Configuration
public class AppConfiguration {
	
	@Bean
	public GameData sc2BaseGameData() {
		return new GameData(sc2GameDef());
	}
	
	@Bean
	public GameDef sc2GameDef() {
		return new SC2GameDef();
	}
	
	@Bean
	public GameData heroesBaseGameData() {
		return new GameData(heroesGameDef());
	}
	
	@Bean
	public GameDef heroesGameDef() {
		return new HeroesGameDef();
	}
	
	@Bean
	public String documentsPath() {
		return new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
	}
	
	@Bean
	public String cachePath() {
		return System.getProperty("user.home" ) + File.separator + ".GalaxyObsUI" + File.separator + "cache";
	}
	
	@Bean
	public MpqEditorInterface mpqEditorInterface() {
		return new MpqEditorInterface(mpqCachePath(), mpqEditorPath());
	}
	
	@Bean
	public String mpqCachePath() {
		return tempDirectory() + "ObserverInterfaceBuilder" + File.separator + "_ExtractedMpq";
	}
	
	@Bean
	public String mpqEditorPath() {
		return basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator + "mpq" +
				File.separator + "MPQEditor.exe";
	}
	
	@Bean
	public String tempDirectory() {
		return System.getProperty("java.io.tmpdir" );
	}
	
	@Bean
	public File basePath() {
		return JarHelper.getJarDir(InterfaceBuilderApp.class);
	}
	
	@Bean
	public File cascExtractorConfigFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CASCConsole.exe.config" );
	}
	
	@Bean
	public File cascExtractorConsoleExeFile() {
		return new File(
				basePath().getParent() + File.separator + "tools" + File.separator + "plugins" + File.separator +
						"casc" + File.separator + "CASCConsole.exe" );
	}
	
	@Bean
	public String raceId() {
		return "Terr";
	}
	
	@Bean
	public MpqBuilderService mpqBuilderService() {
		return new MpqBuilderService();
	}
	
	@Bean
	public ConfigService configService() {
		return new ConfigService();
	}
	
	@Bean
	public CompileService compileService() {
		return new CompileService();
	}
	
	@Bean
	public ProjectService projectService() {
		return new ProjectService();
	}
	
	@Bean
	public FileService fileService() {
		return new FileService();
	}
	
	@Bean
	public ReplayFinder replayService() {
		return new ReplayFinder();
	}
	
	@Bean
	public ThreadPoolExecutor threadPoolExecutor() {
		final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		final ThreadPoolExecutor executor =
				new StylizedTextAreaAppenderThreadPoolExecutor(maxThreads, maxThreads, 5000L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}
	
	@Bean
	public SettingsIniInterface settingsIniInterface() {
		return new SettingsIniInterface(iniSettingsPath());
	}
	
	@Bean
	public String iniSettingsPath() {
		return basePath().getParent() + File.separator + "settings.ini";
	}
	
	@Bean
	public String baseUiPath() {
		return basePath().getParent() + File.separator + "baseUI";
	}
	
	@Bean
	public GameService gameService() {
		return new GameService();
	}
	
	@Bean
	public BaseUiService baseUiService() {
		return new BaseUiService();
	}
	
	@Bean
	public DiscCacheService discCacheService() {
		return new DiscCacheService();
	}
}
