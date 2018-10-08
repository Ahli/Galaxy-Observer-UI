package interfacebuilder.base_ui;

import cascexplorerconfigedit.editor.CascExplorerConfigFileEditor;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.FileService;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.projects.enums.Game;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

public class BaseUiService {
	private static final String UNKNOWN_GAME_EXCEPTION = "Unknown Game";
	private static final Logger logger = LogManager.getLogger(BaseUiService.class);
	
	private final InterfaceBuilderApp app = InterfaceBuilderApp.getInstance();
	
	@Autowired
	private ConfigService configService;
	@Autowired
	private GameService gameService;
	@Autowired
	private FileService fileService;
	@Autowired
	private DiscCacheService discCacheService;
	
	/**
	 * Checks if the specified game's baseUI is older than the game files.
	 *
	 * @param game
	 * @param usePtr
	 * @param useX64
	 * @return true, if outdated
	 */
	public boolean isOutdated(final Game game, final boolean usePtr, final boolean useX64) throws IOException {
		final String gamePath = getGamePath(game, usePtr);
		final GameDef gameDef = gameService.getGameDef(game);
		final String supportDir = (useX64 ? gameDef.getSupportDirectoryX64() : gameDef.getSupportDirectoryX32());
		final String switcherExe = (useX64 ? gameDef.getSwitcherExeNameX64() : gameDef.getSwitcherExeNameX32());
		final File updatedExe = new File(gamePath + File.separator + supportDir + File.separator + switcherExe);
		final File gameBaseUI = new File(configService.getBaseUiPath(gameDef));
		
		if (!gameBaseUI.exists()) {
			return true;
		}
		
		return fileService.isEmptyDirectory(gameBaseUI) ||
				!fileService.directoryFilesAreUpToDate(updatedExe.lastModified(), gameBaseUI);
	}
	
	/**
	 * Returns the game path.
	 *
	 * @param game
	 * @param usePtr
	 * @return
	 */
	private String getGamePath(final Game game, final boolean usePtr) {
		final SettingsIniInterface iniSettings = configService.getIniSettings();
		final String gamePath;
		if (game.equals(Game.SC2)) {
			gamePath = iniSettings.getSc2Path();
		} else {
			if (game.equals(Game.HEROES)) {
				gamePath = usePtr ? iniSettings.getHeroesPtrPath() : iniSettings.getHeroesPath();
			} else {
				throw new InvalidParameterException(UNKNOWN_GAME_EXCEPTION);
			}
		}
		return gamePath;
	}
	
