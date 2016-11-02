package application;


import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Ahli
 *
 */
public class ComponentsListReader {

	/**
	 * Returns the internal path of DescIndex of the mpq file.
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static String getDescIndexPath(File f) throws ParserConfigurationException, SAXException, IOException {
		String intPath = getComponentsListValue(f, "uiui");
		String str = intPath.endsWith("StormLayout") ? "Base.StormData" : "Base.SC2Data";
		return str + File.separator + intPath;
	}

	/**
	 * Returns the path information of a given type value, e.g. "uiui" or
	 * "font". Locales are not supported here.
	 * 
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static String getComponentsListValue(File f, String typeVal) throws ParserConfigurationException, SAXException, IOException {
		// find the type in the xml
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		// must be in a DataComponent node
		NodeList nodeList = doc.getElementsByTagName("DataComponent");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Node attrZero = node.getAttributes().item(0);
			// first attribute's name is Type & value must be as specified
			if (attrZero.getNodeName().equals("Type") && attrZero.getNodeValue().equals(typeVal)) {
				// the text is the value between the tags
				return node.getTextContent();
				// System.out.println("descIndex: "+descIndexIntPath);
			}
		}

		return null;
	}
}
