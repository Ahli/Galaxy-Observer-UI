package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Application that edits the
 * 
 * 
 * 
 * @author Ahli
 *
 */
public class Main {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		System.out.println("Command Line Parameters: [ConfigFilePath] [StoragePath] [OnlineMode] [Product] [Locale]");
		System.out.println("Parameters provided:");
		System.out.println("ConfigFilePath: " + args[0]);
		System.out.println("StoragePath: " + args[1]);
		System.out.println("OnlineMode: " + args[2]);
		System.out.println("Product: " + args[3]);
		System.out.println("Locale: " + args[4]);
		
		final String path = args[0], storagePath = args[1], onlineMode = args[2], product = args[3], locale = args[4];
		
		InputStream is = null;
		try {
			final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			final File f = new File(path);
			
			// parse XML file
			
			// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
			// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
			// doc = dBuilder.parse(curFile);
			
			// WORKAROUND -> provide Inputstream
			is = new FileInputStream(f);
			doc = dBuilder.parse(is);
			is.close();
			
			// edit document
			final NodeList nodeList = doc.getElementsByTagName("setting");
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node curNode = nodeList.item(i);
				
				final NamedNodeMap attributes = curNode.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					final Node attrNode = attributes.item(j);
					
					if (attrNode != null) {
						final String val = attrNode.getNodeValue();
						
						if (val.equalsIgnoreCase("StoragePath")) {
							replaceValueInSettingNode(curNode, storagePath);
						} else if (val.equalsIgnoreCase("OnlineMode")) {
							replaceValueInSettingNode(curNode, onlineMode);
						} else if (val.equalsIgnoreCase("Product")) {
							replaceValueInSettingNode(curNode, product);
						} else if (val.equalsIgnoreCase("Locale")) {
							replaceValueInSettingNode(curNode, locale);
						}
					}
				}
			}
			
			// write DOM back to XML
			final Source source = new DOMSource(doc);
			final Result result = new StreamResult(f);
			Transformer xformer;
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
			
		} catch (IOException | ParserConfigurationException | SAXException | TransformerFactoryConfigurationError
				| TransformerException e1) {
			e1.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
				}
			}
		}
	}
	
	/**
	 * 
	 * @param settingNode
	 * @param newSettingVal
	 */
	private static void replaceValueInSettingNode(final Node settingNode, final String newSettingVal) {
		final NodeList children = settingNode.getChildNodes();
		x: for (int a = 0; a < children.getLength(); a++) {
			final Node curChild = children.item(a);
			if (curChild.getNodeName().equalsIgnoreCase("value")) {
				curChild.setTextContent(newSettingVal);
				break x;
			}
		}
		
	}
	
}
