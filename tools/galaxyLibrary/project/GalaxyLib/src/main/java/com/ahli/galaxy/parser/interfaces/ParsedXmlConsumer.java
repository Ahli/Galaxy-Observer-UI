// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.interfaces;

import com.ahli.galaxy.ui.exceptions.UIException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ParsedXmlConsumer {
	
	void parseFile(@NotNull Path p, @NotNull String raceId, boolean isDevLayout, @NotNull String consoleSkinId)
			throws IOException;
	
	void parse(int level, @NotNull String tagName, @NotNull List<String> attrTypes, @NotNull List<String> attrValues)
			throws UIException;
	
	void endLayoutFile();
	
	void deduplicate();
}
