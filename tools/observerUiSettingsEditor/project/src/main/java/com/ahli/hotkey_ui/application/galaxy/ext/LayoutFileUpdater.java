package com.ahli.hotkey_ui.application.galaxy.ext;

import com.ahli.hotkey_ui.application.model.ValueDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class LayoutFileUpdater extends SimpleFileVisitor<Path> {
	public static final String ERROR_PARSING_FILE = "Error parsing file.";
	private static final String NAME = "name";
	private static final String VAL = "val";
	private static final String CONSTANT = "constant";
	private static final Logger logger = LoggerFactory.getLogger(LayoutFileUpdater.class);
	private final DocumentBuilder dBuilder;
	private final Transformer transformer;
	private final String[] fileExtensions;
	private final List<ValueDef> hotkeys;
	private final List<ValueDef> settings;
	
	LayoutFileUpdater(
			final DocumentBuilder dBuilder,
			final Transformer transformer,
			final String[] fileExtensions,
			final List<ValueDef> hotkeys,
			final List<ValueDef> settings) {
		this.dBuilder = dBuilder;
		this.transformer = transformer;
		this.fileExtensions = fileExtensions;
		this.hotkeys = hotkeys;
		this.settings = settings;
	}
	
	private static void modifyConstants(
			final NodeList childNodes, final List<ValueDef> hotkeys, final List<ValueDef> settings) {
		for (int i = 0, len = childNodes.getLength(); i < len; ++i) {
			final Node curNode = childNodes.item(i);
			
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				
				final String nodeName = curNode.getNodeName().toLowerCase(Locale.ROOT);
				if (nodeName.equals(CONSTANT)) {
					modifyConstant(curNode, hotkeys, settings);
				}
				
			} else {
				readConstants(curNode.getChildNodes(), hotkeys, settings);
			}
		}
	}
	
	/**
	 * Processes the Constants in the given Nodes.
	 *
	 * @param childNodes
	 * @param hotkeys
	 * @param settings
	 */
	public static void readConstants(
			final NodeList childNodes, final List<ValueDef> hotkeys, final List<ValueDef> settings) {
		for (int i = 0, len = childNodes.getLength(); i < len; ++i) {
			final Node curNode = childNodes.item(i);
			
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				
				final String nodeName = curNode.getNodeName().toLowerCase(Locale.ROOT);
				if (nodeName.equals(CONSTANT)) {
					processConstant(curNode, hotkeys, settings);
				}
				
			} else {
				readConstants(curNode.getChildNodes(), hotkeys, settings);
			}
		}
	}
	
	/**
	 * @param node
	 * @param hotkeys
	 * @param settings
	 */
	private static void processConstant(final Node node, final List<ValueDef> hotkeys, final List<ValueDef> settings) {
		final Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), NAME);
		if (nameAttrNode != null) {
			final String name = nameAttrNode.getNodeValue();
			final Node valAttrNode = getNamedItemIgnoreCase(node.getAttributes(), VAL);
			if (valAttrNode != null) {
				final String val = valAttrNode.getNodeValue();
				logger.debug("Constant: name = {}, val = {}", name, val);
				setValueDefCurValue(name, val, hotkeys, settings);
			} else {
				logger.warn("Constant '{}' has no 'val' attribute defined.", name);
			}
		} else {
			logger.warn("Constant has no 'name' attribute defined.");
		}
	}
	
	/**
	 * @param name
	 * @param val
	 * @param hotkeys
	 * @param settings
	 */
	private static void setValueDefCurValue(
			final String name, final String val, final List<ValueDef> hotkeys, final List<ValueDef> settings) {
		for (final ValueDef item : hotkeys) {
			if (item.getId().equalsIgnoreCase(name)) {
				item.setValue(val);
				item.setOldValue(val);
				return;
			}
		}
		for (final ValueDef item : settings) {
			if (item.getId().equalsIgnoreCase(name)) {
				item.setValue(val);
				item.setOldValue(val);
				return;
			}
		}
		logger.debug("no ValueDef found with name: {}", name);
	}
	
	/**
	 * Modify a Constant node from XML with data's current value.
	 *
	 * @param node
	 * @param hotkeys
	 * @param settings
	 */
	private static void modifyConstant(
			final Node node, final List<ValueDef> hotkeys, final List<ValueDef> settings) {
		final Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), NAME);
		if (nameAttrNode != null) {
			final String name = nameAttrNode.getNodeValue();
			final Node valAttrNode = getNamedItemIgnoreCase(node.getAttributes(), VAL);
			if (valAttrNode != null) {
				final String val = valAttrNode.getNodeValue();
				
				for (final ValueDef item : hotkeys) {
					if (item.getId().equalsIgnoreCase(name)) {
						final String itemVal = item.getValue();
						if (!Objects.equals(itemVal, val)) {
							logger.debug("updating hotkey constant: '{}' with val: '{}' from '{}'", name, itemVal, val);
							valAttrNode.setNodeValue(itemVal);
						}
						return;
					}
				}
				for (final ValueDef item : settings) {
					if (item.getId().equalsIgnoreCase(name)) {
						if (!Objects.equals(item.getValue(), val)) {
							final String itemVal = item.getValue();
							logger.debug("updating setting constant: '{}' with val: '{}' from '{}'",
									name,
									itemVal,
									val);
							valAttrNode.setNodeValue(itemVal);
						}
						return;
					}
				}
			} else {
				logger.warn("Constant has no 'val' attribute defined.");
			}
		} else {
			logger.warn("Constant has no 'name' attribute defined.");
		}
	}
	
	/**
	 * @param nodes
	 * @param name
	 * @return
	 */
	private static Node getNamedItemIgnoreCase(final NamedNodeMap nodes, final String name) {
		final Node node = nodes.getNamedItem(name);
		if (node == null) {
			for (int i = 0, len = nodes.getLength(); i < len; ++i) {
				final Node curNode = nodes.item(i);
				if (name.equalsIgnoreCase(curNode.getNodeName())) {
					return curNode;
				}
			}
		}
		return node;
	}
	
	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		final String fileName = file.getFileName().toString();
		final int i = fileName.lastIndexOf('.');
		if (i >= 0) {
			final String curExt = fileName.substring(i + 1);
			boolean found = false;
			for (final String ext : fileExtensions) {
				if (ext.equalsIgnoreCase(curExt)) {
					found = true;
					break;
				}
			}
			if (found) {
				final Document doc;
				try (final InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
					doc = dBuilder.parse(is);
				} catch (final SAXException e) {
					logger.trace(ERROR_PARSING_FILE, e);
					throw new IOException(ERROR_PARSING_FILE, e);
				}
				
				logger.debug("processing file: {}", file);
				
				// process files
				final Element elem = doc.getDocumentElement();
				final NodeList childNodes = elem.getChildNodes();
				modifyConstants(childNodes, hotkeys, settings);
				
				// write DOM back to XML
				try {
					transformer.transform(new DOMSource(doc), new StreamResult(file.toFile()));
				} catch (final TransformerException e) {
					logger.error("Transforming to generate XML file failed.", e);
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
		logger.error("Failed to access file: {}", file);
		throw exc;
	}
}
