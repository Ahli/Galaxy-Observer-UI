// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser;

import com.ahli.galaxy.parser.abstracts.XmlParserAbstract;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.util.XmlDomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XmlParserDom extends XmlParserAbstract {
	private static final String ANY_TAG = "*";
	private static final Logger logger = LoggerFactory.getLogger(XmlParserDom.class);
	private DocumentBuilder dBuilder;
	
	private List<String> attrTypes;
	private List<String> attrValues;
	
	public XmlParserDom() {
		super(null);
	}
	
	/**
	 * @param consumer
	 */
	public XmlParserDom(final ParsedXmlConsumer consumer) {
		super(consumer);
		init();
	}
	
	private void init() {
		try {
			if (dBuilder == null) {
				dBuilder = XmlDomHelper.buildSecureDocumentBuilder();
			}
		} catch (final ParserConfigurationException e) {
			logger.error("Parser configuration error: ", e);
		}
		attrTypes = new ArrayList<>(3);
		attrValues = new ArrayList<>(3);
	}
	
	@Override
	public void setConsumer(final ParsedXmlConsumer consumer) {
		this.consumer = consumer;
		init();
	}
	
	@Override
	public void clear() {
		dBuilder = null;
		attrTypes = null;
		attrValues = null;
		consumer = null;
	}
	
	@Override
	public void parseFile(final Path p) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("parsing layout file: {}", p.getFileName());
		}
		try {
			final NodeList elements = dBuilder.parse(p.toFile()).getElementsByTagName(ANY_TAG);
			Node node;
			NamedNodeMap attributes;
			Node attr;
			int i;
			int j;
			final int len;
			int len2;
			for (j = 0, len = elements.getLength(); j < len; ++j) {
				node = elements.item(j);
				attributes = node.getAttributes();
				if (attributes != null) {
					for (i = 0, len2 = attributes.getLength(); i < len2; ++i) {
						attr = attributes.item(i);
						attrTypes.add(attr.getNodeName().toLowerCase(Locale.ROOT));
						attrValues.add(attr.getNodeValue());
					}
				}
				consumer.parse(
						getLevel(node.getParentNode()),
						node.getNodeName().toLowerCase(Locale.ROOT),
						attrTypes,
						attrValues);
				attrTypes.clear();
				attrValues.clear();
			}
		} catch (final SAXException | IOException | UIException e) {
			throw new IOException("ERROR during XML parsing.", e);
		}
		consumer.endLayoutFile();
	}
	
	/**
	 * Returns the level of the parent's child node.
	 *
	 * @param parent
	 * @return
	 */
	private static int getLevel(final Node parent) {
		int i = 0;
		Node n = parent;
		while (n != null) {
			n = n.getParentNode();
			++i;
		}
		return i;
	}
}
