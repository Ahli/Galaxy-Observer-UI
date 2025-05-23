package com.ahli.hotkey_ui.application.galaxy.ext;

import com.ahli.hotkey_ui.application.model.OptionValueDef;
import com.ahli.hotkey_ui.application.model.TextValueDef;
import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import com.ahli.xml.XmlDomHelper;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

@Slf4j
final class LayoutFileUpdater extends SimpleFileVisitor<Path> {
	private static final String NAME = "name";
	private static final String VAL = "val";
	private static final String CONSTANT = "constant";
	private final DocumentBuilder dBuilder;
	private final Transformer transformer;
	private final String[] fileExtensions;
	private final List<TextValueDef> hotkeys;
	private final List<ValueDef> settings;
	
	LayoutFileUpdater(
			final DocumentBuilder dBuilder,
			final Transformer transformer,
			final String[] fileExtensions,
			final List<TextValueDef> hotkeys,
			final List<ValueDef> settings) {
		this.dBuilder = dBuilder;
		this.transformer = transformer;
		this.fileExtensions = fileExtensions;
		this.hotkeys = hotkeys;
		this.settings = settings;
	}
	
	private static void modifyConstants(
			final NodeList childNodes, final List<TextValueDef> hotkeys, final List<ValueDef> settings) {
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
			final NodeList childNodes, final List<TextValueDef> hotkeys, final List<ValueDef> settings) {
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
	private static void processConstant(
			final Node node, final List<TextValueDef> hotkeys, final List<ValueDef> settings) {
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME);
		if (nameAttrNode != null) {
			final String name = nameAttrNode.getNodeValue();
			final Node valAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), VAL);
			if (valAttrNode != null) {
				final String val = valAttrNode.getNodeValue();
				log.debug("Constant: name = {}, val = {}", name, val);
				setValueDefCurValue(name, val, hotkeys, settings);
			} else {
				log.warn("Constant '{}' has no 'val' attribute defined.", name);
			}
		} else {
			log.warn("Constant has no 'name' attribute defined.");
		}
	}
	
	/**
	 * @param name
	 * @param val
	 * @param hotkeys
	 * @param settings
	 */
	private static void setValueDefCurValue(
			final String name, final String val, final List<TextValueDef> hotkeys, final List<ValueDef> settings) {
		for (final TextValueDef item : hotkeys) {
			if (item.getId().equalsIgnoreCase(name)) {
				item.setValue(val);
				item.setOldValue(val);
				return;
			}
		}
		for (final ValueDef item : settings) {
			if (item.getId().equalsIgnoreCase(name)) {
				if (item instanceof TextValueDef tvd) {
					tvd.setValue(val);
					tvd.setOldValue(val);
					return;
				} else if (item instanceof OptionValueDef ovd) {
					ovd.setSelectedValue(val);
					ovd.setOldSelectedValue(val);
					return;
				}
			}
		}
		log.debug("no ValueDef found with name: {}", name);
	}
	
	/**
	 * Modify a Constant node from XML with data's current value.
	 *
	 * @param node
	 * @param hotkeys
	 * @param settings
	 */
	private static void modifyConstant(
			final Node node, final List<TextValueDef> hotkeys, final List<ValueDef> settings) {
		final Node nameAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), NAME);
		if (nameAttrNode != null) {
			final String name = nameAttrNode.getNodeValue();
			final Node valAttrNode = XmlDomHelper.getNamedItemIgnoringCase(node.getAttributes(), VAL);
			if (valAttrNode != null) {
				final String val = valAttrNode.getNodeValue();
				
				for (final TextValueDef item : hotkeys) {
					if (item.getId().equalsIgnoreCase(name)) {
						final String itemVal = item.getValue();
						if (!Objects.equals(itemVal, val)) {
							log.debug("updating hotkey constant: '{}' with val: '{}' from '{}'", name, itemVal, val);
							valAttrNode.setNodeValue(itemVal);
						}
						return;
					}
				}
				for (final ValueDef item : settings) {
					if (item.getId().equalsIgnoreCase(name)) {
						final String itemVal = item.getValue();
						if (!Objects.equals(itemVal, val)) {
							log.debug("updating setting constant: '{}' with val: '{}' from '{}'", name, itemVal, val);
							valAttrNode.setNodeValue(itemVal);
						}
						return;
					}
				}
			} else {
				log.warn("Constant has no 'val' attribute defined.");
			}
		} else {
			log.warn("Constant has no 'name' attribute defined.");
		}
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
					final String msg = "Error parsing file.";
					log.trace(msg, e);
					throw new IOException(msg, e);
				}
				
				log.debug("processing file: {}", file);
				
				// process files
				final Element elem = doc.getDocumentElement();
				final NodeList childNodes = elem.getChildNodes();
				modifyConstants(childNodes, hotkeys, settings);
				
				// write DOM back to XML
				try {
					transformer.transform(new DOMSource(doc), new StreamResult(file.toFile()));
				} catch (final TransformerException e) {
					log.error("Transforming to generate XML file failed.", e);
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
		log.error("Failed to access file: {}", file);
		throw exc;
	}
}
