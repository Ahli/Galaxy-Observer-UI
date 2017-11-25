package com.ahli.galaxy.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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

import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.util.XmlDomHelper;

/**
 * Represents a container for UI frame.
 * 
 * @author Ahli
 * 
 */
public class UICatalog implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4725838598338592813L;
	
	private static final Logger LOGGER = LogManager.getLogger(UICatalog.class);
	
	// members
	private ArrayList<UITemplate> templates = null;
	private ArrayList<UITemplate> blizzOnlyTemplates = null;
	private ArrayList<UIConstant> constants = null;
	private ArrayList<UIConstant> blizzOnlyConstants = null;
	private ArrayList<String> blizzOnlyLayouts = null;
	
	// internal, used during processing
	private String curBasePath = "";
	
	/**
	 * Constructor.
	 */
	public UICatalog() {
		templates = new ArrayList<>(2500);
		blizzOnlyTemplates = new ArrayList<>(10);
		constants = new ArrayList<>(800);
		blizzOnlyConstants = new ArrayList<>(1);
		blizzOnlyLayouts = new ArrayList<>(25);
	}
	
	/**
	 * Constructor.
	 */
	public UICatalog(final int templatesCapacity, final int blizzOnlyTemplatesCapacity, final int constantsCapacity,
			final int blizzOnlyConstantsCapacity, final int blizzOnlyLayoutsCapacity) {
		templates = new ArrayList<>(templatesCapacity);
		blizzOnlyTemplates = new ArrayList<>(blizzOnlyTemplatesCapacity);
		constants = new ArrayList<>(constantsCapacity);
		blizzOnlyConstants = new ArrayList<>(blizzOnlyConstantsCapacity);
		blizzOnlyLayouts = new ArrayList<>(blizzOnlyLayoutsCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		// clone with additional space for templates and constants, but perfect fits for
		// blizzOnly
		final UICatalog clone = new UICatalog(templates.size() * 3 / 2 + 1, blizzOnlyTemplates.size(),
				constants.size() * 3 / 2 + 1, blizzOnlyConstants.size(), blizzOnlyLayouts.size());
		// 1041ms for AhliObs -> 12s execution time
		// testing shows that iterators are not faster and are not thread safe
		for (int i = 0; i < templates.size(); i++) {
			clone.templates.add((UITemplate) templates.get(i).clone());
		}
		for (int i = 0; i < blizzOnlyTemplates.size(); i++) {
			clone.blizzOnlyTemplates.add((UITemplate) blizzOnlyTemplates.get(i).clone());
		}
		for (int i = 0; i < constants.size(); i++) {
			clone.constants.add((UIConstant) constants.get(i).clone());
		}
		for (int i = 0; i < blizzOnlyConstants.size(); i++) {
			clone.blizzOnlyConstants.add((UIConstant) blizzOnlyConstants.get(i).clone());
		}
		for (int i = 0; i < blizzOnlyLayouts.size(); i++) {
			clone.blizzOnlyLayouts.add(blizzOnlyLayouts.get(i));
		}
		
		clone.curBasePath = curBasePath;
		return clone;
	}
	
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
	public void processDescIndex(final File f, final String raceId)
			throws SAXException, IOException, ParserConfigurationException, UIException {
		
		final ArrayList<String> generalLayouts = DescIndexReader.getLayoutPathList(f, true);
		
		final ArrayList<String> combinedList = new ArrayList<>(DescIndexReader.getLayoutPathList(f, false));
		
		blizzOnlyLayouts.addAll(combinedList);
		blizzOnlyLayouts.removeAll(generalLayouts);
		
		final String descIndexPath = f.getAbsolutePath();
		final String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());
		LOGGER.trace("descIndexPath=" + descIndexPath);
		LOGGER.trace("basePath=" + basePath);
		
		processLayouts(combinedList, basePath, raceId);
		
		LOGGER.info("UICatalogSizes: " + templates.size() + " " + blizzOnlyTemplates.size() + " " + constants.size()
				+ " " + blizzOnlyConstants.size() + " " + blizzOnlyLayouts.size());
	}
	
	/**
	 * 
	 * @param toProcessList
	 * @param basePath
	 */
	private void processLayouts(final ArrayList<String> toProcessList, final String basePath, final String raceId) {
		loop: for (final String intPath : toProcessList) {
			final boolean isDevLayout = blizzOnlyLayouts.contains(intPath);
			LOGGER.trace("intPath=" + intPath);
			LOGGER.trace("isDevLayout=" + isDevLayout);
			String basePathTemp = "" + basePath;
			while (!new File(basePathTemp + File.separator + intPath).exists()) {
				final int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					basePathTemp = basePathTemp.substring(0, basePathTemp.lastIndexOf(File.separatorChar));
					LOGGER.trace("basePathTemp=" + basePathTemp);
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
			final File layoutFile = new File(basePathTemp + File.separator + intPath);
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
	public void processLayoutFile(final File f, final String raceId, final boolean isDevLayout)
			throws SAXException, IOException, ParserConfigurationException, UIException {
		if (!isDevLayout) {
			LOGGER.info("Processing layout file " + f.getAbsolutePath());
		} else {
			LOGGER.info("Processing Blizz-only layout file " + f.getAbsolutePath());
		}
		
		final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
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
			
			LOGGER.trace("retrieving document's children");
			final NodeList nodes = doc.getChildNodes();
			
			if (nodes.getLength() <= 0) {
				LOGGER.warn("Empty layout file: " + f);
				return;
			} else {
				// parse document's content
				parse(nodes, null, f.getName().substring(0, f.getName().lastIndexOf('.')), raceId, isDevLayout);
				LOGGER.trace("Finished parsing file.");
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
	private void processLayoutFile(final String pathAttributeValue, final String raceId, final boolean isDevLayout) {
		LOGGER.trace("processing layoutFile from include: " + pathAttributeValue);
		if (isDevLayout) {
			blizzOnlyLayouts.add(pathAttributeValue);
		}
		final String basePath = curBasePath;
		final ArrayList<String> list = new ArrayList<>();
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
	private void parse(final NodeList childNodes, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		if (childNodes != null) {
			final int len = childNodes.getLength();
			LOGGER.trace("checking childNodes, length: " + len);
			
			for (int i = 0; i < len; i++) {
				final Node curNode = childNodes.item(i);
				
				if (isTrashNodeType(curNode)) {
					LOGGER.trace("Skipping node type " + curNode.getNodeType());
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
	private boolean isTrashNodeType(final Node node) {
		// only Node.ELEMENT_NODE is good to use and not trash
		return node.getNodeType() != Node.ELEMENT_NODE;
	}
	
	/**
	 * Checks if any attribute is the requiredToLoad one and does not start with
	 * "!".
	 * 
	 * @param attributes
	 * @return
	 */
	public static boolean isFailingRequiredToLoad(final NamedNodeMap attributes) {
		final Node requiredtoloadAttr = XmlDomHelper.getNamedItemIgnoringCase(attributes, "requiredtoload");
		return requiredtoloadAttr != null && !requiredtoloadAttr.getNodeValue().trim().startsWith("!");
	}
	
	/**
	 * 
	 * @param childNode
	 * @param parent
	 * @throws UIException
	 */
	private void parse(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		final String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
		LOGGER.trace("node name: " + nodeName);
		
		// do not load, if requiredtoload
		if (isFailingRequiredToLoad(node.getAttributes())) {
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
	private void parseInclude(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout, final boolean hasReqToLoadAttribute) {
		final NamedNodeMap attributes = node.getAttributes();
		final Node pathAttribute = XmlDomHelper.getNamedItemIgnoringCase(attributes, "path");
		if (pathAttribute != null) {
			final boolean thisOneIsDevLayoutToo = (isDevLayout || hasReqToLoadAttribute);
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
	private void parseDescFlags(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing DescFlags");
		if (parent == null) {
			final Node val = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "val");
			if (getConstantValue(val.getNodeValue(), raceId, isDevLayout).compareToIgnoreCase("locked") == 0) {
				for (final UITemplate template : templates) {
					if (template.getFileName().compareToIgnoreCase(fileName) == 0) {
						template.setLocked(true);
						LOGGER.trace("locked templated " + fileName + "/" + template.getElement().getName());
					}
				}
			}
		} else {
			LOGGER.trace("DescFlags found with parent. -> ignored");
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
	private void parseController(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Controller");
		// Controllers may not have a name defined
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		String name = null;
		boolean nameIsImplicit = true;
		
		if (nameAttrNode != null) {
			name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
			nameIsImplicit = false;
		}
		if (name == null) {
			LOGGER.trace("name is null");
		} else {
			LOGGER.trace("name = " + name);
		}
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIController thisElem = templateElem != null ? (UIController) templateElem : new UIController(name);
		thisElem.setNameIsImplicit(nameIsImplicit);
		
		// parse other settings in that line
		final NamedNodeMap attributes = node.getAttributes();
		LOGGER.trace("attribute length = " + attributes.getLength());
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node curAttribute = attributes.item(i);
			if (nameAttrNode == curAttribute) {
				// ignore name attribute
				continue;
			} else {
				final String attrKey = curAttribute.getNodeName();
				final String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId, isDevLayout);
				LOGGER.trace("key,value = '" + attrKey + "', '" + attrVal + "'");
				// final String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
				final String overriddenVal = thisElem.addValue(attrKey, attrVal);
				if (overriddenVal != null) {
					LOGGER.trace("overridden value = " + overriddenVal);
				}
			}
		}
		
		// register with Animation/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIAnimation) {
			final UIAnimation p = (UIAnimation) parent;
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
	private void parseState(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing State");
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("State has no specified 'name'");
		}
		final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("name = " + name);
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIState thisElem = templateElem != null ? (UIState) templateElem : new UIState(name);
		
		// register with Animation/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIStateGroup) {
			final UIStateGroup p = (UIStateGroup) parent;
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
	private void parseAnchor(final Node node, final UIElement parent, final String raceId, final boolean isDevLayout)
			throws UIException {
		LOGGER.trace("parsing Anchor");
		if (parent instanceof UIFrame) {
			final UIFrame frame = (UIFrame) parent;
			
			final NamedNodeMap attributes = node.getAttributes();
			LOGGER.trace("attribute length = " + attributes.getLength());
			
			final Node nodeSide = XmlDomHelper.getNamedItemIgnoringCase(attributes, "side");
			final Node nodeRelative = XmlDomHelper.getNamedItemIgnoringCase(attributes, "relative");
			final Node nodePos = XmlDomHelper.getNamedItemIgnoringCase(attributes, "pos");
			final Node nodeOffset = XmlDomHelper.getNamedItemIgnoringCase(attributes, "offset");
			
			if (nodeRelative == null) {
				throw new UIException("'Anchor' attribute does not contain required attribute 'relative='");
			}
			final String relative = getConstantValue(nodeRelative.getNodeValue(), raceId, isDevLayout);
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
				final String pos = getConstantValue(nodePos.getNodeValue(), raceId, isDevLayout);
				final String side = getConstantValue(nodeSide.getNodeValue(), raceId, isDevLayout);
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
				} catch (final NumberFormatException e) {
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
	private void parseAttribute(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Attribute");
		final String id = node.getNodeName();
		final UIAttribute thisElem = new UIAttribute(id);
		LOGGER.trace("id = " + id);
		
		final NamedNodeMap attributes = node.getAttributes();
		LOGGER.trace("attribute length = " + attributes.getLength());
		
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node curAttribute = attributes.item(i);
			final String attrKey = curAttribute.getNodeName();
			final String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId, isDevLayout);
			LOGGER.trace("key,value = '" + attrKey + "', '" + attrVal + "'");
			final String overriddenVal = thisElem.addValue(attrKey, attrVal);
			if (overriddenVal != null) {
				LOGGER.trace("overridden value = " + overriddenVal);
			}
		}
		
		// register with frame/animation/stategroup/state/controller
		if (parent instanceof UIFrame) {
			final UIFrame p = (UIFrame) parent;
			// p.addAttribute(id, thisElem);
			p.getAttributes().put(id, thisElem);
		} else if (parent instanceof UIAnimation) {
			if (id.equalsIgnoreCase("event")) {
				final UIAnimation p = (UIAnimation) parent;
				// override all animation events
				if (p.isNextEventsAdditionShouldOverride()) {
					p.getEvents().clear();
					p.setNextEventsAdditionShouldOverride(false);
				}
				p.addEvent(id, thisElem);
			} else if (id.equalsIgnoreCase("driver")) {
				final UIAnimation p = (UIAnimation) parent;
				p.setDriver(thisElem);
			} else {
				throw new UIException("Parent element (Frame) expects 'Event' attribute instead of '" + id + "'");
			}
		} else if (parent instanceof UIStateGroup) {
			if (id.compareToIgnoreCase("defaultstate") != 0) {
				throw new UIException(
						"Parent element (Animation) expects 'DefaultState' attribute instead of '" + id + "'");
			}
			final UIStateGroup p = (UIStateGroup) parent;
			final String val = thisElem.getValue("val");
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
			final UIController p = (UIController) parent;
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
	private void parseAttrChildren(final NodeList childNodes, final String parentName, final boolean isDevLayout)
			throws UIException {
		// no non-trash children allowed
		final int len = childNodes.getLength();
		for (int i = 0; i < len; i++) {
			final Node node = childNodes.item(i);
			if (isTrashNodeType(node)) {
				final String msg = "Found trash nodeType within Attribute '" + parentName + "', nodeType = '"
						+ node.getNodeType() + "'";
				LOGGER.trace(msg);
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
	private void parseDesc(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Desc");
		
		// go deeper
		parse(node.getChildNodes(), null, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseStateGroup(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Stategroup");
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Stategroup has no specified 'name'");
		}
		final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("name = " + name);
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIStateGroup thisElem = templateElem != null ? (UIStateGroup) templateElem : new UIStateGroup(name);
		
		// register with parent/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			final UIFrame p = (UIFrame) parent;
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
	private void parseAnimation(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Animation");
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Animation has no specified 'name'");
		}
		final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("name = " + name);
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIAnimation thisElem = templateElem != null ? (UIAnimation) templateElem : new UIAnimation(name);
		// register with parent/templates
		if (parent == null) {
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			final UIFrame p = (UIFrame) parent;
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
		}
		// else {
		// // cannot set names for templates as they could be incomplete,
		// // right?
		// }
		
		thisElem.setNextEventsAdditionShouldOverride(true);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
	 */
	private void addTemplate(final String fileName, final UIElement thisElem, final boolean isDevLayout) {
		final List<UITemplate> list = isDevLayout ? blizzOnlyTemplates : templates;
		list.add(new UITemplate(fileName, thisElem));
	}
	
	/**
	 * Set the implicit names of controllers in animations.
	 * 
	 * @param thisElem
	 * @throws UIException
	 */
	private void setImplicitControllerNames(final UIAnimation thisElem) throws UIException {
		LOGGER.trace("Setting implicit controller names");
		final List<UIController> controllers = thisElem.getControllers();
		for (final UIController contr : controllers) {
			if (contr.getName() == null) {
				// final String type = contr.getValues().get("type");
				final String type = contr.getValue("type");
				LOGGER.trace("type = " + type);
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
	private String getImplicitName(final String type, final List<UIController> controllers) throws UIException {
		LOGGER.trace("Constructing implicit controller name");
		if (type == null) {
			throw new UIException("'type=\"...\"' of Controller is not set or invalid.");
		}
		
		int i = 0;
		while (true) {
			final String name = type + "_" + i;
			
			if (controllers.stream()
					.noneMatch(t -> t.getName() != null && t.getName().compareToIgnoreCase(name) == 0)) {
				LOGGER.trace("Constructing implicit controller name: " + name);
				return name;
			}
			LOGGER.trace("Implicit controller name existing: " + name);
			i++;
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @throws UIException
	 */
	private void parseConstant(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Constant");
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Constant has no specified 'name'");
		}
		final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("name = " + name);
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIConstant thisElem = templateElem != null ? (UIConstant) templateElem : new UIConstant(name);
		
		final Node valAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "val");
		if (valAttrNode == null) {
			throw new UIException("Constant has no specified 'val'");
		}
		final String val = getConstantValue(valAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("val = " + val);
		thisElem.setValue(val);
		
		// register with parent/templates overriding previous constant values
		if (!isDevLayout) {
			// is general layout
			final boolean removedBlizzOnly = removeConstantFromList(name, blizzOnlyConstants);
			removeConstantFromList(name, constants);
			constants.add(thisElem);
			if (removedBlizzOnly) {
				LOGGER.warn("WARNING: constant '" + name
						+ "' overrides value from Blizz-only constant, so this might be fine.");
			}
		} else {
			// is blizz-only layout
			removeConstantFromList(name, blizzOnlyConstants);
			final boolean removedGeneral = removeConstantFromList(name, blizzOnlyConstants);
			blizzOnlyConstants.add(thisElem);
			if (removedGeneral) {
				LOGGER.warn("WARNING: constant '" + name
						+ "' from Blizz-only layout overrides a general constant, so this might be fine.");
			}
		}
		
		// go deeper
		parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
	}
	
	/**
	 * 
	 * @param name
	 * @param listOfConstants
	 * @return
	 */
	private boolean removeConstantFromList(final String name, final List<UIConstant> listOfConstants) {
		boolean result = false;
		for (int i = listOfConstants.size() - 1; i >= 0; i--) {
			final UIConstant curConst = listOfConstants.get(i);
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
	private void parseFrame(final Node node, final UIElement parent, final String fileName, final String raceId,
			final boolean isDevLayout) throws UIException {
		LOGGER.trace("parsing Frame");
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "name");
		if (nameAttrNode == null) {
			throw new UIException("Frame has no specified 'name'");
		}
		final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("name = " + name);
		
		final Node typeAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), "type");
		if (typeAttrNode == null) {
			throw new UIException("Frame has no specified 'type'");
		}
		final String type = getConstantValue(typeAttrNode.getNodeValue(), raceId, isDevLayout);
		LOGGER.trace("type = " + type);
		
		final String template = readTemplate(node.getAttributes());
		final UIElement templateElem = instanciateTemplate(template, name, isDevLayout, parent);
		final UIFrame thisElem = templateElem != null ? (UIFrame) templateElem : new UIFrame(name, type);
		
		// register with parent/templates
		if (parent == null) {
			LOGGER.trace("Adding new template Frame named " + fileName + "/" + thisElem.getName());
			addTemplate(fileName, thisElem, isDevLayout);
		} else if (parent instanceof UIFrame) {
			final UIFrame p = (UIFrame) parent;
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
	private UIElement instanciateTemplate(String path, final String newName, final boolean isDevLayout,
			final UIElement parent) throws UIException {
		if (path == null) {
			return null;
		}
		LOGGER.trace("Instanciating Template of path " + path);
		path = path.replace('\\', '/');
		final String fileName = path.substring(0, path.indexOf("/"));
		
		// 1. check templates
		UIElement templateInstance = instanciateTemplateFromList(templates, fileName, path, newName);
		if (templateInstance != null) {
			return templateInstance;
		} else {
			// 2. if fail -> check dev templates
			templateInstance = instanciateTemplateFromList(blizzOnlyTemplates, fileName, path, newName);
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
	
	/**
	 * 
	 * @param templates
	 * @param fileName
	 * @param path
	 * @param newName
	 * @return
	 */
	private UIElement instanciateTemplateFromList(final List<UITemplate> templates, final String fileName,
			final String path, final String newName) {
		for (final UITemplate curTemplate : templates) {
			if (curTemplate.getFileName().equalsIgnoreCase(fileName)) {
				// found template file
				final String newPath = UIElement.removeLeftPathLevel(path);
				final UIElement frameFromPath = curTemplate.receiveFrameFromPath(newPath);
				
				if (frameFromPath == null) {
					// not the correct template
					continue;
				}
				final UIElement clone = (UIElement) frameFromPath.clone();
				clone.setName(newName);
				return clone;
			}
		}
		return null;
	}
	
	/**
	 * Reads a template if it is existing.
	 * 
	 * @param attributes
	 *            the attributes
	 * @return template reference or null
	 */
	private String readTemplate(final NamedNodeMap attributes) {
		final Node template = attributes.getNamedItem("template");
		return template != null ? template.getNodeValue() : null;
	}
	
	/**
	 * 
	 * @param constantRef
	 * @param raceId
	 *            id String of the viewer's Race
	 * @return
	 */
	public String getConstantValue(final String constantRef, final String raceId, final boolean isDevLayout) {
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
		final String prefix = constantRef.substring(0, i);
		final String constantName = constantRef.substring(i);
		LOGGER.trace("Encountered Constant: prefix='" + prefix + "', constantName='" + constantName + "'");
		for (final UIConstant c : constants) {
			if (c.getName().equalsIgnoreCase(constantName)) {
				return c.getValue();
			}
		}
		// constant tag with race suffix
		if (i == 2) {
			final String constantNameWithRacePostFix = constantName + "_" + raceId;
			for (final UIConstant c : constants) {
				if (c.getName().equalsIgnoreCase(constantNameWithRacePostFix)) {
					return c.getValue();
				}
			}
		}
		if (i >= 3) {
			LOGGER.error("ERROR: Encountered a constant definition with three #'" + constantRef
					+ "' when its maximum is two '#'.");
		}
		
		if (!isDevLayout) {
			LOGGER.warn("WARNING: Did not find a constant definition for '" + constantRef + "', so '" + constantName
					+ "' is used instead.");
		} else {
			// inside blizz-only
			for (final UIConstant c : blizzOnlyConstants) {
				if (c.getName().equalsIgnoreCase(constantName)) {
					return c.getValue();
				}
			}
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
	public void setTemplates(final ArrayList<UITemplate> templates) {
		this.templates = templates;
	}
	
	/**
	 * @return the blizzOnlyTemplates
	 */
	public ArrayList<UITemplate> getBlizzOnlyTemplates() {
		return blizzOnlyTemplates;
	}
	
	/**
	 * @param blizzOnlyTemplates
	 *            the blizzOnlyTemplates to set
	 */
	public void setBlizzOnlyTemplates(final ArrayList<UITemplate> blizzOnlyTemplates) {
		this.blizzOnlyTemplates = blizzOnlyTemplates;
	}
	
	/**
	 * @return the constants
	 */
	public ArrayList<UIConstant> getConstants() {
		return constants;
	}
	
	/**
	 * @param constants
	 *            the constants to set
	 */
	public void setConstants(final ArrayList<UIConstant> constants) {
		this.constants = constants;
	}
	
	/**
	 * @return the blizzOnlyConstants
	 */
	public ArrayList<UIConstant> getBlizzOnlyConstants() {
		return blizzOnlyConstants;
	}
	
	/**
	 * @param blizzOnlyConstants
	 *            the blizzOnlyConstants to set
	 */
	public void setBlizzOnlyConstants(final ArrayList<UIConstant> blizzOnlyConstants) {
		this.blizzOnlyConstants = blizzOnlyConstants;
	}
	
	/**
	 * @return the devLayouts
	 */
	public ArrayList<String> getDevLayouts() {
		return blizzOnlyLayouts;
	}
	
	/**
	 * @param devLayouts
	 *            the devLayouts to set
	 */
	public void setDevLayouts(final ArrayList<String> devLayouts) {
		blizzOnlyLayouts = devLayouts;
	}
	
	/**
	 * @return the curBasePath
	 */
	public String getCurBasePath() {
		return curBasePath;
	}
	
	/**
	 * @param curBasePath
	 *            the curBasePath to set
	 */
	public void setCurBasePath(final String curBasePath) {
		this.curBasePath = curBasePath;
	}
	
}
