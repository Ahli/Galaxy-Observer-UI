package com.ahli.galaxy.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.util.XmlDomHelper;

/**
 * Represents a container for UI frame.
 * 
 * @author Ahli
 * 
 */
public class UICatalog {
	private final static Logger LOGGER = LogManager.getLogger(UICatalog.class);
	
	// members
	ArrayList<UITemplate> templates = new ArrayList<>();
	ArrayList<UITemplate> blizzDevTemplates = new ArrayList<>();
	ArrayList<UIConstant> constants = new ArrayList<>();
	ArrayList<UIConstant> blizzOnlyConstants = new ArrayList<>();
	ArrayList<String> devLayouts = new ArrayList<>();
	
	String curBasePath = "";
	
	/**
	 * 
	 * @param f
	 *            descIndex file to process
	 * @param raceId
	 *            to use to check constants starting with ##
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 */
	public void processDescIndex(File f, String raceId)
			throws SAXException, IOException, ParserConfigurationException, UIException {
		
		ArrayList<String> generalLayouts = DescIndexReader.getLayoutPathList(f, true);
		
		ArrayList<String> combinedList = new ArrayList<>(DescIndexReader.getLayoutPathList(f, false));
		
		devLayouts.addAll(combinedList);
		devLayouts.removeAll(generalLayouts);
		
		String descIndexPath = f.getAbsolutePath();
		String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());
		LOGGER.debug("descIndexPath=" + descIndexPath);
		LOGGER.debug("basePath=" + basePath);
		
