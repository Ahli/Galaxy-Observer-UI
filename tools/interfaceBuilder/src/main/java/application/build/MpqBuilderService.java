package application.build;

import application.InterfaceBuilderApp;
import application.baseUi.BaseUiService;
import application.compile.CompileService;
import application.compress.RuleSet;
import application.config.ConfigService;
import application.integration.FileService;
import application.integration.SettingsIniInterface;
import application.projects.Project;
import application.projects.ProjectService;
import application.projects.enums.Game;
import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReader;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.DescIndexReader;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.Date;
import java.util.List;

public class MpqBuilderService {
	private static final Logger logger = LogManager.getLogger();
	
	private final InterfaceBuilderApp app = InterfaceBuilderApp.getInstance();
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private CompileService compileService;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	@Qualifier ("sc2BaseGameData")
	private GameData sc2BaseGameData;
	
	@Autowired
	@Qualifier ("heroesBaseGameData")
	private GameData heroesBaseGameData;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private BaseUiService baseUiService;
	
	/**
	 * Finds and builds a project based on the specified path.
	 *
	 * @param path
	 */
	public void build(final String path) {
		final File f = new File(path);
		final Project project;
		
		final List<Project> projectsOfPath = projectService.getProjectsOfPath(path);
		if (projectsOfPath.isEmpty()) {
			final Game game;
			if (projectService.pathContainsCompileableForGame(path, heroesBaseGameData)) {
				game = Game.HEROES;
			} else if (projectService.pathContainsCompileableForGame(path, sc2BaseGameData)) {
				game = Game.SC2;
			} else {
				throw new IllegalArgumentException("Specified path '" + path + "' did not contain any project.");
			}
			project = new Project(f.getName(), f.getAbsolutePath(), game);
		} else {
			project = projectsOfPath.get(0);
		}
		
		build(project, true);
	}
	
	/**
	 * Builds the mpq archive file for a project.
	 *
	 * @param project
	 */
	public void build(final Project project, final boolean useCmdLineSettings) {
		// 1. parse base UI (is skipped if not necessary)
		// 2. then build
		final GameData gameData = getGameData(project.getGame());
		final Runnable followupTask = () -> {
			final File interfaceDirectory = new File(project.getProjectPath());
			buildSpecificUI(interfaceDirectory, gameData, useCmdLineSettings, project);
		};
		parseBaseUiIfNecessary(gameData, useCmdLineSettings, followupTask);
	}
	
	/**
	 * Returns the GameData definition for the specified game.
	 *
	 * @param game
	 * @return
	 */
	private GameData getGameData(final Game game) {
		final GameData baseGame;
		switch (game) {
			case SC2:
				baseGame = sc2BaseGameData;
				break;
			case HEROES:
				baseGame = heroesBaseGameData;
				break;
			default:
				throw new IllegalArgumentException("Unhandled game enum " + game.toString() + ".");
		}
		return baseGame;
	}
	
	/**
	 * Builds a specific Interface in a Build Thread.
	 *
	 * @param interfaceDirectory
	 * 		folder of the interface file to be built
	 * @param game
	 * @param useCmdLineSettings
	 */
	private void buildSpecificUI(final File interfaceDirectory, final GameData game, final boolean useCmdLineSettings,
			final Project project) {
		if (app.getExecutor().isShutdown()) {
			logger.error("ERROR: Executor shut down. Skipping building a UI..."); //$NON-NLS-1$
			return;
		}
		if (!interfaceDirectory.exists() || !interfaceDirectory.isDirectory()) {
			logger.error("ERROR: Can't build UI from file '" + interfaceDirectory +
					"', expected an existing directory."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		final boolean verifyLayout;
		final SettingsIniInterface settings = configService.getIniSettings();
		if (useCmdLineSettings) {
			verifyLayout = settings.isCmdLineVerifyLayout();
		} else {
			verifyLayout = settings.isGuiVerifyLayout();
		}
		if (game.getUiCatalog() == null && verifyLayout) {
			// parse default UI
			throw new IllegalStateException(
					"Base UI of game '" + game.getGameDef().getName() + "' has not been parsed.");
		}
		
		// create tasks for the worker pool
		app.getExecutor().execute(() -> {
			try {
				app.addThreadLoggerTab(Thread.currentThread().getName(), interfaceDirectory.getName(), false);
				// create unique cache path
				final MpqEditorInterface threadsMpqInterface =
						new MpqEditorInterface(configService.getMpqCachePath() + Thread.currentThread().getId(),
								configService.getMpqEditorPath());
				
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
						threadsMpqInterface.setCustomCompressionRules(ruleSet.getCompressionRules());
					}
				}
				threadsMpqInterface.clearCacheExtractedMpq();
				buildFile(interfaceDirectory, game, threadsMpqInterface, compressXml, compressMpqSetting,
						buildUnprotectedToo, repairLayoutOrder, verifyLayout, verifyXml, project);
				threadsMpqInterface.clearCacheExtractedMpq();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final IOException e) {
				logger.error("ERROR: Exception while building UIs.", e); //$NON-NLS-1$
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			}
		});
	}
	
