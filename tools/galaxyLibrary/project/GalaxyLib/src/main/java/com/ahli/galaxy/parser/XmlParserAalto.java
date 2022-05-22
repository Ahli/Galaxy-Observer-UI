// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser;

import com.ahli.galaxy.parser.abstracts.XmlParserAbstract;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.ui.exceptions.UIException;
import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class XmlParserAalto extends XmlParserAbstract {
	private AsyncXMLInputFactory asyncInputFactory;
	@Nullable
	private List<String> attrTypes;
	@Nullable
	private List<String> attrValues;
	
	public XmlParserAalto() {
		super(null);
	}
	
	/**
	 * @param consumer
	 */
	public XmlParserAalto(@Nullable final ParsedXmlConsumer consumer) {
		super(consumer);
		init();
	}
	
	private void init() {
		attrTypes = new ArrayList<>(3);
		attrValues = new ArrayList<>(3);
		asyncInputFactory = new InputFactoryImpl();
		asyncInputFactory.configureForSpeed();
	}
	
	@Override
	public void setConsumer(@Nullable final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
		init();
	}
	
	@Override
	public void clear() {
		attrTypes = null;
		attrValues = null;
		consumer = null;
		asyncInputFactory = null;
	}
	
	@Override
	public void parseFile(@NotNull final Path p) throws IOException {
		if (consumer == null || attrTypes == null || attrValues == null || asyncInputFactory == null) {
			throw new IllegalStateException("No consumer set");
		}
		if (log.isTraceEnabled()) {
			log.trace("parsing layout file: {}", p.getFileName());
		}
		try {
			final byte[] bytes = Files.readAllBytes(p);
			final int offset = bytes[0] == -17 && bytes[1] == -69 && bytes[2] == -65 ? 3 : 0;
			final AsyncXMLStreamReader<AsyncByteArrayFeeder> parser =
					asyncInputFactory.createAsyncFor(bytes, offset, bytes.length - offset);
			int eventType = parser.next();
			if (eventType == XMLStreamConstants.START_DOCUMENT) {
				while ((eventType = parser.next()) != XMLStreamConstants.END_DOCUMENT) {
					if (eventType == XMLStreamConstants.START_ELEMENT) {
						final String tagName = parser.getName().getLocalPart().toLowerCase(Locale.ROOT);
						final int attributeCount = parser.getAttributeCount();
						for (int i = 0; i < attributeCount; ++i) {
							attrTypes.add(parser.getAttributeName(i).getLocalPart().toLowerCase(Locale.ROOT));
							attrValues.add(parser.getAttributeValue(i));
						}
						consumer.parse(parser.getDepth(), tagName, attrTypes, attrValues);
						attrTypes.clear();
						attrValues.clear();
					} else if (eventType == AsyncXMLStreamReader.EVENT_INCOMPLETE) {
						consumer.endLayoutFile();
						return;
					}
				}
			}
		} catch (final IOException | UIException | XMLStreamException e) {
			throw new IOException("ERROR during XML parsing.", e);
		}
		consumer.endLayoutFile();
	}
	
}

