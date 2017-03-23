package application.baseUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.xml.sax.SAXParseException;

import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;

@SuppressWarnings("restriction")
public class DefaultUICatalog {

	// Logger
	//private final static Logger LOGGER = Logger.getLogger(DefaultUICatalog.class.getName());
	private final static Logger LOGGER = LogManager.getLogger(DefaultUICatalog.class);
	
	// members
	ArrayList<DefaultUIElement> elements = new ArrayList<>();

	public void processFile(File f) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		InputStream is = null;

		Document doc = null;
		try {
			// parse XML file

			// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
			// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
			// doc = dBuilder.parse(curFile);

			// WORKAROUND -> provide Inputstream
			is = new FileInputStream(f);
			doc = dBuilder.parse(is);

			DefaultUIElement gameUI = new DefaultUIElement("GameUI", "GameUI");
			elements.add(gameUI);
			
			LOGGER.debug("retrieving Desc node");
			Node desc = doc.getChildNodes().item(0);
			
			if(doc.getChildNodes().getLength() > 1){
				LOGGER.error("Unhandled root nodes in file: " + f);
			}

			// parse all content like frames, etc...
			parse(desc.getChildNodes(), gameUI);

		} catch (SAXParseException | MalformedByteSequenceException e) {
			LOGGER.error("Failed to parse file: " + f, e);
			// couldn't parse, most likely no XML file
			// -> do nothing
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * 
	 * @param childNodes
	 * @param parent
	 */
	private void parse(NodeList childNodes, DefaultUIElement parent) {
		if (childNodes != null) {
			Node curNode = null, curAttribute = null;
			String name = "", type = "";
			boolean hasType = false, hasName = false, isFrame = false;
			DefaultUIElement elem = null;

			LOGGER.debug("checking childNodes, length: " + childNodes.getLength());
			// process all childNodes
			for (int i = 0; i < childNodes.getLength(); i++) {
				curNode = childNodes.item(i);

				short curNodeType = curNode.getNodeType();

				// skip boring elements
				if (curNodeType != Node.ELEMENT_NODE) {
					LOGGER.debug("Skipping node type " + curNodeType);
					continue;
				}

				name = "";
				type = "";
				hasType = false;
				hasName = false;
				isFrame = false;

				// process attributes
				NamedNodeMap attributes = curNode.getAttributes();
				if (attributes != null) {
					LOGGER.debug("checking attributes, length: " + attributes.getLength());

					for (int j = 0; j < attributes.getLength(); j++) {
						curAttribute = attributes.item(j);
						String attrName = curAttribute.getNodeName();
						if (attrName.equalsIgnoreCase("name")) {
							name = curAttribute.getNodeValue();
							hasName = true;
							LOGGER.debug("found attr name: " + name);
						} else if (attrName.equalsIgnoreCase("type")) {
							type = curAttribute.getNodeValue();
							hasType = true;
							LOGGER.debug("found attr type: " + type);
						} else {
							// TODO
							LOGGER.debug("found attribute: " + attrName);
						}
					}

					if (hasName) {
						if (hasType) {
							// Frame
							elem = new DefaultUIElement(name, type);
							parent.addChild(elem);
							LOGGER.debug("Frame, name: " + name + ", type: " + type);
							parse(curNode.getChildNodes(), elem);
							isFrame = true;
						} else {
							if (curNode.getNodeName().equalsIgnoreCase("Animation")) {
								// Animation
								elem = new DefaultUIElement(name, "Animation");
								parent.addChild(elem);
								LOGGER.debug("Animation, name: " + name);
							} else if (curNode.getNodeName().equalsIgnoreCase("Constant")) {
								// Constant
								elem = new DefaultUIElement(name, "Constant");
								parent.addChild(elem);
								LOGGER.debug("Constant, name: " + name);
							} else {
								// Other
								LOGGER.debug("other: " + curNode.getNodeName());
							}
						}
					} else {
						// frame properties
					}
				}

				// check child frames, if a frame was created
				if (elem != null && isFrame) {
					NodeList children = curNode.getChildNodes();
					if (children != null) {
						parse(children, elem);
					}
				}
			}
		}
	}

}
