package com.ahli.galaxy.parser;

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
