// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser.abstracts;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import org.jetbrains.annotations.Nullable;

public abstract class XmlParserAbstract implements XmlParser {
	
	@Nullable
	protected ParsedXmlConsumer consumer;
	
	protected XmlParserAbstract(@Nullable final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
	}
	
}
