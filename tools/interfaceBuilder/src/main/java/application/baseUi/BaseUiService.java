package application.baseUi;

import application.CascExplorerConfigFileEditor;
import application.InterfaceBuilderApp;
import application.compress.GameService;
import application.config.ConfigService;
import application.integration.FileService;
import application.integration.SettingsIniInterface;
import application.projects.enums.Game;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class BaseUiService {
	public static final String UNKNOWN_GAME_EXCEPTION = "Unknown Game";
	private static final char QUOTE = '\"';
	private final Logger logger = LogManager.getLogger();
	@Autowired
	ConfigService configService;
	@Autowired
	GameService gameService;
	@Autowired
	FileService fileService;
	
	/**
	 * Checks if the specified game's baseUI is older than the game files.
	 *
	 * @param game
	 * @param usePtr
	 * @return true, if outdated
	 */
	public boolean isOutdated(final Game game, final boolean usePtr) throws IOException {
		final String gamePath = getGamePath(game, usePtr);
		final GameDef gameDef = gameService.getGameDef(game);
		final File gameExe =
				new File(gamePath + File.separator + (usePtr ? gameDef.getPtrRootExeName() : gameDef.getRootExeName()));
		final File gameBaseUI = new File(configService.getBaseUiPath(gameDef));
		
		if (!gameBaseUI.exists()) {
			return true;
		}
		
		return fileService.isEmptyDirectory(gameBaseUI) ||
				!fileService.directoryFilesAreUpToDate(gameExe.lastModified(), gameBaseUI);
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
			if (!destination.exists()) {
				if (!destination.mkdirs()) {
					logger.error(String.format("Directory %s could not be created.", destination));
					return;
				}
			}
			fileService.cleanDirectory(destination);
		} catch (final IOException e) {
			logger.error(String.format("Directory %s could not be cleaned.", destination), e);
			return;
		}
		
		final File extractorExe = configService.getCascExtractorConsoleExeFile();
		final String[] queryMasks = getQueryMasks(game);
		int i = 0;
		for (final String mask : queryMasks) {
			final int sleepDuration = 1000 + i * 5000;
			final Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						final String cmd =
								"cmd start cmd /C " + QUOTE + QUOTE + extractorExe + QUOTE + " " + mask + " " +
										destination + File.separator + " " + "enUS" + " " + "None" + QUOTE;
						logger.trace("executing: " + cmd);
						// TODO replace with proper check if config file finished writing
						Thread.sleep(sleepDuration);
						//						Runtime.getRuntime().exec(cmd).waitFor();
						Runtime.getRuntime().exec(cmd);
						//						logger.trace("finished executing: " + cmd);
					} catch (final IOException e) {
						logger.error("Extracting files from CASC via CascExtractor failed.", e); //$NON-NLS-1$
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			};
			executor.execute(task);
			i++;
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
				return new String[] { "*.StormLayout", "*Assets.txt", "*.StormStyle" };
			default:
				throw new InvalidParameterException(UNKNOWN_GAME_EXCEPTION);
		}
	}
}
