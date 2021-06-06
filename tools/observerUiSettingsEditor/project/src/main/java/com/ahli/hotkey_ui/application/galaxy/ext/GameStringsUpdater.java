package com.ahli.hotkey_ui.application.galaxy.ext;

import com.ahli.hotkey_ui.application.model.Constants;
import com.ahli.hotkey_ui.application.model.ValueDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

public class GameStringsUpdater extends SimpleFileVisitor<Path> {
	
	private static final Logger logger = LogManager.getLogger(GameStringsUpdater.class);
	
	private final List<ValueDef> gamestringsAddSettings;
	
	public GameStringsUpdater(final List<ValueDef> gamestringsAddSettings) {
		this.gamestringsAddSettings = gamestringsAddSettings;
	}
	
	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		
		if (file.getFileName().toString().equalsIgnoreCase("GameStrings.txt")) {
			
			logger.debug("processing file: {}", file);
			
			StringBuilder gamestrings = new StringBuilder(Files.readString(file, StandardCharsets.UTF_8));
			
			boolean changed = false;
			for (final ValueDef setting : gamestringsAddSettings) {
				final String value = setting.getValue();
				if (Objects.equals(value, Constants.TRUE)) {
					if (setting.getGamestringsAdd().charAt(0) != '\n') {
						gamestrings.append('\n');
					}
					gamestrings.append(setting.getGamestringsAdd());
					changed = true;
				} else if (Objects.equals(value, Constants.FALSE)) {
					gamestrings = new StringBuilder(gamestrings.toString()
							.replaceAll("[\n\r]?" + setting.getGamestringsAdd(), ""));
					changed = true;
				}
			}
			
			if (changed) {
				Files.writeString(file, gamestrings, StandardCharsets.UTF_8);
			}
		}
		
		return FileVisitResult.CONTINUE;
	}
	
}
