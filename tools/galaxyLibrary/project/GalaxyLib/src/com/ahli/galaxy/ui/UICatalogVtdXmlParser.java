// package com.ahli.galaxy.ui;
//
// import java.io.File;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Locale;
// import java.util.function.Predicate;
//
// import javax.xml.parsers.DocumentBuilder;
// import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.parsers.ParserConfigurationException;
//
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.w3c.dom.DOMException;
// import org.w3c.dom.NamedNodeMap;
// import org.w3c.dom.Node;
// import org.w3c.dom.NodeList;
// import org.xml.sax.SAXException;
//
// import com.ahli.galaxy.ui.exception.UIException;
// import com.ahli.galaxy.ui.interfaces.UICatalog;
// import com.ahli.util.XmlDomHelper;
// import com.ximpleware.AutoPilot;
// import com.ximpleware.NavException;
// import com.ximpleware.VTDGen;
// import com.ximpleware.VTDNav;
//
/// **
// * Represents a container for UI frame.
// *
// * @author Ahli
// */
// public class UICatalogVtdXmlParser implements UICatalog {
// private static final String TOP = "top";
//
// private static final String DRIVER = "driver";
//
// private static final String TEMPLATE2 = "template";
//
// private static final String DESC_FLAGS = "'DescFlags'";
//
// private static final String DEFAULTSTATE = "defaultstate";
//
// private static final String RIGHT = "right";
//
// private static final String ZERO_VAL = "0";
//
// private static final String EVENT = "event";
//
// private static final String BOTTOM = "bottom";
//
// private static final String LEFT = "left";
//
// private static final String LOCKED = "locked";
//
// private static final String VAL = "val";
//
// private static final String PATH = "path";
//
// private static final String CONSTANT = "constant";
//
// private static final String STATEGROUP = "stategroup";
//
// private static final String ANIMATION = "animation";
//
// private static final String CONTROLLER = "controller";
//
// private static final String STATE = "state";
//
// private static final String ANCHOR = "anchor";
//
// private static final String FRAME = "frame";
//
// private static final String DESC = "desc";
//
// private static final String DESCFLAGS = "descflags";
//
// private static final String ACTION = "action";
//
// private static final String KEY = "key";
//
// private static final String WHEN = "when";
//
// private static final String INCLUDE = "include";
//
// private static final String TYPE_ATTR = "type";
//
// private static final String NAME_ATTR = "name";
//
// static long totalExecutionTime = 0;
//
// private static final Logger logger = LogManager.getLogger();
//
// DocumentBuilder dBuilder = null;
//
// // members
// private List<UITemplate> templates;
// private List<UITemplate> blizzOnlyTemplates;
// private List<UIConstant> constants;
// private List<UIConstant> blizzOnlyConstants;
// private List<String> blizzOnlyLayouts;
//
// // internal, used during processing
// private String curBasePath;
//
// /**
// * Constructor.
// *
// * @throws ParserConfigurationException
// */
// public UICatalogVtdXmlParser() {
// templates = new ArrayList<>(2500);
// blizzOnlyTemplates = new ArrayList<>();
// constants = new ArrayList<>(800);
// blizzOnlyConstants = new ArrayList<>();
// blizzOnlyLayouts = new ArrayList<>(25);
// }
//
// /**
// * Constructor.
// *
// * @throws ParserConfigurationException
// */
// public UICatalogVtdXmlParser(final int templatesCapacity, final int
// blizzOnlyTemplatesCapacity,
// final int constantsCapacity, final int blizzOnlyConstantsCapacity, final int
// blizzOnlyLayoutsCapacity) {
// templates = new ArrayList<>(templatesCapacity);
// blizzOnlyTemplates = new ArrayList<>(blizzOnlyTemplatesCapacity);
// constants = new ArrayList<>(constantsCapacity);
// blizzOnlyConstants = new ArrayList<>(blizzOnlyConstantsCapacity);
// blizzOnlyLayouts = new ArrayList<>(blizzOnlyLayoutsCapacity);
// }
//
// /**
// * Returns a deep clone of this.
// */
// @Override
// public Object deepCopy() {
// // clone with additional space for templates and constants, but perfect fits
// for
// // blizzOnly
// final UICatalogVtdXmlParser clone = new
// UICatalogVtdXmlParser(templates.size() * 3 / 2 + 1,
// blizzOnlyTemplates.size(), constants.size() * 3 / 2 + 1,
// blizzOnlyConstants.size(),
// blizzOnlyLayouts.size());
// // 1041ms for AhliObs -> 12s execution time
// // testing shows that iterators are not faster and are not thread safe
// int i, len;
// for (i = 0, len = templates.size(); i < len; i++) {
// clone.templates.add((UITemplate) templates.get(i).deepCopy());
// }
// for (i = 0, len = blizzOnlyTemplates.size(); i < len; i++) {
// clone.blizzOnlyTemplates.add((UITemplate)
// blizzOnlyTemplates.get(i).deepCopy());
// }
// for (i = 0, len = constants.size(); i < len; i++) {
// clone.constants.add((UIConstant) constants.get(i).deepCopy());
// }
// for (i = 0, len = blizzOnlyConstants.size(); i < len; i++) {
// clone.blizzOnlyConstants.add((UIConstant)
// blizzOnlyConstants.get(i).deepCopy());
// }
// for (i = 0, len = blizzOnlyLayouts.size(); i < len; i++) {
// clone.blizzOnlyLayouts.add(blizzOnlyLayouts.get(i));
// }
// clone.curBasePath = curBasePath;
//
// // TODO debug
// // printDebugStats();
// // clone.printDebugStats();
//
// return clone;
// }
//
// /**
// * @throws ParserConfigurationException
// */
// private void prepareDomParser() throws ParserConfigurationException {
// if (dBuilder == null) {
// dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
// }
// }
//
// @Override
// public void clearParser() {
// dBuilder = null;
// }
//
// @Override
// public void processDescIndex(final File f, final String raceId)
// throws SAXException, IOException, ParserConfigurationException, UIException,
// InterruptedException {
//
// final ArrayList<String> generalLayouts = DescIndexReader.getLayoutPathList(f,
// true);
//
// final ArrayList<String> combinedList = new
// ArrayList<>(DescIndexReader.getLayoutPathList(f, false));
//
// blizzOnlyLayouts.addAll(combinedList);
// blizzOnlyLayouts.removeAll(generalLayouts);
//
// final String descIndexPath = f.getAbsolutePath();
// final String basePath = descIndexPath.substring(0, descIndexPath.length() -
// f.getName().length());
// logger.trace("descIndexPath=" + descIndexPath);
// logger.trace("basePath=" + basePath);
//
// processLayouts(combinedList, basePath, raceId);
//
// printDebugStats();
// }
//
// /**
// * @param toProcessList
// * @param basePath
// * @throws InterruptedException
// * if the current thread was interrupted
// */
// private void processLayouts(final ArrayList<String> toProcessList, final
// String basePath, final String raceId)
// throws InterruptedException {
// loop: for (final String intPath : toProcessList) {
// final boolean isDevLayout = blizzOnlyLayouts.contains(intPath);
// logger.trace("intPath=" + intPath);
// logger.trace("isDevLayout=" + isDevLayout);
// String basePathTemp = "" + basePath;
// while (!new File(basePathTemp + File.separator + intPath).exists()) {
// final int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
// if (lastIndex != -1) {
// basePathTemp = basePathTemp.substring(0,
// basePathTemp.lastIndexOf(File.separatorChar));
// logger.trace("basePathTemp=" + basePathTemp);
// } else {
// if (!isDevLayout) {
// logger.error("ERROR: Cannot find layout file: " + intPath);
// } else {
// logger.warn("WARNING: Cannot find Blizz-only layout file: " + intPath + ", so
// this is fine.");
// }
// continue loop;
// }
// }
// curBasePath = basePathTemp;
// final File layoutFile = new File(basePathTemp + File.separator + intPath);
// try {
// processLayoutFile(layoutFile, raceId, isDevLayout);
// } catch (IOException | ParserConfigurationException | UIException e) {
// logger.error("ERROR: encountered an Exception while processing the layout
// file '" + layoutFile + "'.",
// e);
// e.printStackTrace();
// }
// if (Thread.interrupted()) {
// throw new InterruptedException();
// }
// }
// }
//
// @Override
// public void processLayoutFile(final File f, final String raceId, final
// boolean isDevLayout)
// throws IOException, ParserConfigurationException, UIException,
// InterruptedException {
// if (!isDevLayout) {
// logger.info("Processing layout file " + f.getAbsolutePath());
// } else {
// logger.info("Processing Blizz-only layout file " + f.getAbsolutePath());
// }
//
// try {
// // parse XML file
//
// final long startTime = System.currentTimeMillis();
// prepareDomParser();
//
// // TODO
// final VTDGen vtd = new VTDGen();
// vtd.parseFile(f.getPath(), false);
// final VTDNav nav = vtd.getNav();
// final AutoPilot ap = new AutoPilot(nav);
// ap.selectElement("*");
//
// while (ap.iterate()) {
// final int curDepth = nav.getCurrentDepth();
//
// }
//
// final long executionTime = (System.currentTimeMillis() - startTime);
// synchronized (this) {
// totalExecutionTime += executionTime;
// }
// logger.info("XML parsing layout file took " + executionTime + "ms. Total
// time: " + totalExecutionTime);
//
// // logger.trace("retrieving document's children");
// // final NodeList nodes = doc.getChildNodes();
// //
// // if (nodes.getLength() <= 0) {
// // logger.warn("Empty layout file: " + f);
// // return;
// // } else {
// // // parse document's content
// // parse(nodes, null, f.getName().substring(0, f.getName().lastIndexOf('.')),
// // raceId, isDevLayout);
// // logger.trace("Finished parsing file.");
// // }
// } catch (final NavException e) {
// logger.error("Failed to parse file: " + f, e);
// // couldn't parse, most likely no XML file
// // -> do nothing
// }
// }
//
// /**
// * @param nodeValue
// * @param raceId
// * @throws InterruptedException
// * if the current Thread was interrupted
// */
// private void processLayoutFile(final String pathAttributeValue, final String
// raceId, final boolean isDevLayout)
// throws InterruptedException {
// logger.trace("processing layoutFile from include: " + pathAttributeValue);
// if (isDevLayout) {
// blizzOnlyLayouts.add(pathAttributeValue);
// }
// final String basePath = curBasePath;
// final ArrayList<String> list = new ArrayList<>();
// list.add(pathAttributeValue);
// processLayouts(list, basePath, raceId);
// }
//
// /**
// * @param childNodes
// * @param parent
// * @param isDevLayout
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parse(final NodeList childNodes, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// if (childNodes != null) {
// for (int i = 0, len = childNodes.getLength(); i < len; i++) {
// final Node curNode = childNodes.item(i);
//
// if (isTrashNodeType(curNode)) {
// logger.trace("Skipping node type " + curNode.getNodeType());
// continue;
// }
//
// parse(curNode, parent, fileName, raceId, isDevLayout);
// // TODO test, seems to lower the performance via increase GC
// // due to the un-editable list, this is stupid anyway
// // curNode.getParentNode().removeChild(curNode);
// // i--;
// // len--;
// }
// }
// }
//
// /**
// * @param node
// * @return
// */
// private boolean isTrashNodeType(final Node node) {
// // only Node.ELEMENT_NODE is good to use and not trash
// return node.getNodeType() != Node.ELEMENT_NODE;
// }
//
// /**
// * @param childNode
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parse(final Node node, final UIElement parent, final String
// fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// final String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
// logger.trace("node name: " + nodeName);
//
// // do not load, if requiredtoload
// if (XmlDomHelper.isFailingRequiredToLoad(node.getAttributes())) {
// if (nodeName.equals(INCLUDE)) {
// parseInclude(node, parent, fileName, raceId, isDevLayout, true);
// } else if (!isDevLayout) {
// logger.warn("WARNING: Encountered 'requiredToLoad' attribute in
// non-Blizz-only layout. ElementName: "
// + nodeName + ", parent: " + parent);
// }
// return;
// }
//
// // use lowercase for cases!
// switch (nodeName) {
// case FRAME:
// parseFrame(node, parent, fileName, raceId, isDevLayout);
// break;
// case ANCHOR:
// parseAnchor(node, parent, raceId, isDevLayout);
// break;
// case STATE:
// parseState(node, parent, fileName, raceId, isDevLayout);
// break;
// case CONTROLLER:
// parseController(node, parent, fileName, raceId, isDevLayout);
// break;
// case ANIMATION:
// parseAnimation(node, parent, fileName, raceId, isDevLayout);
// break;
// case STATEGROUP:
// parseStateGroup(node, parent, fileName, raceId, isDevLayout);
// break;
// case CONSTANT:
// parseConstant(node, parent, fileName, raceId, isDevLayout);
// break;
// case DESC:
// parseDesc(node, parent, fileName, raceId, isDevLayout);
// break;
// case DESCFLAGS:
// parseDescFlags(node, parent, fileName, raceId, isDevLayout);
// break;
// case INCLUDE:
// parseInclude(node, parent, fileName, raceId, isDevLayout, false);
// break;
// default:
// // attribute or something unknown that will cause an error
// parseAttribute(node, parent, fileName, raceId, isDevLayout);
// }
// }
//
// /**
// * @param node
// * @param parent
// * @param fileName
// * @param raceId
// * @param isDevLayout
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseInclude(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout, final boolean hasReqToLoadAttribute) throws
// DOMException, InterruptedException {
// final NamedNodeMap attributes = node.getAttributes();
// final Node pathAttribute = XmlDomHelper.getNamedItemIgnoringCase(attributes,
// PATH);
// if (pathAttribute != null) {
// final boolean thisOneIsDevLayoutToo = (isDevLayout || hasReqToLoadAttribute);
// processLayoutFile(pathAttribute.getNodeValue(), raceId,
// thisOneIsDevLayoutToo);
// } else {
// if (!isDevLayout) {
// logger.error("ERROR: encountered 'Include' without required 'path'
// attribute.");
// } else {
// logger.error(
// "WARNING: encountered 'Include' without required 'path' attribute in a
// blizz-only layout, so this is fine.");
// }
// }
// }
//
// /**
// * @param node
// * @param parent
// * @param fileName
// * @throws UIException
// */
// private void parseDescFlags(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException {
// logger.trace("parsing DescFlags");
// if (parent == null) {
// final Node val = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(),
// VAL);
// if (getConstantValue(val.getNodeValue(), raceId,
// isDevLayout).compareToIgnoreCase(LOCKED) == 0) {
// for (final UITemplate template : templates) {
// if (template.getFileName().compareToIgnoreCase(fileName) == 0) {
// template.setLocked(true);
// logger.trace("locked templated " + fileName + "/" +
// template.getElement().getName());
// }
// }
// }
// } else {
// logger.trace("DescFlags found with parent. -> ignored");
// }
//
// // verify that it cannot go deeper
// parseAttrChildren(node.getChildNodes(), DESC_FLAGS, isDevLayout);
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseController(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Controller");
// // Controllers may not have a name defined
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// String name = null;
// boolean nameIsImplicit = true;
//
// if (nameAttrNode != null) {
// name = getConstantValue(nameAttrNode.getNodeValue(), raceId, isDevLayout);
// nameIsImplicit = false;
// }
// if (name == null) {
// logger.trace("name is null");
// } else {
// logger.trace("name = " + name);
// }
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIController thisElem = templateElem != null ? (UIController)
// templateElem : new UIController(name);
// thisElem.setNameIsImplicit(nameIsImplicit);
//
// // parse other settings in that line
// final NamedNodeMap attributes = node.getAttributes();
// logger.trace("attribute length = " + attributes.getLength());
// for (int i = 0; i < attributes.getLength(); i++) {
// final Node curAttribute = attributes.item(i);
// if (nameAttrNode == curAttribute) {
// // ignore name attribute
// continue;
// } else {
// final String attrKey = curAttribute.getNodeName();
// final String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("key,value = '" + attrKey + "', '" + attrVal + "'");
// // final String overriddenVal = thisElem.getValues().put(attrKey, attrVal);
// final String overriddenVal = thisElem.addValue(attrKey, attrVal);
// if (overriddenVal != null) {
// logger.trace("overridden value = " + overriddenVal);
// }
// }
// }
//
// // register with Animation/templates
// if (parent == null) {
// addTemplate(fileName, thisElem, isDevLayout);
// } else if (parent instanceof UIAnimation) {
// final UIAnimation p = (UIAnimation) parent;
// p.getControllers().add(thisElem);
// } else {
// throw new UIException("Parent element does not allow a Controller to be
// defined here. Parent: " + parent);
// // LOGGER.error("Parent element does not allow a Controller to be defined
// // here.");
// // thisElem.setNextAdditionShouldOverride(true);
// // return;
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
//
// // after parsing children
// thisElem.setNextAdditionShouldOverride(true);
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseState(final Node node, final UIElement parent, final String
// fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing State");
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// if (nameAttrNode == null) {
// throw new UIException("State has no specified 'name'");
// }
// final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("name = " + name);
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIState thisElem = templateElem != null ? (UIState) templateElem : new
// UIState(name);
//
// // register with Animation/templates
// if (parent == null) {
// addTemplate(fileName, thisElem, isDevLayout);
// } else if (parent instanceof UIStateGroup) {
// final UIStateGroup p = (UIStateGroup) parent;
// p.getStates().add(thisElem);
// } else {
// throw new UIException("Parent element does not allow a State to be defined
// here. Parent: " + parent);
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
//
// // after parsing children
// thisElem.setNextAdditionShouldOverrideWhens(true);
// thisElem.setNextAdditionShouldOverrideActions(true);
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// */
// private void parseAnchor(final Node node, final UIElement parent, final
// String raceId, final boolean isDevLayout)
// throws UIException {
// logger.trace("parsing Anchor");
// if (parent instanceof UIFrame) {
// final UIFrame frame = (UIFrame) parent;
//
// final NamedNodeMap attributes = node.getAttributes();
// logger.trace("attribute length = " + attributes.getLength());
//
// final Node nodeSide = XmlDomHelper.getNamedItemIgnoringCase(attributes,
// "side");
// final Node nodeRelative = XmlDomHelper.getNamedItemIgnoringCase(attributes,
// "relative");
// final Node nodePos = XmlDomHelper.getNamedItemIgnoringCase(attributes,
// "pos");
// final Node nodeOffset = XmlDomHelper.getNamedItemIgnoringCase(attributes,
// "offset");
//
// if (nodeRelative == null) {
// throw new UIException("'Anchor' attribute does not contain required attribute
// 'relative='");
// }
// final String relative = getConstantValue(nodeRelative.getNodeValue(), raceId,
// isDevLayout);
//
// String offset = null;
// if (nodeOffset != null) {
// offset = getConstantValue(nodeOffset.getNodeValue(), raceId, isDevLayout);
// }
//
// if (nodeSide != null) {
// // version with side
// if (nodePos == null) {
// throw new UIException("'Anchor' attribute contains 'side=', but not 'pos='");
// }
// if (nodeOffset == null) {
// throw new UIException("'Anchor' attribute contains 'side=', but not
// 'offset='");
// }
// final String pos = getConstantValue(nodePos.getNodeValue(), raceId,
// isDevLayout);
// final String side = getConstantValue(nodeSide.getNodeValue(), raceId,
// isDevLayout);
// UIAnchorSide sideVal = null;
// if (side.compareToIgnoreCase(LEFT) == 0) {
// sideVal = UIAnchorSide.Left;
// } else if (side.compareToIgnoreCase(BOTTOM) == 0) {
// sideVal = UIAnchorSide.Bottom;
// } else if (side.compareToIgnoreCase(RIGHT) == 0) {
// sideVal = UIAnchorSide.Right;
// } else if (side.compareToIgnoreCase(TOP) == 0) {
// sideVal = UIAnchorSide.Top;
// } else {
// throw new UIException(
// "'Anchor' attribute has unrecognizable value for 'side='. Value is '" + side
// + "'");
// }
// frame.setAnchorPos(sideVal, pos);
// frame.setAnchorRelative(sideVal, relative);
// frame.setAnchorOffset(sideVal, offset);
// } else {
// // version without side
// if (offset == null) {
// // relative information alone is enough
// offset = ZERO_VAL;
// }
// try {
// frame.setAnchor(relative, offset);
// } catch (final NumberFormatException e) {
// throw new UIException("Could not parse an 'Anchor' attribute's 'offset='
// value as a number.");
// }
// }
//
// // verify that it cannot go deeper
// parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " +
// parent.getName() + "'", isDevLayout);
//
// } else {
// throw new UIException("Anchor attributes are not supported here. Parent: " +
// parent);
// }
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseAttribute(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Attribute");
// final String id = node.getNodeName();
// logger.trace("id = " + id);
//
// final NamedNodeMap attributes = node.getAttributes();
// final int len = attributes.getLength();
// logger.trace("attribute length = " + len);
//
// final UIAttribute thisElem = new UIAttribute(id, len);
// for (int i = 0; i < len; i++) {
// final Node curAttribute = attributes.item(i);
// final String attrKey = curAttribute.getNodeName();
// final String attrVal = getConstantValue(curAttribute.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("key,value = '" + attrKey + "', '" + attrVal + "'");
// final String overriddenVal = thisElem.addValue(attrKey, attrVal);
//
// if (overriddenVal != null) {
// logger.trace("overridden value = " + overriddenVal);
// }
// }
//
// // register with frame/animation/stategroup/state/controller
// if (parent instanceof UIFrame) {
// final UIFrame p = (UIFrame) parent;
// p.addAttribute(thisElem);
// } else if (parent instanceof UIAnimation) {
// if (id.equalsIgnoreCase(EVENT)) {
// final UIAnimation p = (UIAnimation) parent;
// // override all animation events
// if (p.isNextEventsAdditionShouldOverride()) {
// p.getEvents().clear();
// p.setNextEventsAdditionShouldOverride(false);
// }
// p.addEvent(id, thisElem);
// } else if (id.equalsIgnoreCase(DRIVER)) {
// final UIAnimation p = (UIAnimation) parent;
// p.setDriver(thisElem);
// } else {
// throw new UIException("Parent element (Frame) expects 'Event' attribute
// instead of '" + id + "'");
// }
// } else if (parent instanceof UIStateGroup) {
// if (id.compareToIgnoreCase(DEFAULTSTATE) != 0) {
// throw new UIException(
// "Parent element (Animation) expects 'DefaultState' attribute instead of '" +
// id + "'");
// }
// final UIStateGroup p = (UIStateGroup) parent;
// final String val = thisElem.getValue(VAL);
// if (val != null) {
// p.setDefaultState(val);
// } else {
// throw new UIException(
// "Parent element (StateGroup) expects this 'DefaultState' attribute to have a
// 'val' entry.");
// }
// } else if (parent instanceof UIState) {
// UIState p = null;
// switch (id.toLowerCase(Locale.ROOT)) {
// case WHEN:
// p = (UIState) parent;
// // override all state whens
// if (p.isNextAdditionShouldOverrideWhens()) {
// p.getWhens().clear();
// p.setNextAdditionShouldOverrideWhens(false);
// }
// p.getWhens().add(thisElem);
// break;
// case ACTION:
// p = (UIState) parent;
// // override all state actions
// if (p.isNextAdditionShouldOverrideActions()) {
// p.getWhens().clear();
// p.setNextAdditionShouldOverrideActions(false);
// }
// p.getActions().add(thisElem);
// break;
// default:
// throw new UIException(
// "Parent element (UIState) expects 'when' or 'action' attribute instead of '"
// + id + "'");
// }
// } else if (parent instanceof UIController) {
// if (id.compareToIgnoreCase(KEY) != 0) {
// throw new UIException("Parent element (UIController) expects 'key' attribute
// instead of '" + id + "'");
// }
// final UIController p = (UIController) parent;
// // override all controller keys
// if (p.isNextAdditionShouldOverride()) {
// p.getKeys().clear();
// p.setNextAdditionShouldOverride(false);
// }
// p.getKeys().add(thisElem);
// } else {
// if (parent != null) {
// throw new UIException("Parent element '" + parent.getName()
// + "' does not allow an Attribute to be defined here. Parent: " + parent);
// } else {
// throw new UIException("Parent element 'null' does not allow an Attribute to
// be defined here.");
// }
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
// }
//
// /**
// * @param childNodes
// * @param thisElem
// * @throws UIException
// */
// private void parseAttrChildren(final NodeList childNodes, final String
// parentName, final boolean isDevLayout)
// throws UIException {
// // no non-trash children allowed
// final int len = childNodes.getLength();
// for (int i = 0; i < len; i++) {
// final Node node = childNodes.item(i);
// if (isTrashNodeType(node)) {
// final String msg = "Found trash nodeType within Attribute '" + parentName +
// "', nodeType = '"
// + node.getNodeType() + "'";
// logger.trace(msg);
// throw new UIException(msg);
// } else {
// // TODO
// }
//
// // verify that it cannot go deeper
// parseAttrChildren(node.getChildNodes(), "'Anchor Attribute of " +
// node.getNodeName() + "'", isDevLayout);
// }
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseDesc(final Node node, final UIElement parent, final String
// fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Desc");
//
// // go deeper
// parse(node.getChildNodes(), null, fileName, raceId, isDevLayout);
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseStateGroup(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Stategroup");
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// if (nameAttrNode == null) {
// throw new UIException("Stategroup has no specified 'name'");
// }
// final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("name = " + name);
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIStateGroup thisElem = templateElem != null ? (UIStateGroup)
// templateElem : new UIStateGroup(name);
//
// // register with parent/templates
// if (parent == null) {
// addTemplate(fileName, thisElem, isDevLayout);
// } else if (parent instanceof UIFrame) {
// final UIFrame p = (UIFrame) parent;
// p.getChildren().add(thisElem);
// } else {
// throw new UIException("Parent element does not allow a Stategroup to be
// defined here. Parent: " + parent);
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseAnimation(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Animation");
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// if (nameAttrNode == null) {
// throw new UIException("Animation has no specified 'name'");
// }
// final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("name = " + name);
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIAnimation thisElem = templateElem != null ? (UIAnimation)
// templateElem : new UIAnimation(name);
// // register with parent/templates
// if (parent == null) {
// addTemplate(fileName, thisElem, isDevLayout);
// } else if (parent instanceof UIFrame) {
// final UIFrame p = (UIFrame) parent;
// p.getChildren().add(thisElem);
// } else {
// throw new UIException("Parent element does not allow an Animation to be
// defined here. Parent: " + parent);
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
//
// // after parsing children
//
// // set implicit controller names
// if (parent != null) {
// setImplicitControllerNames(thisElem);
// }
// // else {
// // // cannot set names for templates as they could be incomplete,
// // // right?
// // }
//
// thisElem.setNextEventsAdditionShouldOverride(true);
// }
//
// /**
// * @param fileName
// * @param thisElem
// * @param isDevLayout
// */
// @Override
// public void addTemplate(final String fileName, final UIElement thisElem,
// final boolean isDevLayout) {
// final List<UITemplate> list = isDevLayout ? blizzOnlyTemplates : templates;
// list.add(new UITemplate(fileName, thisElem));
// }
//
// /**
// * Set the implicit names of controllers in animations.
// *
// * @param thisElem
// * @throws UIException
// */
// private void setImplicitControllerNames(final UIAnimation thisElem) throws
// UIException {
// logger.trace("Setting implicit controller names");
// final List<UIController> controllers = thisElem.getControllers();
// for (final UIController contr : controllers) {
// if (contr.getName() == null) {
// // final String type = contr.getValues().get("type");
// final String type = contr.getValue(TYPE_ATTR);
// logger.trace("type = " + type);
// contr.setName(getImplicitName(type, controllers));
// contr.setNameIsImplicit(true);
// }
// }
// }
//
// /**
// * @param type
// * @param controllers
// * @return
// * @throws UIException
// */
// private String getImplicitName(final String type, final List<UIController>
// controllers) throws UIException {
// logger.trace("Constructing implicit controller name");
// if (type == null) {
// throw new UIException("'type=\"...\"' of Controller is not set or invalid.");
// }
//
// int i = 0;
// while (true) {
// final String name = type + "_" + i;
//
// if (controllers.stream().noneMatch(new Predicate<UIController>() {
// @Override
// public boolean test(final UIController t) {
// return t.getName() != null && t.getName().compareToIgnoreCase(name) == 0;
// }
// })) {
// logger.trace("Constructing implicit controller name: " + name);
// return name;
// }
// logger.trace("Implicit controller name existing: " + name);
// i++;
// }
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseConstant(final Node node, final UIElement parent, final
// String fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Constant");
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// if (nameAttrNode == null) {
// throw new UIException("Constant has no specified 'name'");
// }
// final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("name = " + name);
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIConstant thisElem = templateElem != null ? (UIConstant) templateElem
// : new UIConstant(name);
//
// final Node valAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), VAL);
// if (valAttrNode == null) {
// throw new UIException("Constant has no specified 'val'");
// }
// final String val = getConstantValue(valAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("val = " + val);
// thisElem.setValue(val);
//
// // register with parent/templates overriding previous constant values
// if (!isDevLayout) {
// // is general layout
// final boolean removedBlizzOnly = removeConstantFromList(name,
// blizzOnlyConstants);
// removeConstantFromList(name, constants);
// constants.add(thisElem);
// if (removedBlizzOnly) {
// logger.warn("WARNING: constant '" + name
// + "' overrides value from Blizz-only constant, so this might be fine.");
// }
// } else {
// // is blizz-only layout
// removeConstantFromList(name, blizzOnlyConstants);
// final boolean removedGeneral = removeConstantFromList(name,
// blizzOnlyConstants);
// blizzOnlyConstants.add(thisElem);
// if (removedGeneral) {
// logger.warn("WARNING: constant '" + name
// + "' from Blizz-only layout overrides a general constant, so this might be
// fine.");
// }
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
// }
//
// /**
// * @param name
// * @param listOfConstants
// * @return
// */
// private boolean removeConstantFromList(final String name, final
// List<UIConstant> listOfConstants) {
// boolean result = false;
// for (int i = listOfConstants.size() - 1; i >= 0; i--) {
// final UIConstant curConst = listOfConstants.get(i);
// if (curConst.getName().compareToIgnoreCase(name) == 0) {
// listOfConstants.remove(i);
// result = true;
// }
// }
// return result;
// }
//
// /**
// * @param node
// * @param parent
// * @throws UIException
// * @throws InterruptedException
// * @throws DOMException
// */
// private void parseFrame(final Node node, final UIElement parent, final String
// fileName, final String raceId,
// final boolean isDevLayout) throws UIException, DOMException,
// InterruptedException {
// logger.trace("parsing Frame");
// final Node nameAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME_ATTR);
// if (nameAttrNode == null) {
// throw new UIException("Frame has no specified 'name'");
// }
// final String name = getConstantValue(nameAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("name = " + name);
//
// final Node typeAttrNode =
// XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), TYPE_ATTR);
// if (typeAttrNode == null) {
// throw new UIException("Frame has no specified 'type'");
// }
// final String type = getConstantValue(typeAttrNode.getNodeValue(), raceId,
// isDevLayout);
// logger.trace("type = " + type);
//
// final String template = readTemplate(node.getAttributes());
// final UIElement templateElem = instanciateTemplate(template, name,
// isDevLayout, parent);
// final UIFrame thisElem = templateElem != null ? (UIFrame) templateElem : new
// UIFrame(name, type);
//
// // register with parent/templates
// if (parent == null) {
// logger.trace("Adding new template Frame named " + fileName + "/" +
// thisElem.getName());
// addTemplate(fileName, thisElem, isDevLayout);
// } else if (parent instanceof UIFrame) {
// final UIFrame p = (UIFrame) parent;
// p.getChildren().add(thisElem);
// } else {
// throw new UIException("Parent element does not allow a Frame to be defined
// here. Parent: " + parent);
// }
//
// // go deeper
// parse(node.getChildNodes(), thisElem, fileName, raceId, isDevLayout);
// }
//
// /**
// * @param template
// * @return Template instance
// * @throws UIException
// */
// private UIElement instanciateTemplate(String path, final String newName,
// final boolean isDevLayout,
// final UIElement parent) throws UIException {
// if (path == null) {
// return null;
// }
// logger.trace("Instanciating Template of path " + path);
// path = path.replace('\\', '/');
// final String fileName = path.substring(0, path.indexOf('/'));
//
// // 1. check templates
// UIElement templateInstance = instanciateTemplateFromList(templates, fileName,
// path, newName);
// if (templateInstance != null) {
// return templateInstance;
// } else {
// // 2. if fail -> check dev templates
// templateInstance = instanciateTemplateFromList(blizzOnlyTemplates, fileName,
// path, newName);
// if (templateInstance != null) {
// if (!isDevLayout) {
// logger.error("ERROR: the non-Blizz-only frame '" + parent + "' uses a
// Blizz-only template '" + path
// + "'.");
// }
// return templateInstance;
// }
// }
// // template does not exist or its layout was not loaded, yet
// if (!isDevLayout) {
// logger.error("ERROR: Template of path '" + path + "' could not be found.");
// } else {
// logger.warn("WARNING: Template of path '" + path
// + "' could not be found, but we are creating a Blizz-only layout, so this is
// fine.");
// }
// return null;
// }
//
// /**
// * @param templates
// * @param fileName
// * @param path
// * @param newName
// * @return
// */
// private UIElement instanciateTemplateFromList(final List<UITemplate>
// templates, final String fileName,
// final String path, final String newName) {
//
// for (final UITemplate curTemplate : templates) {
// if (curTemplate.getFileName().equalsIgnoreCase(fileName)) {
// // found template file
// final String newPath = UIElement.removeLeftPathLevel(path);
// final UIElement frameFromPath = curTemplate.receiveFrameFromPath(newPath);
//
// if (frameFromPath == null) {
// // not the correct template
// continue;
// }
// final UIElement clone = (UIElement) frameFromPath.deepCopy();
// clone.setName(newName);
// return clone;
// }
// }
// return null;
// }
//
// /**
// * Reads a template if it is existing.
// *
// * @param attributes
// * the attributes
// * @return template reference or null
// */
// private String readTemplate(final NamedNodeMap attributes) {
// final Node template = attributes.getNamedItem(TEMPLATE2);
// return template != null ? template.getNodeValue() : null;
// }
//
// @Override
// public String getConstantValue(final String constantRef, final String raceId,
// final boolean isDevLayout) {
// // String id = constantRef;
//
// int i = 0;
// if (constantRef.length() > 0) {
// while (constantRef.charAt(i) == '#') {
// i++;
// }
// }
// // no constant tag
// if (i <= 0) {
// return constantRef;
// }
// final String prefix = constantRef.substring(0, i);
// final String constantName = constantRef.substring(i);
// logger.trace("Encountered Constant: prefix='" + prefix + "', constantName='"
// + constantName + "'");
// for (final UIConstant c : constants) {
// if (c.getName().equalsIgnoreCase(constantName)) {
// return c.getValue();
// }
// }
// // constant tag with race suffix
// if (i == 2) {
// final String constantNameWithRacePostFix = constantName + "_" + raceId;
// for (final UIConstant c : constants) {
// if (c.getName().equalsIgnoreCase(constantNameWithRacePostFix)) {
// return c.getValue();
// }
// }
// }
// if (i >= 3) {
// logger.error("ERROR: Encountered a constant definition with three #'" +
// constantRef
// + "' when its maximum is two '#'.");
// }
//
// if (!isDevLayout) {
// logger.warn("WARNING: Did not find a constant definition for '" + constantRef
// + "', so '" + constantName
// + "' is used instead.");
// } else {
// // inside blizz-only
// for (final UIConstant c : blizzOnlyConstants) {
// if (c.getName().equalsIgnoreCase(constantName)) {
// return c.getValue();
// }
// }
// logger.warn("WARNING: Did not find a constant definition for '" + constantRef
// + "', but it is a Blizz-only layout, so this is fine.");
// }
// return constantName;
// }
//
// @Override
// public List<UITemplate> getTemplates() {
// return templates;
// }
//
// @Override
// public void setTemplates(final List<UITemplate> templates) {
// this.templates = templates;
// }
//
// @Override
// public List<UITemplate> getBlizzOnlyTemplates() {
// return blizzOnlyTemplates;
// }
//
// @Override
// public void setBlizzOnlyTemplates(final List<UITemplate> blizzOnlyTemplates)
// {
// this.blizzOnlyTemplates = blizzOnlyTemplates;
// }
//
// @Override
// public List<UIConstant> getConstants() {
// return constants;
// }
//
// @Override
// public void setConstants(final List<UIConstant> constants) {
// this.constants = constants;
// }
//
// @Override
// public List<UIConstant> getBlizzOnlyConstants() {
// return blizzOnlyConstants;
// }
//
// @Override
// public void setBlizzOnlyConstants(final List<UIConstant> blizzOnlyConstants)
// {
// this.blizzOnlyConstants = blizzOnlyConstants;
// }
//
// @Override
// public List<String> getDevLayouts() {
// return blizzOnlyLayouts;
// }
//
// @Override
// public void setDevLayouts(final List<String> devLayouts) {
// blizzOnlyLayouts = devLayouts;
// }
//
// @Override
// public String getCurBasePath() {
// return curBasePath;
// }
//
// /**
// * @param curBasePath
// * the curBasePath to set
// */
// public void setCurBasePath(final String curBasePath) {
// this.curBasePath = curBasePath;
// }
//
// public void printDebugStats() {
// logger.info("UICatalogSizes: " + templates.size() + " " +
// blizzOnlyTemplates.size() + " " + constants.size()
// + " " + blizzOnlyConstants.size() + " " + blizzOnlyLayouts.size());
// // int count = 0;
// // for(int i = 0; i < templates.size(); i++) {
// // UIElement elem = templates.get(i).getElement();
// // if(elem instanceof UIFrame) {
// // UIFrame frame = (UIFrame) elem;
// // frame.getChildren();
// // }
// // }
// }
// }
