package com.ahli.galaxy;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the Components List file within the MPQ Archive.
 * 
 * @author Ahli
 *
 */
public class ComponentsListReader {
	static Logger LOGGER = LogManager.getLogger(ComponentsListReader.class);
	
	/**
	 * Returns the internal path of DescIndex of the mpq file.
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static String getDescIndexPath(final File f) throws ParserConfigurationException, SAXException, IOException {
		final String intPath = getComponentsListValue(f, "uiui");
		String str = intPath.endsWith("StormLayout") ? "Base.StormData" : "Base.SC2Data";
		str += File.separator + intPath;
		LOGGER.trace("DescIndexPath: " + str);
		return str;
	}
	
	/**
	 * Returns the path information of a given type value, e.g. "uiui" or "font".
	 * Locales are not supported here.
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static String getComponentsListValue(final File f, final String typeVal)
			throws ParserConfigurationException, SAXException, IOException {
		// find the type in the xml
		final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document doc = dBuilder.parse(f);
		
		// must be in a DataComponent node
		final NodeList nodeList = doc.getElementsByTagName("DataComponent");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			final Node attrZero = node.getAttributes().item(0);
			// first attribute's name is Type & value must be as specified
			if (attrZero.getNodeName().equals("Type") && attrZero.getNodeValue().equals(typeVal)) {
				// the text is the value between the tags
				return node.getTextContent();
			}
		}
		
		return null;
	}
}
