package com.ahli.galaxy.parser.interfaces;

import com.ahli.galaxy.ui.exception.UIException;

import java.util.List;

public interface ParsedXmlConsumer {
	
	void parse(int level, String lowerCase, List<String> attrTypes, List<String> attrValues) throws UIException;
	
	void endLayoutFile();
	
}
