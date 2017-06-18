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
public class LayoutReader {
	static Logger LOGGER = LogManager.getLogger(LayoutReader.class);

	public static ArrayList<String> getDependencyLayouts(File f, ArrayList<String> ownConstants)
			throws ParserConfigurationException, SAXException, IOException {
		String nameWithFileEnding = f.getName();
		String nameWOfileEnding = nameWithFileEnding.substring(0, Math.max(0, nameWithFileEnding.lastIndexOf('.')));

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		ArrayList<String> list = new ArrayList<>();

		// check TEMPLATES
		NodeList nodes = doc.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node frame = nodes.item(i);
			// check if node is a frame
			if (frame.getNodeName().equalsIgnoreCase("Frame")) {
				NamedNodeMap attributes = frame.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attr = attributes.item(j);

					// attribute is Template
					if (attr.getNodeName().equalsIgnoreCase("template")) {
						String dependency = attr.getNodeValue();
						if (dependency != null) {
							int firstIndex = dependency.indexOf('/');
							int firstIndex2 = dependency.indexOf('\\');
							if (firstIndex < 0) {
								firstIndex = Integer.MAX_VALUE;
							}
							if (firstIndex2 < 0) {
								firstIndex2 = Integer.MAX_VALUE;
							}
							firstIndex = Math.min(firstIndex, firstIndex2);
							String layoutName = dependency.substring(0, firstIndex);
							if (!layoutName.equalsIgnoreCase(nameWOfileEnding)) {
								if (!doesNameAppearInList(layoutName, list)) {
									LOGGER.trace(nameWOfileEnding + " has dependency to " + layoutName);
									list.add(layoutName);
								}
							}
						}

					}

				}

			}

		}

		if (ownConstants == null) {
			ownConstants = getLayoutsConstantDefinitions(doc);
			for (String str : ownConstants) {
				LOGGER.trace(nameWOfileEnding + " defines constant " + str);
			}
		}

		// constantUsage
		// nodes = doc.getElementsByTagName("*");
		ArrayList<String> usedConstants = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			// String nodeName = node.getNodeName();
			// if(nodeName.startsWith("#")){
			// String constName = nodeName;
			// if(!doesNameAppearInList(constName, usedConstants)){
			// System.out.println(nameWOfileEnding + " uses undefined constant "
			// + constName);
			// usedConstants.add(constName);
			// }
			// }

			// if (true) {
			NamedNodeMap attributes = node.getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				Node attribute = attributes.item(j);
				String attrName = attribute.getNodeName();
				String attrValue = attribute.getNodeValue();

				// attribute name
				if (attrName.startsWith("#")) {
					String constName = attrName;
					if (!doesNameAppearInList(constName, usedConstants)
							&& !doesConstantNameAppearInList(constName, ownConstants)) {
						LOGGER.trace(nameWOfileEnding + " uses undefined constant " + constName);
						usedConstants.add(constName);
						list.add(constName);
					}
				}
				// attribute value
				if (attrValue.startsWith("#")) {
					String constName = attrValue;
					if (!doesNameAppearInList(constName, usedConstants)
							&& !doesConstantNameAppearInList(constName, ownConstants)) {
						LOGGER.trace(nameWOfileEnding + " uses undefined constant " + constName);
						usedConstants.add(constName);
						list.add(constName);
					}
				}
			}
			// }

		}

//		// add all constants that are not self-defined to dependency list
//		x:for(String constant : usedConstants){
//			for(String own : ownConstants){
//				if(constant.equals(own)){
//					continue x;
//				}
//			}
//			// if not self-defined => add as dependency
//			list.add(constant);
//			
//		}
			
			
		
		
		
		return list;
		// TODO what if a template uses a constant? what if it is in another
		// TODO what if a constant is defined in a layout after its usage?
		// layout?
	}

	/**
	 * 
	 * @param constUsage
	 * @param list
	 * @return
	 */
	private static boolean doesConstantNameAppearInList(String constUsage, ArrayList<String> list) {

		String name = constUsage;

		if (constUsage.startsWith("#")) {
			if (constUsage.startsWith("##")) {
				name = constUsage.substring(2);
			} else {
				name = constUsage.substring(1);
			}
		}

		for (String n : list) {
			if (n.equalsIgnoreCase(name)) {
//				 System.out.println("check - true - "+constUsage);
				return true;
			}
		}
//		 System.out.println("check - false - "+constUsage+" - "+name);
		return false;
	}

	/**
	 * Checks if a name appears in the list.
	 * 
	 * @param name
	 * @param list
	 * @return
	 */
	private static boolean doesNameAppearInList(String name, ArrayList<String> list) {
		for (String n : list) {
			if (n.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list with Constants defined in the given layout file.
	 * 
	 * @param f
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ArrayList<String> getLayoutsConstantDefinitions(File f)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		return getLayoutsConstantDefinitions(doc);
	}

	/**
	 * Returns a list with Constants defined in the given layout file.
	 * 
	 * @param doc
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ArrayList<String> getLayoutsConstantDefinitions(Document doc) {
		// create list of own constant definitions
		ArrayList<String> ownConstants = new ArrayList<>();
		NodeList constants = doc.getElementsByTagName("Constant");
		for (int i = 0; i < constants.getLength(); i++) {
			Node constant = constants.item(i);
			NamedNodeMap attributes = constant.getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				Node attr = attributes.item(j);
				// attribute is Template
				if (attr.getNodeName().equalsIgnoreCase("name")) {
					ownConstants.add(attr.getNodeValue());
					LOGGER.trace("FOUND CONSTANT DEFINITION: " + attr.getNodeValue());
				}
//				else
//					System.out.println("REJECTED CONSTANT ATTR: " + attr.getNodeName());
			}
		}
		return ownConstants;
	}
}
