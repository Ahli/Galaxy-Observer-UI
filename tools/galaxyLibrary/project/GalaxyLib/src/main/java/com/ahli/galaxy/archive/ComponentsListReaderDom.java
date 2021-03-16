// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.archive;

import com.ahli.galaxy.game.GameDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Reads the Components List file within the MPQ Archive.
 *
 * @author Ahli
 */
public final class ComponentsListReaderDom {
	private static final String TYPE = "Type";
	private static final String DATA_COMPONENT = "DataComponent";
	private static final String UIUI = "uiui";
	private static final Logger logger = LogManager.getLogger(ComponentsListReaderDom.class);
	
	/**
	 *
	 */
	private ComponentsListReaderDom() {
	}
	
	/**
	 * Returns the internal path of DescIndex of the mpq file.
	 *
	 * @param compListFile
	 * 		components list file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static String getDescIndexPath(final Path compListFile, final GameDef game)
			throws ParserConfigurationException, SAXException, IOException {
		final String str = game.baseDataFolderName() + File.separator + getComponentsListValue(compListFile, UIUI);
		logger.trace("DescIndexPath: {}", str);
		return str;
	}
	
	/**
	 * Returns the path information of a given type value, e.g. "uiui" or "font". Locales are not supported here.
	 *
	 * @param compListFile
	 * 		components list file
	 * @param typeVal
	 * 		value of the type, e.g. "uiui"
	 * @return path information of the specified type value
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static String getComponentsListValue(final Path compListFile, final String typeVal)
			throws ParserConfigurationException, SAXException, IOException {
		// find the type in the xml
		final DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setNamespaceAware(false);
		dbFac.setValidating(false);
		dbFac.setAttribute("http://xml.org/sax/features/external-general-entities", false);
		dbFac.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbFac.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbFac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbFac.setXIncludeAware(false);
		dbFac.setExpandEntityReferences(false);
		dbFac.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbFac.setIgnoringComments(true);
		final DocumentBuilder dBuilder = dbFac.newDocumentBuilder();
		final Document doc = dBuilder.parse(compListFile.toString());
		
		// must be in a DataComponent node
		final NodeList nodeList = doc.getElementsByTagName(DATA_COMPONENT);
		for (int i = 0, len = nodeList.getLength(); i < len; ++i) {
			final Node node = nodeList.item(i);
			final Node attrZero = node.getAttributes().item(0);
			// first attribute's name is Type & value must be as specified
			if (TYPE.equals(attrZero.getNodeName()) && attrZero.getNodeValue().equals(typeVal)) {
				// the text is the value between the tags
				return node.getTextContent();
			}
		}
		
		return null;
	}
}
