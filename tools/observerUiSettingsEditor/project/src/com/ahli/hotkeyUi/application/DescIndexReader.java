package com.ahli.hotkeyUi.application;

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
 * 
 * @author Ahli
 *
 */
public class DescIndexReader {
	static Logger LOGGER = LogManager.getLogger("DescIndexReader");

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
	public static ArrayList<String> getLayoutPathList(File f, boolean ignoreRequiredToLoadEntries)
			throws SAXException, IOException, ParserConfigurationException {
		ArrayList<String> list = new ArrayList<>();

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		// must be in a DataComponent node
		NodeList nodeList = doc.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equalsIgnoreCase("Include")) {
				NamedNodeMap attributes = node.getAttributes();
				String path = attributes.item(0).getNodeValue();

				// ignore requiredtoload if desired
				if (ignoreRequiredToLoadEntries && attributes.getLength() > 1
						&& attributes.item(1).getNodeName().equalsIgnoreCase("requiredtoload")) {
					continue;
				}

				list.add(path);
				LOGGER.debug("Adding layout path to layoutPathList: " + path);
			}
		}
		return list;
	}

	// private List<Node> getElementsByTagNameIgnoreCase(Document doc, String
	// tag) {
	// NodeList children = doc.getChildNodes();
	// return getElementsByTagNameIgnoreCase(children, tag);
	// }
	//
	// private List<Node> getElementsByTagNameIgnoreCase(NodeList children,
	// String tag) {
	// List<Node> list = new ArrayList<Node>();
	// int len = children.getLength();
	// for (int i = 0; i < len; i++) {
	// Node node = children.item(i);
	// if (tag.equalsIgnoreCase(node.getNodeName())) {
	// list.add(node);
	// list.addAll(getElementsByTagNameIgnoreCase(node.getChildNodes(), tag));
	// }
	// }
	// return list;
	// }
}
