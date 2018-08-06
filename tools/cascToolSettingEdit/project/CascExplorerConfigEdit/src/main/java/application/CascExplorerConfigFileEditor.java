package application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class CascExplorerConfigFileEditor {
	private static final Logger logger = LogManager.getLogger();
	
	private CascExplorerConfigFileEditor() {
	}
	
	/**
	 * Edits the specified config file and replaces the specified values.
	 *
	 * @param f
	 * @param storagePath
	 * @param onlineMode
	 * @param product
	 * @param locale
	 */
	public static void write(final File f, final String storagePath, final String onlineMode, final String product,
			final String locale) {
		try (final InputStream is = Files.newInputStream(f.toPath())) {
			final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = dBuilder.parse(is);
			
			// edit document
			final NodeList nodeList = doc.getElementsByTagName("setting");
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node curNode = nodeList.item(i);
				
				final NamedNodeMap attributes = curNode.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					final Node attrNode = attributes.item(j);
					
					if (attrNode != null) {
						final String val = attrNode.getNodeValue();
						
						if ("StoragePath".equalsIgnoreCase(val)) {
							replaceValueInSettingNode(curNode, storagePath);
						} else if ("OnlineMode".equalsIgnoreCase(val)) {
							replaceValueInSettingNode(curNode, onlineMode);
						} else if ("Product".equalsIgnoreCase(val)) {
							replaceValueInSettingNode(curNode, product);
						} else if ("Locale".equalsIgnoreCase(val)) {
							replaceValueInSettingNode(curNode, locale);
						}
					}
				}
			}
			
			// write DOM back to XML
			final Source source = new DOMSource(doc);
			final Result result = new StreamResult(f);
			
			final TransformerFactory factory = TransformerFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			
		} catch (final IOException | ParserConfigurationException | SAXException | TransformerFactoryConfigurationError | TransformerException e1) {
			logger.error("Error editing CascExplroer's configuration file.", e1);
		}
		
	}
	
	/**
	 * @param settingNode
	 * @param newSettingVal
	 */
	private static void replaceValueInSettingNode(final Node settingNode, final String newSettingVal) {
		final NodeList children = settingNode.getChildNodes();
		for (int a = 0; a < children.getLength(); a++) {
			final Node curChild = children.item(a);
			if (curChild.getNodeName().equalsIgnoreCase("value")) {
				curChild.setTextContent(newSettingVal);
				break;
			}
		}
	}
	
}
