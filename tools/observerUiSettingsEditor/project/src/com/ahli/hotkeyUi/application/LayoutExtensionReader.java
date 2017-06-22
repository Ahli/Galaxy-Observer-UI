package com.ahli.hotkeyUi.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ahli.hotkeyUi.application.model.ValueDef;
import com.ahli.util.SilentXmlSaxErrorHandler;

public class LayoutExtensionReader {
	static Logger LOGGER = LogManager.getLogger("LayoutExtensionReader");

	private ArrayList<ValueDef> hotkeys = new ArrayList<>();
	private ArrayList<ValueDef> settings = new ArrayList<>();

	/**
	 * @return the hotkeys
	 */
	public ArrayList<ValueDef> getHotkeys() {
		return hotkeys;
	}

	/**
	 * @param hotkeys
	 *            the hotkeys to set
	 */
	public void setHotkeys(ArrayList<ValueDef> hotkeys) {
		this.hotkeys = hotkeys;
	}

	/**
	 * @return the settings
	 */
	public ArrayList<ValueDef> getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(ArrayList<ValueDef> settings) {
		this.settings = settings;
	}

	/**
	 * Processes the specified Layout files. It finds hotkeys and setting
	 * definitions.
	 * 
	 * @param layoutFiles
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void processLayoutFiles(Collection<File> layoutFiles)
			throws ParserConfigurationException, SAXException, IOException {

		LOGGER.info("Scanning for XML file...");

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		// provide error handler that does not print incompatible files into
		// console
		dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());

		InputStream is = null;

		x: for (File curFile : layoutFiles) {
			Document doc = null;
			try {
				// parse XML file

				// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
				// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
				// doc = dBuilder.parse(curFile);

				// WORKAROUND -> provide Inputstream
				is = new FileInputStream(curFile);
				doc = dBuilder.parse(is);

			} catch (SAXParseException | IOException e) {
				// couldn't parse, most likely no XML file
				if (is != null) {
					is.close();
				}
				continue x;
			} finally {
				if (is != null) {
					is.close();
				}
			}

			LOGGER.debug("comments - processing file: " + curFile.getPath());

			// read comments
			Element elem = doc.getDocumentElement();
			NodeList childNodes = elem.getChildNodes();
			readComments(childNodes);
		}

		is = null;

		x: for (File curFile : layoutFiles) {
			Document doc = null;
			try {
				// parse XML file

				// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
				// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
				// doc = dBuilder.parse(curFile);

				// WORKAROUND -> provide Inputstream
				is = new FileInputStream(curFile);
				doc = dBuilder.parse(is);

			} catch (SAXParseException | IOException e) {
				// couldn't parse, most likely no XML file
				if (is != null) {
					is.close();
				}
				continue x;
			} finally {
				if (is != null) {
					is.close();
				}
			}

			LOGGER.debug("constants - processing file: " + curFile.getPath());

			// read constants
			Element elem = doc.getDocumentElement();
			NodeList childNodes = elem.getChildNodes();
			readConstants(childNodes);
		}
	}

	/**
	 * Processes the Comments in the given Nodes.
	 * 
	 * @param childNodes
	 */
	private void readComments(NodeList childNodes) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node curNode = childNodes.item(i);

