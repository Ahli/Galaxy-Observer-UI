package com.ahli.galaxy.parser.abstracts;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;

public abstract class XmlParserAbstract implements XmlParser {
	
	protected ParsedXmlConsumer consumer;
	
	public XmlParserAbstract(final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
	}
	
}
