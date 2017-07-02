package com.ahli.galaxy.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
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

import com.ahli.galaxy.DescIndexReader;


/**
 * 
 * @author Ahli
 *
 */
public class UICatalog {
	private final static Logger LOGGER = LogManager.getLogger(UICatalog.class);

	// members
	ArrayList<UITemplate> templates = new ArrayList<>();
	ArrayList<UIConstant> constants = new ArrayList<>();

	/**
	 * 
	 * @param f
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 */
	public void processDescIndex(File f) throws SAXException, IOException, ParserConfigurationException, UIException {
		ArrayList<String> layouts = DescIndexReader.getLayoutPathList(f, true);
		String descIndexPath = f.getAbsolutePath();
		String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());

		LOGGER.debug("descIndexPath=" + descIndexPath);
		LOGGER.debug("basePath=" + basePath);
		for (String intPath : layouts) {
			LOGGER.debug("intPath=" + intPath);
			String basePathTemp = basePath;
			while (!new File(basePathTemp + File.separator + intPath).exists()) {
				int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					basePathTemp = basePathTemp.substring(0, basePathTemp.lastIndexOf(File.separatorChar));
					LOGGER.debug("basePathTemp=" + basePathTemp);
				} else {
					throw new UIException("Cannot find file " + intPath);
				}
			}

