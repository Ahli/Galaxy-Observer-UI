// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.interfaces;

import com.ahli.galaxy.ui.exceptions.UIException;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ParsedXmlConsumer {
	
	void parseFile(@NonNull Path p, @NonNull String raceId, boolean isDevLayout, @NonNull String consoleSkinId)
			throws IOException;
	
	void parse(int level, @NonNull String tagName, @NonNull List<String> attrTypes, @NonNull List<String> attrValues)
			throws UIException;
	
	void endLayoutFile();
	
	void deduplicate();
}
