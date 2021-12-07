// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Class that finds replays.
 *
 * @author Ahli
 */
public class ReplayService {
	private static final Logger logger = LogManager.getLogger(ReplayService.class);
	
	private final FileService fileService;
	
	public ReplayService(final FileService fileService) {
		this.fileService = fileService;
	}
	
	/**
	 * Gets the last or newest replay.
	 *
	 * @param isHeroes
	 * 		gameIsHeroes
	 * @param documentsPath
	 * 		the documents path
	 * @return the last or newest replay
	 * @throws IOException
	 */
	public Path getLastUsedOrNewestReplay(final boolean isHeroes, final Path documentsPath) throws IOException {
		Path replay = null;
		try {
			replay = getLastUsedReplay(isHeroes, documentsPath);
		} catch (final IOException e) {
			logger.error("Failed to receive last used replay.", e);
		}
		if (replay == null || !Files.exists(replay) || !Files.isRegularFile(replay)) {
			logger.trace("Last used replay is invalid, getting newest replay instead.");
			
			replay = getNewestReplay(isHeroes, documentsPath);
		}
		return replay;
	}
	
	/**
	 * Returns the last used replay file read from the game's Variables.txt.
	 *
	 * @param isHeroes
	 * @param documentsPath
	 * 		the documents path
	 * @return the last used replay
	 * @throws IOException
	 * 		Signals that an I/O exception has occurred.
	 */
	public static Path getLastUsedReplay(final boolean isHeroes, final Path documentsPath) throws IOException {
		final Path basePath = documentsPath.resolve(
				(isHeroes ? "Heroes of the Storm" : "StarCraft II") + File.separator + "Variables.txt");
		logger.trace(basePath);
		
		String line;
		String replayPath = null;
		try (final BufferedReader br = Files.newBufferedReader(basePath)) {
			final String searchToken = "lastReplayFilePath=";
			while ((line = br.readLine()) != null) {
				if (line.startsWith(searchToken)) {
					replayPath = line.substring(searchToken.length());
					break;
				}
			}
		}
		logger.trace("replayPath: {}", replayPath);
		if (replayPath == null) {
			return null;
		}
		return Path.of(replayPath);
	}
	
	/**
	 * Returns the newest Replay file for the game.
	 *
	 * @param isHeroes
	 * @param documentsPath
	 * 		the documents path
	 * @return the newest replay
	 * @throws IOException
	 */
	public Path getNewestReplay(final boolean isHeroes, final Path documentsPath) throws IOException {
		final Path basePath;
		final String extensions;
		if (isHeroes) {
			basePath = documentsPath.resolve("Heroes of the Storm" + File.separator + "Accounts");
			extensions = ".stormreplay";
		} else {
			basePath = documentsPath.resolve("StarCraft II" + File.separator + "Accounts");
			extensions = ".sc2replay";
		}
		logger.trace(basePath);
		
		final List<Path> allReplays = fileService.getFilesOfDirectory(basePath);
		
		logger.trace("# Replays found: {}", allReplays.size());
		
		long newestDate = Long.MIN_VALUE;
		Path newestReplay = null;
		for (final Path curReplay : allReplays) {
			// check extension of file
			final String curReplayName = curReplay.getFileName().toString();
			logger.trace("curReplay name: {}", curReplayName);
			if (curReplayName.toLowerCase(Locale.ROOT).endsWith(extensions)) {
				// check date
				final long curDate = Files.getLastModifiedTime(curReplay).toMillis();
				logger.trace("curDate: {}", curDate);
				if (curDate > newestDate) {
					newestDate = curDate;
					newestReplay = curReplay;
				}
			}
		}
		if (newestReplay != null) {
			logger.info("newest Replay: {}", newestReplay);
		}
		return newestReplay;
	}
}
