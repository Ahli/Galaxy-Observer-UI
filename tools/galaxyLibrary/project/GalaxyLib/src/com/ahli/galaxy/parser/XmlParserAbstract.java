package com.ahli.galaxy.parser;

public abstract class XmlParserAbstract implements XmlParser {
	
	protected ParsedXmlConsumer consumer;
	
	public XmlParserAbstract(final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
	}
	
}
