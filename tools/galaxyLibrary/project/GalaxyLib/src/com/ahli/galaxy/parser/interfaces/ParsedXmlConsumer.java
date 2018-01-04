package com.ahli.galaxy.parser.interfaces;

import java.util.List;

import com.ahli.galaxy.ui.exception.UIException;

public interface ParsedXmlConsumer {
	
	void parse(int level, String lowerCase, List<String> attrTypes, List<String> attrValues) throws UIException;
	
	void endLayoutFile();
	
}
