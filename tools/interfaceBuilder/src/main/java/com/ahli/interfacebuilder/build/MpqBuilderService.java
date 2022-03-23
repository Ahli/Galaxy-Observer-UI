// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.build;

import com.ahli.galaxy.ModD;
import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndex;
import com.ahli.galaxy.game.Game;
import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.compile.CompileService;
import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.ui.AppController;
import com.ahli.interfacebuilder.ui.PrimaryStageHolder;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMethod;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Log4j2
public class MpqBuilderService {
	
	private final ConfigService configService;
	private final CompileService compileService;
	private final FileService fileService;
	private final ProjectService projectService;
	private final BaseUiService baseUiService;
	private final Game sc2BaseGame;
	private final Game heroesBaseGame;
	private final CleaningForkJoinPool executor;
	private final NavigationController navigationController;
	private final ProgressController progressController;
	private final PrimaryStageHolder primaryStage;
	
	public MpqBuilderService(
			@NonNull final ConfigService configService,
			@NonNull final CompileService compileService,
			@NonNull final FileService fileService,
			@NonNull final ProjectService projectService,
			@NonNull final BaseUiService baseUiService,
			@NonNull final Game sc2BaseGame,
			@NonNull final Game heroesBaseGame,
			@NonNull final CleaningForkJoinPool executor,
			@NonNull final NavigationController navigationController,
			@NonNull final ProgressController progressController,
			@NonNull final PrimaryStageHolder primaryStage) {
		this.configService = configService;
		this.compileService = compileService;
		this.fileService = fileService;
		this.projectService = projectService;
		this.baseUiService = baseUiService;
		this.sc2BaseGame = sc2BaseGame;
		this.heroesBaseGame = heroesBaseGame;
		this.executor = executor;
		this.navigationController = navigationController;
		this.progressController = progressController;
		this.primaryStage = primaryStage;
	}
	
	/**
	 * Schedules a task to find and build a project based on the specified path.
	 *
	 * @param path
	 */
	public void build(@NonNull final Path path) throws IOException {
		final Project project;
		
		final List<Project> projectsOfPath = projectService.getProjectsOfPath(path);
		if (projectsOfPath.isEmpty()) {
			final GameType gameType;
			if (pathContainsCompileableForGame(path, heroesBaseGame)) {
				gameType = GameType.HEROES;
			} else if (pathContainsCompileableForGame(path, sc2BaseGame)) {
				gameType = GameType.SC2;
			} else {
				throw new IllegalArgumentException("Specified path '" + path + "' did not contain any project.");
			}
			project = new Project(path.getFileName().toString(), path, gameType);
		} else {
			project = projectsOfPath.get(0);
		}
		
		build(project, true);
	}
	
	/**
	 * Returns whether the specified path contains a compilable file for the specified game.
	 *
	 * @param path
	 * @param game
	 * @return
	 */
	public boolean pathContainsCompileableForGame(@NonNull final Path path, @NonNull final Game game)
			throws IOException {
		final String extension = game.getGameDef().layoutFileEnding().toLowerCase(Locale.ROOT);
		try (final Stream<Path> walk = Files.walk(path)) {
			return walk.anyMatch(curPath -> curPath.getFileName()
					.toString()
					.toLowerCase(Locale.ROOT)
					.endsWith(extension));
		}
	}
	
	/**
	 * Schedules a task to build the mpq archive file for a project.
	 *
	 * @param project
	 */
	public void build(@NonNull final Project project, final boolean useCmdLineSettings) {
		final BuildTask task = new BuildTask(executor, project, useCmdLineSettings, this, baseUiService);
		executor.execute(task);
	}
	
	/**
	 * Returns the GameData definition for the specified game.
	 *
	 * @param gameType
	 * @return
	 */
	@NonNull
	public Game getGameData(@NonNull final GameType gameType) {
		return switch (gameType) {
			case SC2 -> sc2BaseGame;
			case HEROES -> heroesBaseGame;
		};
	}
	
	/**
	 * Builds a specific Interface in a Build Thread.
	 *
	 * @param game
	 * @param useCmdLineSettings
	 * @param project
	 */
	public void buildSpecificUI(
			@NonNull final Game game, final boolean useCmdLineSettings, @NonNull final Project project) {
		if (executor.isShutdown()) {
			log.error("ERROR: Executor shut down. Skipping building a UI...");
			return;
		}
		
		final Path interfaceDirectory = project.getProjectPath();
		
		if (!Files.exists(interfaceDirectory) || !Files.isDirectory(interfaceDirectory)) {
			log.error("ERROR: Can't build UI from file '{}', expected an existing directory.", interfaceDirectory);
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
			if (primaryStage.hasPrimaryStage()) {
				progressController.addThreadlogTab(Thread.currentThread().getName(),
						interfaceDirectory.getFileName().toString(),
						true);
			}
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
				RuleSet ruleSet = projectService.fetchBestCompressionRuleSet(project);
				if (ruleSet == null) {
					ruleSet = new RuleSet(createInitialRuleset(interfaceDirectory));
					project.setBestCompressionRuleSet(ruleSet);
					projectService.saveProject(project);
				}
				// TODO else verify ruleset
				threadsMpqInterface.setCustomCompressionRules(ruleSet.getCompressionRules());
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
			log.error("ERROR: Exception while building UIs.", e);
		} catch (final Exception e) {
			log.fatal("FATAL ERROR: ", e);
		}
	}
	