	/**
	 * Parses the baseUI of the GameData, if that is required for the settings. Afterwards, another task is added (e.g.
	 * a build task that might require the baseUI).
	 *
	 * @param gameData
	 * @param useCmdLineSettings
	 * @param followupTask
	 */
	private void parseBaseUiIfNecessary(final GameData gameData, final boolean useCmdLineSettings,
			final Runnable followupTask) {
		final boolean verifyLayout;
		final SettingsIniInterface settings = configService.getIniSettings();
		if (useCmdLineSettings) {
			verifyLayout = settings.isCmdLineVerifyLayout();
		} else {
			verifyLayout = settings.isGuiVerifyLayout();
		}
		if (gameData.getUiCatalog() == null && verifyLayout) {
			baseUiService.parseBaseUI(gameData, followupTask);
		} else {
			addTaskToExecutor(followupTask);
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
	private void buildFile(final File sourceFile, final GameData game, final MpqEditorInterface mpqi,
			final boolean compressXml, final int compressMpq, final boolean buildUnprotectedToo,
			final boolean repairLayoutOrder, final boolean verifyLayout, final boolean verifyXml, final Project project)
			throws IOException, InterruptedException {
		app.printInfoLogMessageToGeneral(sourceFile.getName() + " started construction.");
		
		final GameDef gameDef = game.getGameDef();
		
		final String targetPath =
				configService.getDocumentsPath() + File.separator + gameDef.getDocumentsGameDirectoryName() +
						File.separator + gameDef.getDocumentsInterfaceSubdirectoryName();
		
		// init mod data
		final ModData mod = new ModData(game);
		mod.setSourceDirectory(sourceFile);
		final File targetFile = new File(targetPath);
		mod.setTargetFile(targetFile);
		
		// get and create cache
		final File cache = new File(mpqi.getMpqCachePath());
		if (!cache.exists() && !cache.mkdirs()) {
			final String msg = "Unable to create cache directory."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		int cacheClearAttempts;
		for (cacheClearAttempts = 0; cacheClearAttempts <= 100; cacheClearAttempts++) {
			if (!mpqi.clearCacheExtractedMpq()) {
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} else {
				// success
				break;
			}
		}
		if (cacheClearAttempts > 100) {
			final String msg = "ERROR: Cache could not be cleared"; //$NON-NLS-1$
			logger.error(msg);
			return;
		}
		mod.setMpqCacheDirectory(cache);
		// put files into cache
		int copyAttempts;
		for (copyAttempts = 0; copyAttempts <= 100; copyAttempts++) {
			try {
				fileService.copyFileOrDirectory(sourceFile, cache);
				break;
			} catch (final FileSystemException e) {
				if (copyAttempts == 0) {
					logger.warn("Attempt to copy directory failed.", e); //$NON-NLS-1$
				} else if (copyAttempts >= 100) {
					final String msg =
							"Unable to copy directory after 100 copy attempts: " + e.getMessage(); //$NON-NLS-1$
					logger.error(msg, e);
					throw new FileSystemException(msg);
				}
				// sleep and hope the file gets released soon
				Thread.sleep(500);
			} catch (final IOException e) {
				final String msg = "Unable to copy directory"; //$NON-NLS-1$
				logger.error(msg, e);
			}
		}
		if (copyAttempts > 100) {
			// copy keeps failing -> abort
			final String msg = "Above code did not throw exception about copy attempt threshold reached."; //$NON-NLS-1$
			logger.error(msg);
			throw new IOException(msg);
		}
		
		final File componentListFile = mpqi.getComponentListFile();
		mod.setComponentListFile(componentListFile);
		
		final DescIndexData descIndexData = new DescIndexData(mpqi);
		mod.setDescIndexData(descIndexData);
		
		try {
			descIndexData.setDescIndexPathAndClear(ComponentsListReader.getDescIndexPath(componentListFile, gameDef));
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			final String msg = "ERROR: unable to read DescIndex path."; //$NON-NLS-1$
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
		
		final File descIndexFile = mpqi.getFileFromMpq(descIndexData.getDescIndexIntPath());
		try {
			descIndexData.addLayoutIntPath(DescIndexReader.getLayoutPathList(descIndexFile, false));
		} catch (final SAXException | ParserConfigurationException | IOException | MpqException e) {
			logger.error("unable to read Layout paths", e); //$NON-NLS-1$
		}
		
		logger.info("Compiling... " + sourceFile.getName()); //$NON-NLS-1$
		
		// perform checks/improvements on code
		compileService.compile(mod, "Terr", repairLayoutOrder, verifyLayout, verifyXml); //$NON-NLS-1$
		
		logger.info("Building... " + sourceFile.getName()); //$NON-NLS-1$
		
		try {
			mpqi.buildMpq(targetPath, sourceFile.getName(), compressXml, getCompressionModeOfSetting(compressMpq),
					buildUnprotectedToo);
			
			project.setLastBuildDate(new Date());
			final long size = new File(targetPath + File.separator + sourceFile.getName()).length();
			project.setLastBuildSize(size);
			logger.info("Finished building... " + sourceFile.getName() + ". Size: " + (size / 1024) + " " + "kb");
			//$NON-NLS-1$
			projectService.saveProject(project);
			app.printInfoLogMessageToGeneral(sourceFile.getName() + " finished construction.");
		} catch (final IOException | MpqException e) {
			logger.error("ERROR: unable to construct final Interface file.", e); //$NON-NLS-1$
			app.printErrorLogMessageToGeneral(sourceFile.getName() + " could not be created.");
		}
	}
	
	/**
	 * Adds a task to the executor.
	 *
	 * @param followupTask
	 */
	private void addTaskToExecutor(final Runnable followupTask) {
		if (followupTask != null) {
			app.getExecutor().execute(followupTask);
		}
	}
	
	/**
	 * @param compressMpqSetting
	 * @return
	 */
	private MpqEditorCompression getCompressionModeOfSetting(final int compressMpqSetting) {
		switch (compressMpqSetting) {
			case 0:
				return MpqEditorCompression.NONE;
			case 1:
				return MpqEditorCompression.BLIZZARD_SC2_HEROES;
			case 2:
				return MpqEditorCompression.CUSTOM;
			case 3:
				return MpqEditorCompression.SYSTEM_DEFAULT;
			default:
				throw new IllegalArgumentException("Unsupported mpq compression mode.");
		}
	}
}