			if (curNode.getNodeType() == Node.COMMENT_NODE) {

				Comment comment = (Comment) curNode;
				String text = comment.getData();
				processCommentText(text);

			} else {
				readComments(curNode.getChildNodes());
			}
		}
	}

	/**
	 * Creates ValueDef for the Hotkey and Setting definitions found in this
	 * comment
	 * 
	 * @param textInput
	 */
	public void processCommentText(String textInput) {
		String constant = "", description = "", defaultValue = "";

		// String textInputLower = textInput.toLowerCase();
		LOGGER.debug("textInput:" + textInput);
		try {
			// split at keywords @hotkey or @setting without removing, case
			// insensitive
			for (String text : textInput.split("(?=@hotkey|@setting)/i")) {
				LOGGER.debug("token start:" + text);
				text = text.trim();

				constant = "";
				description = "";
				defaultValue = "";

				boolean isHotkey = text.toLowerCase(Locale.ROOT).startsWith("@hotkey");
				boolean isSetting = text.toLowerCase(Locale.ROOT).startsWith("@setting");

				if (isHotkey || isSetting) {
					LOGGER.debug("detected hotkey or setting");
					// move behind keyword
					int pos = (isHotkey) ? "@hotkey".length() : "@setting".length();
					String toProcess = text.substring(pos);

					// move beyond '('
					toProcess = toProcess.substring(1 + toProcess.indexOf('(')).trim();

					// split at keyword
					for (String part : toProcess.split("(?i)(?=(constant|default|description)[\\s]*=)")) {
						part = part.trim();
						String partLower = part.toLowerCase(Locale.ROOT);
						LOGGER.debug("part: " + part);
						if (partLower.startsWith("constant")) {
							// move beyond '='
							part = part.substring(1 + part.indexOf("=")).trim();
							constant = part.substring(part.indexOf('"') + 1, part.lastIndexOf('"'));
							LOGGER.debug("constant = " + constant);
						} else if (partLower.startsWith("default")) {
							// move beyond '='
							part = part.substring(1 + part.indexOf("=")).trim();
							defaultValue = part.substring(part.indexOf('"') + 1, part.lastIndexOf('"'));
							LOGGER.debug("default = " + defaultValue);
						} else if (partLower.startsWith("description")) {
							// move beyond '='
							part = part.substring(1 + part.indexOf("=")).trim();
							description = part.substring(part.indexOf('"') + 1, part.lastIndexOf('"'));
							LOGGER.debug("description = " + description);
						}
					}

					if (!constant.equals("")) {
						if (isHotkey) {
							addHotkeyValueDef(constant, description, defaultValue, "");
						} else {
							addSettingValueDef(constant, description, defaultValue, "");
						}
					}
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.debug("Parsing Comment failed.", e);
		}
	}

	public void addHotkeyValueDef(String constant, String description, String defaultValue, String curValue) {
		ValueDef def = new ValueDef(constant, curValue, description, defaultValue);
		hotkeys.add(def);
	}

	public void addSettingValueDef(String constant, String description, String defaultValue, String curValue) {
		ValueDef def = new ValueDef(constant, curValue, description, defaultValue);
		settings.add(def);
	}

	/**
	 * Processes the Constants in the given Nodes.
	 * 
	 * @param childNodes
	 */
	private void readConstants(NodeList childNodes) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node curNode = childNodes.item(i);

			if (curNode.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = curNode.getNodeName().toLowerCase(Locale.ROOT);
				if (nodeName.equals("constant")) {
					processConstant(curNode);
				}

			} else {
				readConstants(curNode.getChildNodes());
			}
		}
	}

	/**
	 * 
	 * @param node
	 */
	public void processConstant(Node node) {
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		String name = nameAttrNode.getNodeValue();
		Node valAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "val");
		String val = valAttrNode.getNodeValue();
		LOGGER.debug("Constant: name = " + name + ", val = " + val);

		setValueDefCurValue(name, val);
	}

	/**
	 * 
	 * @param name
	 * @param val
	 */
	private void setValueDefCurValue(String name, String val) {
		for (ValueDef item : hotkeys) {
			if (item.getId().equalsIgnoreCase(name)) {
				item.setValue(val);
				return;
			}
		}
		for (ValueDef item : settings) {
			if (item.getId().equalsIgnoreCase(name)) {
				item.setValue(val);
				return;
			}
		}
		LOGGER.debug("no ValueDef found with name: " + name);
	}

	/**
	 * 
	 * @param layoutFiles
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void updateLayoutFiles(Collection<File> layoutFiles)
			throws ParserConfigurationException, SAXException, IOException {
		LOGGER.info("Scanning for XML file...");

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		// provide error handler that does not print incompatible files into
		// console
		dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());

		InputStream is = null;

		x: for (File curFile : layoutFiles) {
			Document doc = null;
			try {
				// parse XML file

				// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
				// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
				// doc = dBuilder.parse(curFile);

				// WORKAROUND -> provide Inputstream
				is = new FileInputStream(curFile);
				doc = dBuilder.parse(is);

			} catch (SAXParseException | IOException e) {
				// couldn't parse, most likely no XML file
				if (is != null) {
					is.close();
				}
				continue x;
			} finally {
				if (is != null) {
					is.close();
				}
			}

			LOGGER.debug("processing file: " + curFile.getPath());

			// process files
			Element elem = doc.getDocumentElement();
			NodeList childNodes = elem.getChildNodes();
			modifyConstants(childNodes);

			// write DOM back to XML
			Source source = new DOMSource(doc);
			Result result = new StreamResult(curFile);
			Transformer xformer;
			try {
				xformer = TransformerFactory.newInstance().newTransformer();
				xformer.transform(source, result);
			} catch (TransformerFactoryConfigurationError | TransformerException e) {
				LOGGER.error("Transforming to generate XML file failed.", e);
				e.printStackTrace();
			}
		}

	}

	private void modifyConstants(NodeList childNodes) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node curNode = childNodes.item(i);

			if (curNode.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = curNode.getNodeName().toLowerCase(Locale.ROOT);
				if (nodeName.equals("constant")) {
					modifyConstant(curNode);
				}

			} else {
				readConstants(curNode.getChildNodes());
			}
		}
	}

	/**
	 * Modify a Constant node from XML with data's current value.
	 * 
	 * @param node
	 */
	private void modifyConstant(Node node) {
		Node nameAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "name");
		String name = nameAttrNode.getNodeValue();
		Node valAttrNode = getNamedItemIgnoreCase(node.getAttributes(), "val");
		String val = valAttrNode.getNodeValue();

		for (ValueDef item : hotkeys) {
			if (item.getId().equalsIgnoreCase(name)) {
				LOGGER.debug("updating hotkey constant: " + name + ", with val: " + val);
				valAttrNode.setNodeValue(item.getValue());
			}
		}
		for (ValueDef item : settings) {
			if (item.getId().equalsIgnoreCase(name)) {
				LOGGER.debug("updating setting constant:" + name + ", with val: " + val);
				valAttrNode.setNodeValue(item.getValue());
			}
		}
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

	/**
	 * Clears stored data.
	 */
	public void clearData() {
		hotkeys.clear();
		settings.clear();
	}
}
