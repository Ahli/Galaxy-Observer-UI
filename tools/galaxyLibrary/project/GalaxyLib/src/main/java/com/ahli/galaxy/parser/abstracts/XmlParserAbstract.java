// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.abstracts;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;

public abstract class XmlParserAbstract implements XmlParser {
	
	protected ParsedXmlConsumer consumer;
	
	public XmlParserAbstract(final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
	}
	
}