		processLayouts(combinedList, basePath, raceId);
	}
	
	/**
	 * 
	 * @param toProcessList
	 * @param basePath
	 */
	private void processLayouts(ArrayList<String> toProcessList, String basePath, String raceId) {
		loop: for (String intPath : toProcessList) {
			boolean isDevLayout = devLayouts.contains(intPath);
			LOGGER.debug("intPath=" + intPath);
			LOGGER.debug("isDevLayout=" + isDevLayout);
			String basePathTemp = "" + basePath;
			while (!new File(basePathTemp + File.separator + intPath).exists()) {
				int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					basePathTemp = basePathTemp.substring(0, basePathTemp.lastIndexOf(File.separatorChar));
					LOGGER.debug("basePathTemp=" + basePathTemp);
				} else {
					if (!isDevLayout) {
						LOGGER.error("ERROR: Cannot find layout file: " + intPath);
					} else {
						LOGGER.warn("WARNING: Cannot find Blizz-only layout file: " + intPath + ", so this is fine.");
					}
					continue loop;
				}
			}
			curBasePath = basePathTemp;
			File layoutFile = new File(basePathTemp + File.separator + intPath);
			try {
				processLayoutFile(layoutFile, raceId, isDevLayout);
			} catch (SAXException | IOException | ParserConfigurationException | UIException e) {
				LOGGER.error("ERROR: encountered an Exception while processing the layout file '" + layoutFile + "'.",
						e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param f
	 *            layout file to process
	 * @param raceId
	 *            to use to check constants starting with ##
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 */
	public void processLayoutFile(File f, String raceId, boolean isDevLayout)
			throws SAXException, IOException, ParserConfigurationException, UIException {
		if (!isDevLayout) {
			LOGGER.info("Processing layout file " + f.getAbsolutePath());
		} else {
			LOGGER.info("Processing Blizz-only layout file " + f.getAbsolutePath());
		}
		
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
				parse(nodes, null, f.getName().substring(0, f.getName().lastIndexOf('.')), raceId, isDevLayout);
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
	 * @param nodeValue
	 * @param raceId
	 */
	private void processLayoutFile(String pathAttributeValue, String raceId, boolean isDevLayout) {
		LOGGER.trace("processing layoutFile from include: " + pathAttributeValue);
		if (isDevLayout) {
			devLayouts.add(pathAttributeValue);
		}
		String basePath = curBasePath;
		ArrayList<String> list = new ArrayList<>();
		list.add(pathAttributeValue);
		processLayouts(list, basePath, raceId);
	}
	
	/**
	 * 
	 * @param childNodes
	 * @param parent
	 * @param isDevLayout
	 * @throws UIException
	 */
	private void parse(NodeList childNodes, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		if (childNodes != null) {
			int len = childNodes.getLength();
			LOGGER.debug("checking childNodes, length: " + len);
			
			for (int i = 0; i < len; i++) {
				Node curNode = childNodes.item(i);
				
				if (isTrashNodeType(curNode)) {
					LOGGER.debug("Skipping node type " + curNode.getNodeType());
					continue;
				}
				
				parse(curNode, parent, fileName, raceId, isDevLayout);
			}
		}
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private boolean isTrashNodeType(Node node) {
		// only Node.ELEMENT_NODE is good to use and not trash
		return node.getNodeType() != Node.ELEMENT_NODE;
	}
	
	/**
	 * 
	 * @param childNode
	 * @param parent
	 * @throws UIException
	 */
	private void parse(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
		LOGGER.debug("node name: " + nodeName);
		
		// do not load, if requiredtoload
		if (XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "requiredtoload") != null) {
			if (nodeName.equals("include")) {
				parseInclude(node, parent, fileName, raceId, isDevLayout, true);
			} else if (!isDevLayout) {
				LOGGER.warn("WARNING: Encountered 'requiredToLoad' attribute in non-Blizz-only layout. ElementName: "
						+ nodeName + ", parent: " + parent);
			}
			return;
		}
		
		// use lowercase for cases!
		switch (nodeName) {
			case "frame":
				parseFrame(node, parent, fileName, raceId, isDevLayout);
				break;
			case "anchor":
				parseAnchor(node, parent, raceId, isDevLayout);
				break;
			case "state":
				parseState(node, parent, fileName, raceId, isDevLayout);
				break;
			case "controller":
				parseController(node, parent, fileName, raceId, isDevLayout);
				break;
			case "animation":
				parseAnimation(node, parent, fileName, raceId, isDevLayout);
				break;
			case "stategroup":
				parseStateGroup(node, parent, fileName, raceId, isDevLayout);
				break;
			case "constant":
				parseConstant(node, parent, fileName, raceId, isDevLayout);
				break;
			case "desc":
				parseDesc(node, parent, fileName, raceId, isDevLayout);
				break;
			case "descflags":
				parseDescFlags(node, parent, fileName, raceId, isDevLayout);
				break;
			case "include":
				parseInclude(node, parent, fileName, raceId, isDevLayout, false);
				break;
			default:
				// attribute or something unknown that will cause an error
				parseAttribute(node, parent, fileName, raceId, isDevLayout);
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @param fileName
	 * @param raceId
	 * @param isDevLayout
	 */
	private void parseInclude(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout,
			boolean hasReqToLoadAttribute) {
		NamedNodeMap attributes = node.getAttributes();
		Node pathAttribute = XmlDomHelper.getNamedItemIgnoringCase(attributes, "path");
		if (pathAttribute != null) {
			boolean thisOneIsDevLayoutToo = (isDevLayout || hasReqToLoadAttribute);
			processLayoutFile(pathAttribute.getNodeValue(), raceId, thisOneIsDevLayoutToo);
		} else {
			if (!isDevLayout) {
				LOGGER.error("ERROR: encountered 'Include' without required 'path' attribute.");
			} else {
				LOGGER.error(
						"WARNING: encountered 'Include' without required 'path' attribute in a blizz-only layout, so this is fine.");
			}
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @param fileName
	 * @throws UIException
	 */
	private void parseDescFlags(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing DescFlags");
		if (parent == null) {
			Node val = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "val");
			if (getConstantValue(val.getNodeValue(), raceId, isDevLayout).compareToIgnoreCase("locked") == 0) {
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
		parseAttrChildren(node.getChildNodes(), "'DescFlags'", isDevLayout);
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseController(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Controller");
		// Controllers may not have a name defined
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		String name = null;
		boolean nameIsImplicit = true;
		
		if (nameAttrNode != null) {
			name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
			nameIsImplicit = false;
		}
		if (name == null) {
			LOGGER.debug("name is null");
		} else {
			LOGGER.debug("name = " + name);
		}
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIController thisElem = templateElem != null ? (UIController) templateElem : new UIController(name);
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
				String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId, isDevLayout);
				LOGGER.debug("key,value = '" + attrKey + "', '" + attrVal + "'");
				String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
				if (overriddenVal != null) {
					LOGGER.debug("overridden value = " + overriddenVal);
				}
			}
		}
		
		// register with Animation/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIAnimation) {
			UIAnimation p = (UIAnimation) parent;
			p.getControllers().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Controller to be defined here. Parent: " + parent);
			// LOGGER.error("Parent element does not allow a Controller to be defined
			// here.");
			// thisElem.setNextAdditionShouldOverride(true);
			// return;
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
		
		// after parsing children
		thisElem.setNextAdditionShouldOverride(true);
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseState(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing State");
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("State has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIState thisElem = templateElem != null ? (UIState) templateElem : new UIState(name);
		
		// register with Animation/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIStateGroup) {
			UIStateGroup p = (UIStateGroup) parent;
			p.getStates().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a State to be defined here. Parent: " + parent);
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
		
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
	private void parseAnchor(Node node, UIElement parent, String raceId, boolean isDevLayout) throws UIException {
		LOGGER.debug("parsing Anchor");
		if (parent instanceof UIFrame) {
			UIFrame frame = (UIFrame) parent;
			
			NamedNodeMap attributes = node.getAttributes();
			LOGGER.debug("attribute length = " + attributes.getLength());
			
			Node nodeSide = XmlDomHelper.getNamedItemIgnoringCase(attributes, "side");
			Node nodeRelative = XmlDomHelper.getNamedItemIgnoringCase(attributes, "relative");
			Node nodePos = XmlDomHelper.getNamedItemIgnoringCase(attributes, "pos");
			Node nodeOffset = XmlDomHelper.getNamedItemIgnoringCase(attributes, "offset");
			
			if (nodeRelative == null) {
				throw new UIException("'Anchor' attribute does not contain required attribute 'relative='");
			}
			String relative = getConstantValue(nodeRelative.getNodeValue(), raceId, isDevLayout);
			String offset = nodeOffset == null ? null
					: getConstantValue(nodeOffset.getNodeValue(), raceId, isDevLayout);
			
			if (nodeSide != null) {
				// version with side
				if (nodePos == null) {
					throw new UIException("'Anchor' attribute contains 'side=', but not 'pos='");
				}
				if (nodeOffset == null) {
					throw new UIException("'Anchor' attribute contains 'side=', but not 'offset='");
				}
				String pos = getConstantValue(nodePos.getNodeValue(), raceId, isDevLayout);
				String side = getConstantValue(nodeSide.getNodeValue(), raceId, isDevLayout);
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
			parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " + parent.getName() + "'", isDevLayout);
			
		} else {
			throw new UIException("Anchor attributes are not supported here. Parent: " + parent);
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAttribute(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Attribute");
		String id = node.getNodeName();
		UIAttribute thisElem = new UIAttribute(id);
		LOGGER.debug("id = " + id);
		
		NamedNodeMap attributes = node.getAttributes();
		LOGGER.debug("attribute length = " + attributes.getLength());
		
		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttribute = attributes.item(i);
			String attrKey = curAttribute.getNodeName();
			String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId, isDevLayout);
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
			} else if (id.equalsIgnoreCase("driver")) {
				UIAnimation p = (UIAnimation) parent;
				p.setDriver(thisElem);
			} else {
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
			if (parent != null) {
				throw new UIException("Parent element '" + parent.getName()
						+ "' does not allow an Attribute to be defined here. Parent: " + parent);
			} else {
				throw new UIException(
						"Parent element 'null' does not allow an Attribute to be defined here. Parent: " + parent);
			}
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param childNodes
	 * @param thisElem
	 * @throws UIException
	 */
	private void parseAttrChildren(NodeList childNodes, String parentName, boolean isDevLayout) throws UIException {
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
			parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " + node.getNodeName() + "'", isDevLayout);
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseDesc(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Desc");
		
		// go deeper
		parse(node.getChildNodes(), null, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseStateGroup(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Stategroup");
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Stategroup has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIStateGroup thisElem = templateElem != null ? (UIStateGroup) templateElem : new UIStateGroup(name);
		
		// register with parent/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Stategroup to be defined here. Parent: " + parent);
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseAnimation(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Animation");
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Animation has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIAnimation thisElem = templateElem != null ? (UIAnimation) templateElem : new UIAnimation(name);
		// register with parent/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow an Animation to be defined here. Parent: " + parent);
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
		
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
	 * 
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
	 */
	private void addTemplate(String fileName, UIElement thisElem, boolean isDevLayout) {
		List<UITemplate> list = isDevLayout ? blizzDevTemplates : templates;
		list.add(new UITemplate(fileName, thisElem));
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
	private void parseConstant(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Constant");
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Constant has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("name = " + name);
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIConstant thisElem = templateElem != null ? (UIConstant) templateElem : new UIConstant(name);
		
		Node valAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "val");
		if (valAttrNode == null) {
			throw new UIException("Constant has no specified 'val'");
		}
		String val = getConstantValue(valAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("val = " + val);
		thisElem.setValue(val);
		
		// register with parent/templates overriding previous constant values
		if (!isDevLayout) {
			// is general layout
			boolean removedBlizzOnly = removeConstantFromList(name, blizzOnlyConstants);
			removeConstantFromList(name, constants);
			constants.add(thisElem);
			if (removedBlizzOnly) {
				LOGGER.warn("WARNING: constant '" + name
						+ "' overrides value from Blizz-only constant, so this might be fine.");
			}
		} else {
			// is blizz-only layout
			removeConstantFromList(name, blizzOnlyConstants);
			boolean removedGeneral = removeConstantFromList(name, blizzOnlyConstants);
			blizzOnlyConstants.add(thisElem);
			if (removedGeneral) {
				LOGGER.warn("WARNING: constant '" + name
						+ "' from Blizz-only layout overrides a general constant, so this might be fine.");
			}
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
	}
	
	private boolean removeConstantFromList(String name, List<UIConstant> listOfConstants) {
		boolean result = false;
		for (int i = listOfConstants.size() - 1; i >= 0; i--) {
			UIConstant curConst = listOfConstants.get(i);
			if (curConst.getName().compareToIgnoreCase(name) == 0) {
				listOfConstants.remove(i);
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseFrame(Node node, UIElement parent, String fileName, String raceId, boolean isDevLayout)
			throws UIException {
		LOGGER.debug("parsing Frame");
		Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Frame has no specified 'name'");
		}
		String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("name = " + name);
		
		Node typeAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "type");
		if (typeAttrNode == null) {
			throw new UIException("Frame has no specified 'type'");
		}
		String type = getConstantValue(typeAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.debug("type = " + type);
		
		String template = readTemplate(node.getAttributes());
		UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		UIFrame thisElem = templateElem != null ? (UIFrame) templateElem : new UIFrame(name, type);
		
		// register with parent/templates
		if (parent == null) {
			LOGGER.debug("Adding new template Frame named " + fileName + "/" + thisElem.getName());
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			UIFrame p = (UIFrame) parent;
			p.getChildren().add(thisElem);
		} else {
			throw new UIException("Parent element does not allow a Frame to be defined here. Parent: " + parent);
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param template
	 * @return Template instance
	 * @throws UIException
	 */
	private UIElement instanciateTemplate(String path, String newName, boolean isDevLayout, UIElement parent)
			throws UIException {
		if (path == null) {
			return null;
		}
		LOGGER.debug("Instanciating Template of path " + path);
		path = path.replace('\\', '/');
		String fileName = path.substring(0, path.indexOf("/"));
		
		// 1. check templates
		UIElement templateInstance = instanciateTemplateFromList(templates, fileName, path, newName);
		if (templateInstance != null) {
			return templateInstance;
		} else {
			// 2. if fail -> check dev templates
			templateInstance = instanciateTemplateFromList(blizzDevTemplates, fileName, path, newName);
			if (templateInstance != null) {
				if (!isDevLayout) {
					LOGGER.error("ERROR: the non-Blizz-only frame '" + parent + "' uses a Blizz-only template '" + path
							+ "'.");
				}
				return templateInstance;
			}
		}
		// template does not exist or its layout was not loaded, yet
		if (!isDevLayout) {
			LOGGER.error("ERROR: Template of path '" + path + "' could not be found.");
		} else {
			LOGGER.warn("WARNING: Template of path '" + path
					+ "' could not be found, but we are creating a Blizz-only layout, so this is fine.");
		}
		return null;
	}
	
	private UIElement instanciateTemplateFromList(List<UITemplate> templates, String fileName, String path,
			String newName) {
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
		return null;
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
	 * @param constantRef
	 * @param raceId
	 *            id String of the viewer's Race
	 * @return
	 */
	public String getConstantValue(String constantRef, String raceId, boolean isDevLayout) {
		String id = constantRef;
		int i = 0;
		while (id.startsWith("#")) {
			i++;
			id = id.substring(1);
		}
		// no constant tag
		if (i <= 0) {
			return constantRef;
		}
		String prefix = constantRef.substring(0, i);
		String constantName = constantRef.substring(i);
		LOGGER.debug("Encountered Constant: prefix='" + prefix + "', constantName='" + constantName + "'");
		for (UIConstant c : constants) {
			if (c.getName().equalsIgnoreCase(constantName)) {
				return c.getValue();
			}
		}
		// constant tag with race suffix
		if (i == 2) {
			String constantNameWithRacePostFix = constantName + "_" + "raceId";
			for (UIConstant c : constants) {
				if (c.getName().equalsIgnoreCase(constantNameWithRacePostFix)) {
					return c.getValue();
				}
			}
		}
		if (!isDevLayout) {
			LOGGER.warn("WARNING: Did not find a constant definition for '" + constantRef + "', so '" + constantName
					+ "' is used instead.");
		} else {
			LOGGER.warn("WARNING: Did not find a constant definition for '" + constantRef
					+ "', but it is a Blizz-only layout, so this is fine.");
		}
		return constantName;
	}
	
	/**
	 * @return the templates
	 */
	public ArrayList<UITemplate> getTemplates() {
		return templates;
	}
	
	/**
	 * @param templates
	 *            the templates to set
	 */
	public void setTemplates(ArrayList<UITemplate> templates) {
		this.templates = templates;
	}
	
	/**
	 * @return the blizzDevTemplates
	 */
	public ArrayList<UITemplate> getBlizzDevTemplates() {
		return blizzDevTemplates;
	}
	
	/**
	 * @param blizzDevTemplates
	 *            the blizzDevTemplates to set
	 */
	public void setBlizzDevTemplates(ArrayList<UITemplate> blizzDevTemplates) {
		this.blizzDevTemplates = blizzDevTemplates;
	}
	
}
