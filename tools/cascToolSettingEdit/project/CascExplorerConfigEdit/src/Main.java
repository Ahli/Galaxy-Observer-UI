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
 * 
 * @author Ahli
 *
 */
public class Main {
	public static void main(String[] args) {
		System.out.println("Command Line Parameters: [ConfigFilePath] [StoragePath] [OnlineMode] [Product] [Locale]");
		System.out.println("Parameters provided:");
		System.out.println("ConfigFilePath: " + args[0]);
		System.out.println("StoragePath: " + args[1]);
		System.out.println("OnlineMode: " + args[2]);
		System.out.println("Product: " + args[3]);
		System.out.println("Locale: " + args[4]);

		String path = args[0], storagePath = args[1], onlineMode = args[2], product = args[3], locale = args[4];

		InputStream is = null;
		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			File f = new File(path);

			// parse XML file

			// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
			// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
			// doc = dBuilder.parse(curFile);

			// WORKAROUND -> provide Inputstream
			is = new FileInputStream(f);
			doc = dBuilder.parse(is);
			is.close();

			// edit document
			NodeList nodeList = doc.getElementsByTagName("setting");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node curNode = nodeList.item(i);

				NamedNodeMap attributes = curNode.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attrNode = attributes.item(j);

					if (attrNode != null) {
						String val = attrNode.getNodeValue();
//						System.out.println("attr name: " + attrNode.getNodeName());
//						System.out.println("attr value: " + val);

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

				// Node attrNode = attributes.getNamedItem("name");

			}

			// write DOM back to XML
			Source source = new DOMSource(doc);
			Result result = new StreamResult(f);
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
				} catch (IOException e) {
				}
			}
		}
	}
	
	private static void replaceValueInSettingNode(Node settingNode, String newSettingVal){
		NodeList children = settingNode.getChildNodes();
//		System.out.println("child nodes count: "+children.getLength());
		x:for(int a=0; a<children.getLength(); a++){
			Node curChild = children.item(a);
//			System.out.println("child name: "+curChild.getNodeName());
//			System.out.println("child value: "+curChild.getNodeValue());
//			System.out.println("child text: "+curChild.getTextContent());
			if (curChild.getNodeName().equalsIgnoreCase("value")) {
				curChild.setTextContent(newSettingVal);
				break x;
			}
		}
		
		
	}

}