			File layoutFile = new File(basePathTemp + File.separator + intPath);
			processLayoutFile(layoutFile);
		}
	}

	/**
	 * 
	 * @param f
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 */
	public void processLayoutFile(File f) throws SAXException, IOException, ParserConfigurationException, UIException {
		LOGGER.info("Processing layout file " + f.getAbsolutePath());

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

			LOGGER.debug("retrieving document's children");
			NodeList nodes = doc.getChildNodes();

			if (nodes.getLength() <= 0) {
				LOGGER.warn("Empty layout file: " + f);
				return;
			} else {
				// parse document's content
				parse(nodes, null, f.getName().substring(0, f.getName().lastIndexOf('.')));
				LOGGER.debug("Finished parsing file.");
			}
		} catch (SAXParseException | IOException e) {
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
	private void parse(NodeList childNodes, UIElement parent, String fileName) throws UIException {
		if (childNodes != null) {
			int len = childNodes.getLength();
			LOGGER.debug("checking childNodes, length: " + len);

			for (int i = 0; i < len; i++) {
				Node curNode = childNodes.item(i);

				if (isTrashNodeType(curNode)) {
					LOGGER.debug("Skipping node type " + curNode.getNodeType());
					continue;
				}

				parse(curNode, parent, fileName);
			}
		}
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private boolean isTrashNodeType(Node node) {
		short nodeType = node.getNodeType();
		// only Node.ELEMENT_NODE is good to use and not trash
		return nodeType != Node.ELEMENT_NODE;
	}

	/**
	 * 
	 * @param childNode
	 * @param parent
	 * @throws UIException
	 */
	private void parse(Node node, UIElement parent, String fileName) throws UIException {
		String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
		LOGGER.debug("node name: " + nodeName);

		// do not load, if requiredtoload
		if (getNamedItemIgnoreCase(node.getAttributes(), "requiredtoload") != null) {
			LOGGER.debug("Encountered 'requiredToLoad=' attribute.");
			return;
		}

		// use lowercase for cases!
		switch (nodeName) {
		case "frame":
			parseFrame(node, parent, fileName);
			break;
		case "anchor":
			parseAnchor(node, parent);
			break;
		case "state":
			parseState(node, parent, fileName);
			break;
		case "controller":
			parseController(node, parent, fileName);
			break;
		case "animation":
			parseAnimation(node, parent, fileName);
			break;
		case "stategroup":
			parseStateGroup(node, parent, fileName);
			break;
		case "constant":
			parseConstant(node, parent, fileName);
			break;
		case "desc":
			parseDesc(node, parent, fileName);
			break;
		case "descflags":
			parseDescFlags(node, parent, fileName);
			break;
		default:
			// attribute
			parseAttribute(node, parent, fileName);
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @param fileName
	 * @throws UIException
	 */
	private void parseDescFlags(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing DescFlags");
		if (parent == null) {
			Node val = getNamedItemIgnoreCase(node.getAttributes(), "val");
			if (getConstantValue(val.getNodeValue()).compareToIgnoreCase("locked") == 0) {
				for (UITemplate template : templates) {
					if (template.getFileName().compareToIgnoreCase(fileName) == 0) {
						template.setLocked(true);
						LOGGER.debug("locked templated " + fileName + "/" + template.getElement().getName());
					}
				}
			}
		} else {
			LOGGER.debug("DescFlags found with parent. -> ignored");
		}

		// verify that it cannot go deeper
		parseAttrChildren(node.getChildNodes(), "'DescFlags'");
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseController(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Controller");
		// Controllers may not have a name defined
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		String name = null;
		boolean nameIsImplicit = true;

		if (nameAttrNode != null) {
			name = getConstantValue(nameAttrNode.getNodeValue());
			nameIsImplicit = false;
		}
		if (name == null) {
			LOGGER.debug("name is null");
		} else {
			LOGGER.debug("name = " + name);
		}
		String template = readTemplate(node.getAttributes());
		UIController thisElem = template == null ? new UIController(name)
				: (UIController) instanciateTemplate(template, name);
		thisElem.setNameIsImplicit(nameIsImplicit);

		// parse other settings in that line
		NamedNodeMap attributes = node.getAttributes();
		LOGGER.debug("attribute length = " + attributes.getLength());
		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttribute = attributes.item(i);
			if (nameAttrNode == curAttribute) {
				// ignore name attribute
				continue;
			} else {
				String attrKey = curAttribute.getNodeName();
				String attrVal = getConstantValue(curAttribute.getNodeValue());
				LOGGER.debug("key,value = '" + attrKey + "', '" + attrVal + "'");
				String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
				if (overriddenVal != null) {
					LOGGER.debug("overridden value = " + overriddenVal);
				}
			}
		}

		// register with Animation/templates
		if (parent == null) {
			templates.add(new UITemplate(fileName, thisElem));
		} else if (parent instanceof UIAnimation) {
			UIAnimation p = (UIAnimation) parent;
			p.getControllers().add(thisElem);
		} else {
			// TODO disabled exception
			//throw new UIException("Parent element does not allow a Controller to be defined here.");
			LOGGER.error("Parent element does not allow a Controller to be defined here.");
			thisElem.setNextAdditionShouldOverride(true);
			return;
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);

		// after parsing children
		thisElem.setNextAdditionShouldOverride(true);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseState(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing State");
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("State has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue());
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIState thisElem = template == null ? new UIState(name) : (UIState) instanciateTemplate(template, name);

		// register with Animation/templates
		if (parent == null) {
			templates.add(new UITemplate(fileName, thisElem));
		} else if (parent instanceof UIStateGroup) {
			UIStateGroup p = (UIStateGroup) parent;
			p.getStates().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a State to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);

		// after parsing children
		thisElem.setNextAdditionShouldOverrideWhens(true);
		thisElem.setNextAdditionShouldOverrideActions(true);
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
			UIFrame frame = (UIFrame) parent;

			NamedNodeMap attributes = node.getAttributes();
			LOGGER.debug("attribute length = " + attributes.getLength());

			Node nodeSide = getNamedItemIgnoreCase(attributes, "side");
			Node nodeRelative = getNamedItemIgnoreCase(attributes, "relative");
			Node nodePos = getNamedItemIgnoreCase(attributes, "pos");
			Node nodeOffset = getNamedItemIgnoreCase(attributes, "offset");

			if (nodeRelative == null) {
				throw new UIException("'Anchor' attribute does not contain required attribute 'relative='");
			}
			String relative = getConstantValue(nodeRelative.getNodeValue());
			String offset = nodeOffset == null ? null : getConstantValue(nodeOffset.getNodeValue());

			if (nodeSide != null) {
				// version with side
				if (nodePos == null) {
					throw new UIException("'Anchor' attribute contains 'side=', but not 'pos='");
				}
				if (nodeOffset == null) {
					throw new UIException("'Anchor' attribute contains 'side=', but not 'offset='");
				}
				String pos = getConstantValue(nodePos.getNodeValue());
				String side = getConstantValue(nodeSide.getNodeValue());
				UIAnchorSide sideVal = null;
				if (side.compareToIgnoreCase("left") == 0) {
					sideVal = UIAnchorSide.Left;
				} else if (side.compareToIgnoreCase("bottom") == 0) {
					sideVal = UIAnchorSide.Bottom;
				} else if (side.compareToIgnoreCase("right") == 0) {
					sideVal = UIAnchorSide.Right;
				} else if (side.compareToIgnoreCase("top") == 0) {
					sideVal = UIAnchorSide.Top;
				} else {
					throw new UIException(
							"'Anchor' attribute has unrecognizable value for 'side='. Value is '" + side + "'");
				}
				frame.setAnchorPos(sideVal, pos);
				frame.setAnchorRelative(sideVal, relative);
				frame.setAnchorOffset(sideVal, offset);
			} else {
				// version without side
				if (offset == null) {
					// relative information alone is enough
					offset = "0";
				}
				try {
					frame.setAnchor(relative, offset);
				} catch (NumberFormatException e) {
					throw new UIException("Could not parse an 'Anchor' attribute's 'offset=' value as a number.");
				}
			}

			// verify that it cannot go deeper
			parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " + parent.getName() + "'");

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
	private void parseAttribute(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Attribute");
		String id = node.getNodeName();
		UIAttribute thisElem = new UIAttribute(id);
		LOGGER.debug("id = " + id);

		NamedNodeMap attributes = node.getAttributes();
		LOGGER.debug("attribute length = " + attributes.getLength());

		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttribute = attributes.item(i);
			String attrKey = curAttribute.getNodeName();
			String attrVal = getConstantValue(curAttribute.getNodeValue());
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
			if (id.equalsIgnoreCase("event")) {
				UIAnimation p = (UIAnimation) parent;
				// override all animation events
				if (p.isNextEventsAdditionShouldOverride()) {
					p.getEvents().clear();
					p.setNextEventsAdditionShouldOverride(false);
				}
				p.getEvents().put(id, thisElem);
			} else if(id.equalsIgnoreCase("driver")){
				UIAnimation p = (UIAnimation) parent;
				p.setDriver(thisElem);
			}else{
				throw new UIException("Parent element (Frame) expects 'Event' attribute instead of '" + id + "'");
			}
		} else if (parent instanceof UIStateGroup) {
			if (id.compareToIgnoreCase("defaultstate") != 0) {
				throw new UIException(
						"Parent element (Animation) expects 'DefaultState' attribute instead of '" + id + "'");
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
			switch (id.toLowerCase(Locale.ROOT)) {
			case "when":
				p = (UIState) parent;
				// override all state whens
				if (p.isNextAdditionShouldOverrideWhens()) {
					p.getWhens().clear();
					p.setNextAdditionShouldOverrideWhens(false);
				}
				p.getWhens().add(thisElem);
				break;
			case "action":
				p = (UIState) parent;
				// override all state actions
				if (p.isNextAdditionShouldOverrideActions()) {
					p.getWhens().clear();
					p.setNextAdditionShouldOverrideActions(false);
				}
				p.getActions().add(thisElem);
				break;
			default:
				throw new UIException(
						"Parent element (UIState) expects 'when' or 'action' attribute instead of '" + id + "'");
			}
		} else if (parent instanceof UIController) {
			if (id.compareToIgnoreCase("key") != 0) {
				throw new UIException("Parent element (UIController) expects 'key' attribute instead of '" + id + "'");
			}
			UIController p = (UIController) parent;
			// override all controller keys
			if (p.isNextAdditionShouldOverride()) {
				p.getKeys().clear();
				p.setNextAdditionShouldOverride(false);
			}
			p.getKeys().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow an Attribute to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);
	}

	/**
	 * 
	 * @param childNodes
	 * @param thisElem
	 * @throws UIException
	 */
	private void parseAttrChildren(NodeList childNodes, String parentName) throws UIException {
		// no non-trash children allowed
		int len = childNodes.getLength();
		for (int i = 0; i < len; i++) {
			Node node = childNodes.item(i);
			if (isTrashNodeType(node)) {
				String msg = "Found trash nodeType within Attribute '" + parentName + "', nodeType = '"
						+ node.getNodeType() + "'";
				LOGGER.debug(msg);
				throw new UIException(msg);
			}

			// verify that it cannot go deeper
			parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " + node.getNodeName() + "'");
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseDesc(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Desc");

		// go deeper
		parse(node.getChildNodes(), null, fileName);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseStateGroup(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Stategroup");
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Stategroup has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue());
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIStateGroup thisElem = template == null ? new UIStateGroup(name)
				: (UIStateGroup) instanciateTemplate(template, name);

		// register with parent/templates
		if (parent == null) {
			templates.add(new UITemplate(fileName, thisElem));
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Stategroup to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAnimation(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Animation");
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Animation has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue());
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIAnimation thisElem = template == null ? new UIAnimation(name)
				: (UIAnimation) instanciateTemplate(template, name);

		// register with parent/templates
		if (parent == null) {
			templates.add(new UITemplate(fileName, thisElem));
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow an Animation to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);

		// after parsing children

		// set implicit controller names
		if (parent != null) {
			setImplicitControllerNames(thisElem);
		} else {
			// cannot set names for templates as they could be incomplete,
			// right?
		}

		thisElem.setNextEventsAdditionShouldOverride(true);
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
				LOGGER.debug("type = " + type);
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

			if (controllers.stream()
					.noneMatch(t -> t.getName() != null && t.getName().compareToIgnoreCase(name) == 0)) {
				LOGGER.debug("Constructing implicit controller name: " + name);
				return name;
			}
			LOGGER.debug("Implicit controller name existing: " + name);
			i++;
		}
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseConstant(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Constant");
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Constant has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue());
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIConstant thisElem = template == null ? new UIConstant(name)
				: (UIConstant) instanciateTemplate(template, name);

		Node valAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "val");
		if (valAttrNode == null) {
			throw new UIException("Constant has no specified 'val'");
		}
		String val = getConstantValue(valAttrNode.getNodeValue());
		LOGGER.debug("val = " + val);
		thisElem.setValue(val);

		// register with parent/templates overriding previous constant values
		for (int i = constants.size() - 1; i >= 0; i--) {
			UIConstant curConst = constants.get(i);
			if (curConst.getName().compareToIgnoreCase(name) == 0) {
				constants.remove(i);
			}
		}
		constants.add(thisElem);

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);
	}

	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseFrame(Node node, UIElement parent, String fileName) throws UIException {
		LOGGER.debug("parsing Frame");
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Frame has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue());
		LOGGER.debug("name = " + name);

		Node typeAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "type");
		if (typeAttrNode == null) {
			throw new UIException("Frame has no specified 'type'");
		}
		String type = getConstantValue(typeAttrNode.getNodeValue());
		LOGGER.debug("type = " + type);

		String template = readTemplate(node.getAttributes());
		UIFrame thisElem = template == null ? new UIFrame(name, type) : (UIFrame) instanciateTemplate(template, name);

		// register with parent/templates
		if (parent == null) {
			LOGGER.debug("Adding new template Frame named " + fileName + "/" + thisElem.getName());
			templates.add(new UITemplate(fileName, thisElem));
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Frame to be defined here.");
		}

		// go deeper
		parse(node.getChildNodes(), thisElem, fileName);
	}

	/**
	 * 
	 * @param template
	 * @return
	 * @throws UIException
	 */
	private UIElement instanciateTemplate(String path, String newName) throws UIException {
		LOGGER.debug("Instanciating Template of path " + path);
		path = path.replace('\\', '/');
		String fileName = path.substring(0, path.indexOf("/"));
		for (UITemplate curTemplate : templates) {
			if (curTemplate.getFileName().equalsIgnoreCase(fileName)) {
				// found template file
				String newPath = UIElement.removeLeftPathLevel(path);
				UIElement frameFromPath = curTemplate.receiveFrameFromPath(newPath);

				if (frameFromPath == null) {
					// not the correct template
					continue;
				}
				UIElement clone = (UIElement) frameFromPath.deepClone();
				clone.setName(newName);
				return clone;
			}
		}

		String msg = "Template of path '" + path + "' could not be found";
		LOGGER.error(msg);
		// throw new UIException(msg);
		// TODO handle this case better... heroes default UI uses template of
		// not-loaded frame
		return new UIFrame(newName, "Frame");
	}

	/**
	 * 
	 * @param attributes
	 * @return template reference or null
	 */
	private String readTemplate(NamedNodeMap attributes) {
		Node template = attributes.getNamedItem("template");
		return template != null ? template.getNodeValue() : null;
	}

	/**
	 * 
	 * @param constantID
	 * @return
	 */
	public String getConstantValue(String constantID) {
		LOGGER.debug("Checking constant value for constantID='" + constantID + "'");
		String id = constantID;
		int i = 0;
		while (id.startsWith("#")) {
			i++;
			id = id.substring(1);
		}
		// no constant tag
		if (i <= 0) {
			return constantID;
		}
		String prefix = constantID.substring(0, i);
		String constantName = constantID.substring(i);
		LOGGER.debug("prefix='" + prefix + "', constantName='" + constantName + "'");
		for (UIConstant c : constants) {
			if (c.getName().equalsIgnoreCase(constantName)) {
				return c.getValue();
			}
		}
		LOGGER.info("Did not find a constant definition. ID is used instead. id = " + constantID);
		return prefix + constantName;
	}

	/**
	 * 
	 * @param nodes
	 * @param name
	 * @return
	 */
	private Node getNamedItemIgnoreCase(NamedNodeMap nodes, String name) {
		Node node = nodes.getNamedItem(name);
		if (node == null) {
			for (int i = 0, len = nodes.getLength(); i < len; i++) {
				Node curNode = nodes.item(i);
				if (name.equalsIgnoreCase(curNode.getNodeName())) {
					return curNode;
				}
			}
		}
		return node;
	}
}
