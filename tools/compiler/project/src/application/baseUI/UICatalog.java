package application.baseUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

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

/**
 * 
 * @author Ahli
 *
 */
@SuppressWarnings("restriction")
public class UICatalog {

	// Logger
	private final static Logger LOGGER = LogManager.getLogger(UICatalog.class);

	// members
	ArrayList<UIElement> templates = new ArrayList<>();
	ArrayList<UIConstant> constants = new ArrayList<>();

	public void processFile(File f) throws SAXException, IOException, ParserConfigurationException, UIException {

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

			UIFrame gameUI = new UIFrame("GameUI", "GameUI");
			templates.add(gameUI);

			LOGGER.debug("retrieving document's children");
			NodeList nodes = doc.getChildNodes();

			if (nodes.getLength() <= 0) {
				LOGGER.warn("Empty layout file: " + f);
				return;
			} else {
				// parse document's content
				parse(nodes, gameUI);
				LOGGER.debug("Finished parsing file.");
			}
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
	 * @throws UIException
	 */
	private void parse(NodeList childNodes, UIElement parent) throws UIException {
		if (childNodes != null) {
			int len = childNodes.getLength();
			LOGGER.debug("checking childNodes, length: " + len);

			for (int i = 0; i < len; i++) {
				Node curNode = childNodes.item(i);

				if (isTrashNodeType(curNode)) {
					LOGGER.debug("Skipping node type " + curNode.getNodeType());
					continue;
				}

				parse(curNode, parent);
			}
		}
	}

	private boolean isTrashNodeType(Node node) {
		short nodeType = node.getNodeType();
		return nodeType != Node.ELEMENT_NODE;
	}

	/**
	 * 
	 * @param childNode
	 * @param parent
	 * @throws UIException
	 */
	private void parse(Node node, UIElement parent) throws UIException {

		String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
		LOGGER.debug("node name: " + nodeName);

		// use lowercase for cases!
		switch (nodeName) {
		case "frame":
			parseFrame(node, parent);
			break;
		case "anchor":
			parseAnchor(node, parent);
			break;
		case "state":
			parseState(node, parent);
			break;
		case "controller":
			parseController(node, parent);
			break;
		case "animation":
			parseAnimation(node, parent);
			break;
		case "stategroup":
			parseStateGroup(node, parent);
			break;
		case "constant":
			parseConstant(node, parent);
			break;
		case "desc":
			parseDesc(node, parent);
			break;
		default:
			// attribute
			parseAttribute(node, parent);
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseController(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Controller");
		// Controllers may not have a name defined
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		String name = null;
		boolean nameIsImplicit = true;

		if (nameAttrNode != null) {
			name = nameAttrNode.getNodeValue();
			nameIsImplicit = false;
		}
		if (name == null) {
			LOGGER.debug("name is null");
		} else {
			LOGGER.debug("name = " + name);
		}
		UIController thisElem = new UIController(name);
		thisElem.setNameIsImplicit(nameIsImplicit);

		// parse other settings in that line
		NamedNodeMap attributes = node.getAttributes();
		LOGGER.debug("attribute length = " + attributes.getLength());
		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttribute = attributes.item(i);
			if(nameAttrNode == curAttribute){
				// ignore name attribute
				continue;
			} else{
				String attrKey = curAttribute.getNodeName();
				String attrVal = curAttribute.getNodeValue();
				LOGGER.debug("key,value = '" + attrKey + "', '" + attrVal + "'");
				String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
				if (overriddenVal != null) {
					LOGGER.debug("overridden value = " + overriddenVal);
				}
			}
		}

		// register with Animation/templates
		if (parent == null) {
			templates.add(thisElem);
		} else if (parent instanceof UIAnimation) {
			UIAnimation p = (UIAnimation) parent;
			p.getControllers().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Controller to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseState(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing State");
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		if (nameAttrNode == null) {
			throw new UIException("State has no specified 'name'");
		}
		String name = nameAttrNode.getNodeValue();
		LOGGER.debug("name = " + name);
		UIState thisElem = new UIState(name);

		// register with Animation/templates
		if (parent == null) {
			templates.add(thisElem);
		} else if (parent instanceof UIStateGroup) {
			UIStateGroup p = (UIStateGroup) parent;
			p.getStates().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a State to be defined here.");
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAnchor(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Anchor");
		if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			
			NamedNodeMap attributes = node.getAttributes();
			LOGGER.debug("attribute length = " + attributes.getLength());

			Node nodeSide = attributes.getNamedItem("side");
			Node nodeRelative = attributes.getNamedItem("relative");
			Node nodePos = attributes.getNamedItem("pos");
			Node nodeOffset = attributes.getNamedItem("offset");
			
			
			
			// TODO
			
		} else {
			throw new UIException("Anchor attributes are not supported here.");
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAttribute(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Attribute");
		String id = node.getNodeName();
		UIAttribute thisElem = new UIAttribute(id);
		LOGGER.debug("id = " + id);

		NamedNodeMap attributes = node.getAttributes();
		LOGGER.debug("attribute length = " + attributes.getLength());

		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttribute = attributes.item(i);
			String attrKey = curAttribute.getNodeName();
			String attrVal = curAttribute.getNodeValue();
			LOGGER.debug("key,value = '" + attrKey + "', '" + attrVal + "'");
			String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
			if (overriddenVal != null) {
				LOGGER.debug("overridden value = " + overriddenVal);
			}

		}

		// register with frame/animation/stategroup/state/controller
		if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getAttributes().put(id, thisElem);
		} else if (parent instanceof UIAnimation) {
			if (thisElem.getName().compareToIgnoreCase("event") != 0) {
				throw new UIException(
						"Parent element (Frame) expects 'Event' attribute instead of '" + thisElem.getName() + "'");
			}
			UIAnimation p = (UIAnimation) parent;
			p.getEvents().put(id, thisElem);
		} else if (parent instanceof UIStateGroup) {
			if (thisElem.getName().compareToIgnoreCase("defaultstate") != 0) {
				throw new UIException("Parent element (Animation) expects 'DefaultState' attribute instead of '"
						+ thisElem.getName() + "'");
			}
			UIStateGroup p = (UIStateGroup) parent;
			String val = thisElem.getValues().get("val");
			if (val != null) {
				p.setDefaultState(val);
			} else {
				throw new UIException(
						"Parent element (StateGroup) expects this 'DefaultState' attribute to have a 'val' entry.");
			}
		} else if (parent instanceof UIState) {
			UIState p = null;
			switch (thisElem.getName().toLowerCase(Locale.ROOT)) {
			case "when":
				p = (UIState) parent;
				p.getWhens().add(thisElem);
				break;
			case "action":
				p = (UIState) parent;
				p.getActions().add(thisElem);
				break;
			default:
				throw new UIException("Parent element (UIState) expects 'when' or 'action' attribute instead of '"
						+ thisElem.getName() + "'");
			}
		} else if (parent instanceof UIController) {
			if (thisElem.getName().compareToIgnoreCase("key") != 0) {
				throw new UIException("Parent element (UIController) expects 'key' attribute instead of '"
						+ thisElem.getName() + "'");
			}
			UIController p = (UIController) parent;
			p.getKeys().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow an Attribute to be defined here.");
		}

		// verify that it cannot go deeper
		parseAttrChildren(node.getChildNodes(), thisElem);
	}

	/**
	 * 
	 * @param childNodes
	 * @param thisElem
	 * @throws UIException
	 */
	private void parseAttrChildren(NodeList childNodes, UIAttribute parent) throws UIException {
		// no non-trash children allowed
		int len = childNodes.getLength();
		for (int i = 0; i < len; i++) {
			Node node = childNodes.item(i);
			if (isTrashNodeType(node)) {
				String msg = "Found trash nodeType within Attribute '" + parent.getName() + "', nodeType = '"
						+ node.getNodeType() + "'";
				LOGGER.debug(msg);
				throw new UIException(msg);
			}
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseDesc(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Desc");

		// go deeper
		parse(node.getChildNodes(), null);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseStateGroup(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Stategroup");
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		if (nameAttrNode == null) {
			throw new UIException("Stategroup has no specified 'name'");
		}
		String name = nameAttrNode.getNodeValue();
		LOGGER.debug("name = " + name);
		UIStateGroup thisElem = new UIStateGroup(name);

		// register with parent/templates
		if (parent == null) {
			templates.add(thisElem);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.addChild(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Stategroup to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAnimation(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Animation");
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		if (nameAttrNode == null) {
			throw new UIException("Animation has no specified 'name'");
		}
		String name = nameAttrNode.getNodeValue();
		LOGGER.debug("name = " + name);
		UIAnimation thisElem = new UIAnimation(name);

		// register with parent/templates
		if (parent == null) {
			templates.add(thisElem);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.addChild(thisElem);
		} else {
			throw new UIException("Parent element does not allow an Animation to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem);

		// set implicit controller names
		if (parent != null) {
			setImplicitControllerNames(thisElem);
		} else {
			// cannot set names for templates as they could be incomplete,
			// right?
		}
	}

	/**
	 * Set the implicit names of controllers in animations
	 * 
	 * @param thisElem
	 * @throws UIException
	 */
	private void setImplicitControllerNames(UIAnimation thisElem) throws UIException {
		LOGGER.debug("Setting implicit controller names");
		ArrayList<UIController> controllers = thisElem.getControllers();
		for (UIController contr : controllers) {
			if (contr.getName() == null) {
				String type = contr.getValues().get("type");
				contr.setName(getImplicitName(type, controllers));
				contr.setNameIsImplicit(true);
			}
		}
	}

	/**
	 * 
	 * @param type
	 * @param controllers
	 * @return
	 * @throws UIException
	 */
	private String getImplicitName(String type, ArrayList<UIController> controllers) throws UIException {
		LOGGER.debug("Constructing implicit controller name");
		if (type == null) {
			throw new UIException("'type=\"...\"' of Controller is not set or invalid.");
		}

		int i = 0;
		while (true) {
			String name = type + "_" + i;

			if (controllers.stream().noneMatch(t -> t.getName() != null && t.getName().compareToIgnoreCase(name) != 0)) {
				LOGGER.debug("Constructing implicit controller name: " + name);
				return name;
			}

			i++;
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseConstant(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Constant");
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		if (nameAttrNode == null) {
			throw new UIException("Constant has no specified 'name'");
		}
		String name = nameAttrNode.getNodeValue();
		LOGGER.debug("name = " + name);
		UIConstant thisElem = new UIConstant(name);

		Node valAttrNode = node.getAttributes().getNamedItem("val");
		if (valAttrNode == null) {
			throw new UIException("Constant has no specified 'val'");
		}
		String val = valAttrNode.getNodeValue();
		LOGGER.debug("val = " + val);
		thisElem.setValue(val);

		// register with parent/templates
		constants.add(thisElem);

		// go deeper
		parse(node.getChildNodes(), thisElem);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseFrame(Node node, UIElement parent) throws UIException {
		LOGGER.debug("parsing Frame");
		Node nameAttrNode = node.getAttributes().getNamedItem("name");
		if (nameAttrNode == null) {
			throw new UIException("Frame has no specified 'name'");
		}
		String name = nameAttrNode.getNodeValue();
		LOGGER.debug("name = " + name);

		Node typeAttrNode = node.getAttributes().getNamedItem("type");
		if (typeAttrNode == null) {
			throw new UIException("Constant has no specified 'val'");
		}
		String type = typeAttrNode.getNodeValue();
		LOGGER.debug("type = " + type);

		UIFrame thisElem = new UIFrame(name, type);

		// register with parent/templates
		if (parent == null) {
			templates.add(thisElem);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.addChild(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Frame to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem);
	}

}
