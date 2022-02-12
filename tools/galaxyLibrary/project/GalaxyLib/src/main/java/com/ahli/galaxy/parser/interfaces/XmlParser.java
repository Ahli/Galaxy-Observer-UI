// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for Parser
 *
 * @author Ahli
 */
public interface XmlParser {
	
	/**
	 * Parses a file and calls a consumer.
	 *
	 * @param p
	 * @throws IOException
	 */
	void parseFile(@NotNull final Path p) throws IOException;
	
	/**
	 * Release the allocated resources.
	 */
	void clear();
	
	void setConsumer(@Nullable ParsedXmlConsumer consumer);
}
