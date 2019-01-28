// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.interfaces;

import java.io.File;
import java.io.IOException;

/**
 * Interface for Parser
 *
 * @author Ahli
 */
public interface XmlParser {
	
	/**
	 * Parses a file and calls a consumer.
	 *
	 * @param f
	 * @throws IOException
	 */
	void parseFile(final File f) throws IOException;
	
	/**
	 * Release allocated resources.
	 */
	void clear();
	
	void setConsumer(ParsedXmlConsumer consumer);
}
