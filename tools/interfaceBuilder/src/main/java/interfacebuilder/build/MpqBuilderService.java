// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.build;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.compile.CompileService;
import interfacebuilder.compress.RuleSet;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.FileService;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.AppController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class MpqBuilderService {
	private static final Logger logger = LogManager.getLogger(MpqBuilderService.class);
	
	private final ConfigService configService;
	private final CompileService compileService;
	private final FileService fileService;
	private final ProjectService projectService;
	private final BaseUiService baseUiService;
	private final GameData sc2BaseGameData;
	private final GameData heroesBaseGameData;
	private final ForkJoinPool executor;
	private final AppController appController;
	
	public MpqBuilderService(
			final ConfigService configService,
			final CompileService compileService,
			final FileService fileService,
			final ProjectService projectService,
			final BaseUiService baseUiService,
			final GameData sc2BaseGameData,
			final GameData heroesBaseGameData,
			final ForkJoinPool executor,
			final AppController appController) {
		this.configService = configService;
		this.compileService = compileService;
		this.fileService = fileService;
		this.projectService = projectService;
		this.baseUiService = baseUiService;
		this.sc2BaseGameData = sc2BaseGameData;
		this.heroesBaseGameData = heroesBaseGameData;
		this.executor = executor;
		this.appController = appController;
	}
	
	/**
	 * Schedules a task to find and build a project based on the specified path.
	 *
	 * @param path
	 */
	public void build(final Path path) throws IOException {
		final Project project;
		
		final List<Project> projectsOfPath = projectService.getProjectsOfPath(path.toString());
		if (projectsOfPath.isEmpty()) {
			final Game game;
			if (projectService.pathContainsCompileableForGame(path, heroesBaseGameData)) {
				game = Game.HEROES;
			} else if (projectService.pathContainsCompileableForGame(path, sc2BaseGameData)) {
				game = Game.SC2;
			} else {
				throw new IllegalArgumentException("Specified path '" + path + "' did not contain any project.");
			}
			project = new Project(path.getFileName().toString(), path, game);
		} else {
			project = projectsOfPath.get(0);
		}
		
		build(project, true);
	}
	
	/**
	 * Schedules a task to build the mpq archive file for a project.
	 *
	 * @param project
	 */
	public void build(final Project project, final boolean useCmdLineSettings) {
		final BuildTask task = new BuildTask(appController, project, useCmdLineSettings, this, baseUiService);
		executor.execute(task);
	}
	
	/**
	 * Returns the GameData definition for the specified game.
	 *
	 * @param game
	 * @return
	 */
	public GameData getGameData(final Game game) {
		return switch (game) {
			case SC2 -> sc2BaseGameData;
			case HEROES -> heroesBaseGameData;
		};
	}
	
	/**
	 * Builds a specific Interface in a Build Thread.
	 *
	 * @param game
	 * @param useCmdLineSettings
	 * @param project
	 */
	void buildSpecificUI(
			final GameData game, final boolean useCmdLineSettings, final Project project) {
		if (executor.isShutdown()) {
			logger.error("ERROR: Executor shut down. Skipping building a UI...");
			return;
		}
		
		final Path interfaceDirectory = project.getProjectPath();
		
		if (!Files.exists(interfaceDirectory) || !Files.isDirectory(interfaceDirectory)) {
			logger.error("ERROR: Can't build UI from file '{}', expected an existing directory.", interfaceDirectory);
			return;
		}
		final SettingsIniInterface settings = configService.getIniSettings();
		final boolean verifyLayout =
				useCmdLineSettings ? settings.isCmdLineVerifyLayout() : settings.isGuiVerifyLayout();
		
		if (game.getUiCatalog() == null && verifyLayout) {
			// parse default UI
			throw new IllegalStateException(String.format("Base UI of game '%s' has not been parsed.",
					game.getGameDef().name()));
		}
		
		// create tasks for the worker pool
		try {
			appController.addThreadLoggerTab(Thread.currentThread().getName(),
					interfaceDirectory.getFileName().toString(),
					true);
			// create unique cache path
			final MpqEditorInterface threadsMpqInterface = new MpqEditorInterface(configService.getMpqCachePath()
					.resolve(Long.toString(Thread.currentThread().getId())), configService.getMpqEditorPath());
			
			// work
			final boolean compressXml;
			final int compressMpqSetting;
			final boolean buildUnprotectedToo;
			final boolean repairLayoutOrder;
			final boolean verifyXml;
			if (useCmdLineSettings) {
				compressXml = settings.isCmdLineCompressXml();
				compressMpqSetting = settings.getCmdLineCompressMpq();
				buildUnprotectedToo = settings.isCmdLineBuildUnprotectedToo();
				repairLayoutOrder = settings.isCmdLineRepairLayoutOrder();
				verifyXml = settings.isCmdLineVerifyXml();
			} else {
				compressXml = settings.isGuiCompressXml();
				compressMpqSetting = settings.getGuiCompressMpq();
				buildUnprotectedToo = settings.isGuiBuildUnprotectedToo();
				repairLayoutOrder = settings.isGuiRepairLayoutOrder();
				verifyXml = settings.isGuiVerifyXml();
			}
			
			// load best compression ruleset
			if (compressXml && compressMpqSetting == 2) {
				final RuleSet ruleSet = projectService.fetchBestCompressionRuleSet(project);
				if (ruleSet != null) {
					// TODO verify/expand ruleset
					threadsMpqInterface.setCustomCompressionRules(ruleSet.getCompressionRules());
				}
			}
			threadsMpqInterface.clearCacheExtractedMpq();
			buildFile(interfaceDirectory,
					game,
					threadsMpqInterface,
					compressXml,
					compressMpqSetting,
					buildUnprotectedToo,
					repairLayoutOrder,
					verifyLayout,
					verifyXml,
					project);
			threadsMpqInterface.clearCacheExtractedMpq();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException e) {
			logger.error("ERROR: Exception while building UIs.", e);
		} catch (final Exception e) {
			logger.fatal("FATAL ERROR: ", e);
		}
	}
	
	/**
	 * Builds MPQ Archive File. Run this in its own thread! Conditions: - Specified MpqInterface requires a unique cache
	 * path for multithreading.
	 *
	 * @param sourceFile
	 * 		folder location
	 * @param game
	 * 		the game data with game definition
	 * @param mpqi
	 * 		MpqInterface with unique cache path
	 * @param compressXml
	 * @param compressMpq
	 * @param buildUnprotectedToo
	 * @param repairLayoutOrder
	 * @param verifyLayout
	 * @param verifyXml
	 * @param project
	 * @throws IOException
	 * 		when something goes wrong
	 * @throws InterruptedException
	 */
	private void buildFile(
			final Path sourceFile,
			final GameData game,
			final MpqEditorInterface mpqi,
			final boolean compressXml,
			final int compressMpq,
			final boolean buildUnprotectedToo,
			final boolean repairLayoutOrder,
			final boolean verifyLayout,
			final boolean verifyXml,
			final Project project) throws IOException, InterruptedException {
		appController.printInfoLogMessageToGeneral(sourceFile.getFileName() + " started construction.");
		
		final GameDef gameDef = game.getGameDef();
		
		final Path targetFile =
				Path.of(configService.getDocumentsPath() + File.separator + gameDef.documentsGameDirectoryName() +
						File.separator + gameDef.documentsInterfaceSubdirectoryName());
		
		// init mod data
		final ModData mod = new ModData(game);
		mod.setSourceDirectory(sourceFile);
		mod.setTargetFile(targetFile);
		
		// get and create cache
		final Path cache = mpqi.getCache();
		if (!Files.exists(cache)) {
			Files.createDirectories(cache);
		}
		int cacheClearAttempts;
		for (cacheClearAttempts = 0; cacheClearAttempts <= 100; ++cacheClearAttempts) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} else {
				// success
				break;
			}
		}
		if (cacheClearAttempts > 100) {
			final String msg = "ERROR: Cache could not be cleared";
			logger.error(msg);
			return;
		}
		mod.setMpqCacheDirectory(cache);
		// put files into cache
		int copyAttempts;
		for (copyAttempts = 0; copyAttempts <= 100; ++copyAttempts) {
			try {
				fileService.copyFileOrDirectory(sourceFile, cache);
				break;
			} catch (final FileSystemException e) {
				if (copyAttempts == 0) {
					logger.warn("Attempt to copy directory failed.", e);
				} else if (copyAttempts >= 100) {
					final String msg = "Unable to copy directory after 100 copy attempts: " + e.getMessage();
					logger.error(msg, e);
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} catch (final IOException e) {
				final String msg = "Unable to copy directory";
				logger.error(msg, e);
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Failed to copy files to: " + cache;
			logger.error(msg);
			throw new IOException(msg);
		}
		
		final Path componentListFile = mpqi.getComponentListFile();
		if (componentListFile == null) {
			final String msg = "ERROR: did not find the ComponentList file.";
			logger.error(msg);
			throw new IOException(msg);
		}
		mod.setComponentListFile(componentListFile);
		
		final DescIndexData descIndexData = new DescIndexData(mpqi);
		mod.setDescIndexData(descIndexData);
		
		try {
			descIndexData.setDescIndexPathAndClear(ComponentsListReaderDom.getDescIndexPath(componentListFile,
					gameDef));
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path.";
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
		
		final File descIndexFile = mpqi.getFilePathFromMpq(descIndexData.getDescIndexIntPath()).toFile();
		try {
			descIndexData.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile).combined());
		} catch (final SAXException | ParserConfigurationException | IOException | MpqException e) {
			logger.error("unable to read Layout paths", e);
		}
		
		logger.info("Compiling... {}", sourceFile.getFileName());
		
		// perform checks/improvements on code
		compileService.compile(mod,
				configService.getRaceId(),
				repairLayoutOrder,
				verifyLayout,
				verifyXml,
				configService.getConsoleSkinId());
		
		logger.info("Building... {}", sourceFile.getFileName());
		
		final String sourceFileName = sourceFile.getFileName().toString();
		try {
			mpqi.buildMpq(targetFile,
					sourceFileName,
					compressXml,
					getCompressionModeOfSetting(compressMpq),
					buildUnprotectedToo);
			
			project.setLastBuildDateTime(LocalDateTime.now());
			final long size = Files.size(targetFile.resolve(sourceFileName));
			project.setLastBuildSize(size);
			logger.info("Finished building... {}. Size: {}kb", sourceFileName, size / 1024);
			projectService.saveProject(project);
			appController.printInfoLogMessageToGeneral(sourceFileName + " finished construction.");
		} catch (final IOException | MpqException e) {
			logger.error("ERROR: unable to construct final Interface file.", e);
			AppController.printErrorLogMessageToGeneral(sourceFileName + " could not be created.");
		}
	}
	
	/**
	 * @param compressMpqSetting
	 * @return
	 */
	private static MpqEditorCompression getCompressionModeOfSetting(final int compressMpqSetting) {
		return switch (compressMpqSetting) {
			case 0 -> MpqEditorCompression.NONE;
			case 1 -> MpqEditorCompression.BLIZZARD_SC2_HEROES;
			case 2 -> MpqEditorCompression.CUSTOM;
			case 3 -> MpqEditorCompression.SYSTEM_DEFAULT;
			default -> throw new IllegalArgumentException("Unsupported mpq compression mode.");
		};
	}
}
