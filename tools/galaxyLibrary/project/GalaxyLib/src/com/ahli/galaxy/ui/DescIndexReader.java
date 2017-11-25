package com.ahli.galaxy.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads a a Desc Index File.
 * 
 * @author Ahli
 *
 */
public final class DescIndexReader {
	private static Logger logger = LogManager.getLogger(DescIndexReader.class);
	
	/**
	 * 
	 */
	private DescIndexReader() {
	}
	
	/**
	 * Grabs all layout file paths from a given descIndex file.
	 * 
	 * @param f
	 *            descIndex file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static ArrayList<String> getLayoutPathList(final File f, final boolean ignoreRequiredToLoadEntries)
			throws SAXException, IOException, ParserConfigurationException {
		final ArrayList<String> list = new ArrayList<>();
		
		final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		logger.trace("reading layouts from descIndexFile: " + f);
		final Document doc = dBuilder.parse(f);
		
		// must be in a DataComponent node
		final NodeList nodeList = doc.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			if (node.getNodeName().equalsIgnoreCase("Include")) {
				final NamedNodeMap attributes = node.getAttributes();
				final String path = attributes.item(0).getNodeValue();
				
				// ignore requiredtoload if desired
				if (ignoreRequiredToLoadEntries && UICatalog.isFailingRequiredToLoad(attributes)) {
					continue;
				}
				
				list.add(path);
				logger.trace("Adding layout path to layoutPathList: " + path);
			}
		}
		return list;
	}
	
}
