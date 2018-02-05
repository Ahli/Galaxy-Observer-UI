package com.ahli.galaxy.archive;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Reads the Components List file within the MPQ Archive.
 *
 * @author Ahli
 */
public final class ComponentsListReader {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 *
	 */
	private ComponentsListReader() {
	}
	
	/**
	 * Returns the internal path of DescIndex of the mpq file.
	 *
	 * @param f
	 * 		components list file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static String getDescIndexPath(final File f, final GameDef game) throws ParserConfigurationException, SAXException, IOException {
		final String str = game.getBaseDataFolderName() + File.separator + getComponentsListValue(f, "uiui");
		logger.trace("DescIndexPath: {}", () -> str);
		return str;
	}
	
	/**
	 * Returns the path information of a given type value, e.g. "uiui" or "font".
	 * Locales are not supported here.
	 *
	 * @param f
	 * 		components list file
	 * @param typeVal
	 * 		value of the type, e.g. "uiui"
	 * @return path information of the specified type value
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static String getComponentsListValue(final File f, final String typeVal) throws ParserConfigurationException, SAXException, IOException {
		// find the type in the xml
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		final DocumentBuilder dBuilder = factory.newDocumentBuilder();
		final Document doc = dBuilder.parse(f);
		
		// must be in a DataComponent node
		final NodeList nodeList = doc.getElementsByTagName("DataComponent");
		for (int i = 0, len = nodeList.getLength(); i < len; i++) {
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
