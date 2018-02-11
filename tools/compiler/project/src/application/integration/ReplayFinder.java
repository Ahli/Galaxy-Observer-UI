/*
 *
 */
package application.integration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Class that finds replays.
 *
 * @author Ahli
 */
public class ReplayFinder {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Gets the last or newest replay.
	 *
	 * @param isHeroes
	 * 		gameIsHeroes
	 * @param documentsPath
	 * 		the documents path
	 * @return the last or newest replay
	 */
	public File getLastUsedOrNewestReplay(final boolean isHeroes, final String documentsPath) {
		File replay = null;
		try {
			replay = getLastUsedReplay(isHeroes, documentsPath);
		} catch (final IOException e) {
			logger.error("Failed to receive last used replay.", e);
		}
		if (replay == null || !replay.exists() || !replay.isFile()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Last used replay is invalid, getting newest replay instead.");
			}
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
	public File getLastUsedReplay(final boolean isHeroes, final String documentsPath) throws IOException {
		String basePath = documentsPath + File.separator;
		if (isHeroes) {
			basePath += "Heroes of the Storm";
		} else {
			basePath += "StarCraft II";
		}
		basePath += File.separator + "Variables.txt";
		if (logger.isTraceEnabled()) {
			logger.trace(basePath);
		}
		
		String line, replayPath = null;
		try (final InputStreamReader is = new InputStreamReader(new FileInputStream(new File(basePath)),
				StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(is)) {
			
			boolean found = false;
			final String searchToken = "lastReplayFilePath=";
			while ((line = br.readLine()) != null && !found) {
				if (line.startsWith(searchToken)) {
					found = true;
					replayPath = line.substring(searchToken.length());
				}
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("replayPath: " + replayPath);
		}
		if (replayPath == null) {
			return null;
		}
		return new File(replayPath);
	}
	
	/**
	 * Returns the newest Replay file for the game.
	 *
	 * @param isHeroes
	 * @param documentsPath
	 * 		the documents path
	 * @return the newest replay
	 */
	public File getNewestReplay(final boolean isHeroes, final String documentsPath) {
		String basePath = documentsPath + File.separator;
		final String[] extensions;
		if (isHeroes) {
			basePath += "Heroes of the Storm";
			extensions = new String[] { "StormReplay" };
		} else {
			basePath += "StarCraft II";
			extensions = new String[] { "SC2Replay" };
		}
		basePath += File.separator + "Accounts";
		if (logger.isTraceEnabled()) {
			logger.trace(basePath);
		}
		
		final Collection<File> allReplays =
				FileUtils.listFiles(new File(basePath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		if (logger.isTraceEnabled()) {
			logger.trace("# Replays found: " + allReplays.size());
		}
		
		long newestDate = Long.MIN_VALUE;
		File newestReplay = null;
		for (final File curReplay : allReplays) {
			// check extension of file
			final String curReplayName = curReplay.getName();
			if (logger.isTraceEnabled()) {
				logger.trace("curReplay name: " + curReplayName);
			}
			final String extension = FilenameUtils.getExtension(curReplayName);
			if (logger.isTraceEnabled()) {
				logger.trace("extension: " + extension);
			}
			if (curReplay.isFile() && extension.equalsIgnoreCase(extensions[0])) {
				// check date
				final long curDate = curReplay.lastModified();
				if (logger.isTraceEnabled()) {
					logger.trace("curDate: " + curDate);
				}
				if (curDate > newestDate) {
					newestDate = curDate;
					newestReplay = curReplay;
				}
			}
		}
		if (newestReplay != null) {
			logger.info("newest Replay: " + newestReplay.getName());
		}
		return newestReplay;
	}
}