	/**
	 * Creates Tasks that will extract the base UI for a specified game.
	 *
	 * @param game
	 * @param usePtr
	 */
	public void extract(final Game game, final boolean usePtr) {
		logger.info(String.format("Extracting baseUI for %s", game.toString()));
		prepareCascExplorerConfig(game, usePtr);
		
		final ThreadPoolExecutor executor = InterfaceBuilderApp.getInstance().getExecutor();
		final GameDef gameDef = gameService.getGameDef(game);
		final File destination = new File(configService.getBaseUiPath(gameDef));
		
		try {
			if (!destination.exists() && !destination.mkdirs()) {
				logger.error(String.format("Directory %s could not be created.", destination));
				return;
			}
			fileService.cleanDirectory(destination);
			discCacheService.remove(gameDef.getName(), usePtr);
		} catch (final IOException e) {
			logger.error(String.format("Directory %s could not be cleaned.", destination), e);
			return;
		}
		
		final File extractorExe = configService.getCascExtractorConsoleExeFile();
		final String[] queryMasks = getQueryMasks(game);
		for (final String mask : queryMasks) {
			final Runnable task = () -> {
				try {
					if (extract(extractorExe, mask, destination)) {
						Thread.sleep(50);
						if (extract(extractorExe, mask, destination)) {
							logger.warn(
									"Extraction failed due to a file access. Try closing the Battle.net App, if it " +
											"is" + " " + "running and this fails to extract all files.");
						}
					}
				} catch (final IOException e) {
					logger.error("Extracting files from CASC via CascExtractor failed.", e);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			};
			executor.execute(task);
		}
		
	}
	
	/**
	 * Edits the config file of the CASCexplorer.
	 *
	 * @param game
	 * @param usePtr
	 */
	private void prepareCascExplorerConfig(final Game game, final boolean usePtr) {
		final File configFile = configService.getCascExtractorConfigFile();
		final String storagePath;
		final String onlineMode = "False";
		final String product;
		final String locale = "enUS";
		switch (game) {
			case SC2:
				storagePath = configService.getIniSettings().getSc2Path();
				product = "sc2";
				break;
			case HEROES:
				if (usePtr) {
					storagePath = configService.getIniSettings().getHeroesPtrPath();
				} else {
					storagePath = configService.getIniSettings().getHeroesPath();
				}
				product = "heroes";
				break;
			default:
				throw new InvalidParameterException(UNKNOWN_GAME_EXCEPTION);
		}
		CascExplorerConfigFileEditor.write(configFile, storagePath, onlineMode, product, locale);
	}
	
	private String[] getQueryMasks(final Game game) {
		switch (game) {
			case SC2:
				return new String[] { "*.SC2Layout", "*Assets.txt", "*.SC2Style" };
			case HEROES:
				return new String[] { "*.stormlayout", "*assets.txt", "*.stormstyle" };
			default:
				throw new InvalidParameterException(UNKNOWN_GAME_EXCEPTION);
		}
	}
	
	/**
	 * @param extractorExe
	 * @param mask
	 * @param destination
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean extract(final File extractorExe, final String mask, final File destination)
			throws IOException, InterruptedException {
		final ProcessBuilder pb =
				new ProcessBuilder(extractorExe.getAbsolutePath(), mask, destination + File.separator, "enUS", "None");
		// put error and normal output into the same stream
		pb.redirectErrorStream(true);
		
		boolean retry = false;
		final Process process = pb.start();
		// empty output buffers and print to console
		try (final InputStream is = process.getInputStream()) {
			do {
				Thread.sleep(50);
				final String log = IOUtils.toString(is, Charset.defaultCharset());
				if (log.contains("Unhandled Exception: System.IO.IOException: The process cannot access the file")) {
					retry = true;
				} else {
					logger.info(log);
				}
			} while (process.isAlive());
		}
		return retry;
	}
	
	/**
	 * Parses the baseUI of the specified game in its own thread. Afterwards, a specified followupTask is executed. The
	 * parsing of the baseUI is synchronized.
	 *
	 * @param game
	 * 		game whose default UI is parsed
	 * @param followupTask
	 */
	public void parseBaseUI(final GameData game, final Runnable followupTask) {
		// create tasks for the worker pool
		app.getExecutor().execute(() -> {
			// lock per game
			synchronized (game.getGameDef().getName()) {
				UICatalog uiCatalog = game.getUiCatalog();
				final String gameName = game.getGameDef().getName();
				if (uiCatalog != null) {
					logger.trace("Aborting parsing baseUI for '" + gameName + "' as was already parsed.");
				} else {
					boolean needToParseAgain = true;
					final boolean isPtr = !(game.getGameDef() instanceof SC2GameDef) &&
							configService.getIniSettings().isHeroesPtrActive();
					if (discCacheService.exists(gameName, isPtr)) {
						// load from cache
						try {
							uiCatalog = discCacheService.get(gameName, isPtr, UICatalogImpl.class);
							game.setUiCatalog(uiCatalog);
							needToParseAgain = false;
							logger.trace("Loaded UI from cache");
						} catch (final IOException e) {
							logger.warn("ERROR: loading cached base UI failed.", e);
						}
					}
					if (needToParseAgain) {
						// parse baseUI
						uiCatalog = new UICatalogImpl();
						app.printInfoLogMessageToGeneral("Starting to parse base " + gameName + " UI.");
						app.addThreadLoggerTab(Thread.currentThread().getName(),
								game.getGameDef().getNameHandle() + "UI", true);
						final String gameDir = configService.getBaseUiPath(game.getGameDef()) + File.separator +
								game.getGameDef().getModsSubDirectory();
						try {
							final WildcardFileFilter fileFilter =
									new WildcardFileFilter("descindex.*layout", IOCase.INSENSITIVE);
							for (final String modOrDir : game.getGameDef().getCoreModsOrDirectories()) {
								
								final File directory = new File(gameDir + File.separator + modOrDir);
								if (!directory.exists() || !directory.isDirectory()) {
									throw new IOException("BaseUI out of date.");
								}
								
								final Collection<File> descIndexFiles =
										FileUtils.listFiles(directory, fileFilter, TrueFileFilter.INSTANCE);
								logger.info("number of descIndexFiles found: " + descIndexFiles.size());
								
								for (final File descIndexFile : descIndexFiles) {
									logger.info("parsing descIndexFile '" + descIndexFile.getPath() + "'");
									uiCatalog.processDescIndex(descIndexFile, game.getGameDef().getDefaultRaceId());
								}
							}
							game.setUiCatalog(uiCatalog);
						} catch (final SAXException | IOException | ParserConfigurationException e) {
							logger.error("ERROR parsing base UI catalog for '" + gameName + "'.", e);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						} finally {
							uiCatalog.clearParser();
						}
						final String msg = "Finished parsing base UI for " + gameName + ".";
						logger.info(msg);
						app.printInfoLogMessageToGeneral(msg);
						try {
							discCacheService.put(uiCatalog, gameName, isPtr);
						} catch (final IOException e) {
							logger.error("ERROR when creating cache file of UI", e);
						}
					}
				}
			}
			
			addTaskToExecutor(followupTask);
		});
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
	
}