	/**
	 * Creates an initial ruleset.
	 *
	 * @param cacheModDirectory
	 * @return
	 * @throws IOException
	 */
	private MpqEditorCompressionRule[] createInitialRuleset(final Path cacheModDirectory) throws IOException {
		final List<MpqEditorCompressionRule> initRules = new ArrayList<>(20);
		initRules.add(new MpqEditorCompressionRuleSize(0, 0).setSingleUnit(true));
		try (final Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile).forEach(p -> {
				final var compressionRule = getDefaultRule(p, cacheModDirectory);
				initRules.add(compressionRule);
			});
		}
		return initRules.toArray(new MpqEditorCompressionRule[0]);
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
			@NonNull final Path sourceFile,
			@NonNull final Game game,
			@NonNull final MpqEditorInterface mpqi,
			final boolean compressXml,
			final int compressMpq,
			final boolean buildUnprotectedToo,
			final boolean repairLayoutOrder,
			final boolean verifyLayout,
			final boolean verifyXml,
			@NonNull final Project project) throws IOException, InterruptedException {
		primaryStage.printInfoLogMessageToGeneral(sourceFile.getFileName() + " started construction.");
		
		final GameDef gameDef = game.getGameDef();
		
		final Path targetFile =
				Path.of(configService.getDocumentsPath() + File.separator + gameDef.documentsGameDirectoryName() +
						File.separator + gameDef.documentsInterfaceSubdirectoryName());
		
		// init mod data
		final ModD mod = new ModD(game);
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
			log.error(msg);
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
					log.warn("Attempt to copy directory failed.", e);
				} else if (copyAttempts >= 100) {
					final String msg = "Unable to copy directory after 100 copy attempts: " + e.getMessage();
					log.error(msg, e);
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} catch (final IOException e) {
				final String msg = "Unable to copy directory";
				log.error(msg, e);
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Failed to copy files to: " + cache;
			log.error(msg);
			throw new IOException(msg);
		}
		
		final Path componentListFile = mpqi.getComponentListFile();
		if (componentListFile == null) {
			final String msg = "ERROR: did not find the ComponentList file.";
			log.error(msg);
			throw new IOException(msg);
		}
		mod.setComponentListFile(componentListFile);
		
		final DescIndex descIndex = new DescIndex(mpqi);
		mod.setDescIndex(descIndex);
		
		try {
			descIndex.setDescIndexPathAndClear(ComponentsListReaderDom.getDescIndexPath(componentListFile, gameDef));
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path.";
			log.error(msg, e);
			throw new IOException(msg, e);
		}
		
		final Path descIndexFile = mpqi.getFilePathFromMpq(descIndex.getDescIndexIntPath());
		try {
			descIndex.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile).combined());
		} catch (final SAXException | ParserConfigurationException | IOException | MpqException e) {
			log.error("unable to read Layout paths", e);
		}
		
		log.info("Compiling... {}", sourceFile.getFileName());
		
		// perform checks/improvements on code
		compileService.compile(mod,
				configService.getRaceId(),
				repairLayoutOrder,
				verifyLayout,
				verifyXml,
				configService.getConsoleSkinId());
		
		log.info("Building... {}", sourceFile.getFileName());
		
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
			log.info("Finished building... {}. Size: {}kb", sourceFileName, size / 1024);
			projectService.saveProject(project);
			primaryStage.printInfoLogMessageToGeneral(sourceFileName + " finished construction.");
		} catch (final IOException | MpqException e) {
			log.error("ERROR: unable to construct final Interface file.", e);
			AppController.printErrorLogMessageToGeneral(sourceFileName + " could not be created.");
		}
	}
	
	/**
	 * @param path
	 * @param sourceDir
	 * @return
	 */
	private static MpqEditorCompressionRuleMask getDefaultRule(final Path path, final Path sourceDir) {
		final var rule = new MpqEditorCompressionRuleMask(getFileMask(path, sourceDir));
		rule.setSingleUnit(true).setCompress(true);
		rule.setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
		return rule;
	}
	
	/**
	 * @param compressMpqSetting
	 * @return
	 */
	@NonNull
	private static MpqEditorCompression getCompressionModeOfSetting(final int compressMpqSetting) {
		return switch (compressMpqSetting) {
			case 0 -> MpqEditorCompression.NONE;
			case 1 -> MpqEditorCompression.BLIZZARD_SC2_HEROES;
			case 2 -> MpqEditorCompression.CUSTOM;
			case 3 -> MpqEditorCompression.SYSTEM_DEFAULT;
			default -> throw new IllegalArgumentException("Unsupported mpq compression mode.");
		};
	}
	
	/**
	 * Returns the MpqCompressionRuleSet's mask String for the Path of an extracted interface's file.
	 *
	 * @param p
	 * @return
	 */
	private static String getFileMask(final Path p, final Path sourceDir) {
		final String name = p.normalize().toString();
		return name.substring(sourceDir.toString().length() + 1);
	}
	
	/**
	 * Builds the specified projects.
	 *
	 * @param projects
	 * @param useCmdLineSettings
	 */
	public void build(@NonNull final Iterable<Project> projects, final boolean useCmdLineSettings) {
		boolean building = false;
		for (final Project project : projects) {
			building = true;
			build(project, useCmdLineSettings);
		}
		if (building) {
			// switch to progress
			navigationController.clickProgress();
		}
	}
	
}
