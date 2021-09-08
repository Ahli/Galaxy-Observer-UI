// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.galaxy.ext;

import com.ahli.galaxy.game.GameDef;
import com.ahli.hotkey_ui.application.model.ValueDef;
import com.ahli.util.XmlDomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LayoutExtensionReader {
	private static final String ATTRIBUTE_CONSTANT = "constant";
	private static final String ATTRIBUTE_DEFAULT = "default";
	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_VALUES = "values";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_GAMESTRINGS_ADD = "gamestrings_add";
	private static final String HOTKEY = "@hotkey";
	private static final String SETTING = "@setting";
	private static final Logger logger = LogManager.getLogger(LayoutExtensionReader.class);
	private static final Pattern HOTKEY_SETTING_REGEX_PATTERN = Pattern.compile("(?<=@hotkey|@setting)/i");
	private static final Pattern ATTRIBUTES_REGEX_PATTERN =
			Pattern.compile("(?i)(?=(?:constant|default|description|values|type|gamestrings_add)[\\s]*=)");
	private List<ValueDef> hotkeys = new ArrayList<>();
	private List<ValueDef> settings = new ArrayList<>();
	
	
	private static String getValueAfterEqualsChar(final String part) {
		return part.substring(1 + part.indexOf('=')).trim();
	}
	
	private static String getValueWithinQuotes(final String part) {
		final int quoteEnd = part.lastIndexOf('"');
		final int quoteStart = part.indexOf('"');
		if (quoteStart < 0 || quoteStart >= quoteEnd) {
			return part;
		}
		return part.substring(quoteStart + 1, quoteEnd);
	}
	
	
	/**
	 * @return the hotkeys
	 */
	public List<ValueDef> getHotkeys() {
		return hotkeys;
	}
	
	/**
	 * @param hotkeys
	 * 		the hotkeys to set
	 */
	public void setHotkeys(final List<ValueDef> hotkeys) {
		this.hotkeys = hotkeys;
	}
	
	/**
	 * @return the settings
	 */
	public List<ValueDef> getSettings() {
		return settings;
	}
	
	/**
	 * @param settings
	 * 		the settings to set
	 */
	public void setSettings(final List<ValueDef> settings) {
		this.settings = settings;
	}
	
	/**
	 * Processes the specified Layout files. It finds hotkeys and setting definitions.
	 *
	 * @param layoutFiles
	 * 		the layout files
	 * @throws ParserConfigurationException
	 * 		if the XML parser configuration was invalid
	 * @throws SAXException
	 * 		if there was a parse error
	 */
	public void processLayoutFiles(final List<Path> layoutFiles) throws ParserConfigurationException, SAXException {
		logger.info("Scanning for XML file...");
		
		final DocumentBuilder dBuilder = XmlDomHelper.buildSecureDocumentBuilder(true, false);
		
		Document doc;
		for (final Path path : layoutFiles) {
			try {
				doc = dBuilder.parse(path.toString());
			} catch (final SAXParseException | IOException e) {
				logger.trace(LayoutFileUpdater.ERROR_PARSING_FILE, e);
				// couldn't parse, most likely no XML file
				continue;
			}
			
			logger.debug("comments - processing file: {}", path);
			
			// read comments
			final Element elem = doc.getDocumentElement();
			final NodeList childNodes = elem.getChildNodes();
			readComments(childNodes);
		}
		
		for (final Path path : layoutFiles) {
			try {
				// parse XML file
				doc = dBuilder.parse(path.toString());
			} catch (final SAXParseException | IOException e) {
				logger.trace(LayoutFileUpdater.ERROR_PARSING_FILE, e);
				// couldn't parse, most likely no XML file
				continue;
			}
			
			logger.debug("constants - processing file: {}", path);
			
			// read constants
			final Element elem = doc.getDocumentElement();
			final NodeList childNodes = elem.getChildNodes();
			LayoutFileUpdater.readConstants(childNodes, hotkeys, settings);
		}
	}
	
	/**
	 * Processes the Comments in the given Nodes.
	 *
	 * @param childNodes
	 */
	private void readComments(final NodeList childNodes) {
		for (int i = 0, len = childNodes.getLength(); i < len; ++i) {
			final Node curNode = childNodes.item(i);
			
			if (curNode.getNodeType() == Node.COMMENT_NODE) {
				
				final Comment comment = (Comment) curNode;
				final String text = comment.getData();
				processCommentText(text);
				
			} else {
				readComments(curNode.getChildNodes());
			}
		}
	}
	
	/**
	 * Creates ValueDef for the Hotkey and Setting definitions found in this comment
	 *
	 * @param textInput
	 */
	public void processCommentText(final String textInput) {
		logger.debug("textInput:{}", textInput);
		try {
			// split at keywords @hotkey or @setting without removing, case insensitive
			for (String text : HOTKEY_SETTING_REGEX_PATTERN.split(textInput)) {
				logger.debug("token start:{}", text);
				text = text.trim();
				
				final String lowerCaseText = text.toLowerCase(Locale.ROOT);
				final boolean isHotkey = lowerCaseText.startsWith(HOTKEY);
				if (isHotkey || lowerCaseText.startsWith(SETTING)) {
					logger.debug("detected hotkey or setting");
					
					String constant = "";
					String description = "";
					String defaultValue = "";
					String type = "";
					String gamestringsAdd = "";
					String[] allowedValues = null;
					
					// move behind keyword
					final int pos = isHotkey ? HOTKEY.length() : SETTING.length();
					String toProcess = text.substring(pos);
					
					// move beyond '('
					toProcess = toProcess.substring(1 + toProcess.indexOf('(')).trim();
					
					// split at keyword
					for (String part : ATTRIBUTES_REGEX_PATTERN.split(toProcess)) {
						part = part.trim();
						logger.trace("part: {}", part);
						final String partLower = part.toLowerCase(Locale.ROOT);
						part = getValueAfterEqualsChar(part);
						if (partLower.startsWith(ATTRIBUTE_CONSTANT)) {
							constant = getValueWithinQuotes(part);
							logger.trace("constant = {}", constant);
						} else if (partLower.startsWith(ATTRIBUTE_DEFAULT)) {
							defaultValue = getValueWithinQuotes(part);
							logger.trace("default = {}", defaultValue);
						} else if (partLower.startsWith(ATTRIBUTE_DESCRIPTION)) {
							description = getValueWithinQuotes(part);
							logger.trace("description = {}", description);
						} else if (partLower.startsWith(ATTRIBUTE_VALUES)) {
							allowedValues = part.split("/");
							for (int i = 0; i < allowedValues.length; ++i) {
								allowedValues[i] = getValueWithinQuotes(allowedValues[i]);
							}
							logger.trace("values = {}", part);
						} else if (partLower.startsWith(ATTRIBUTE_TYPE)) {
							type = getValueWithinQuotes(part).trim();
							logger.trace("type = {}", part);
						} else if (partLower.startsWith(ATTRIBUTE_GAMESTRINGS_ADD)) {
							gamestringsAdd = getValueWithinQuotes(part).trim();
							logger.trace("gamestrings_add = {}", part);
						}
					}
					
					if (isHotkey) {
						addHotkeyValueDef(constant, description, defaultValue);
					} else {
						settings.add(new ValueDef(constant,
								description,
								defaultValue,
								type,
								allowedValues,
								gamestringsAdd));
					}
				}
			}
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			logger.trace("Parsing Comment failed.", e);
		}
	}
	
	private void addHotkeyValueDef(final String constant, final String description, final String defaultValue) {
		hotkeys.add(new ValueDef(constant, description, defaultValue));
	}
	
	/**
	 * @param projectInCache
	 * @return true if changes were present
	 * @throws ParserConfigurationException
	 * @throws TransformerConfigurationException
	 */
	public boolean updateLayoutFiles(final Path projectInCache)
			throws ParserConfigurationException, TransformerConfigurationException {
		
		final List<ValueDef> changedHotkeys = hotkeys.stream().filter(ValueDef::hasChanged).toList();
		final List<ValueDef> changedSettings = settings.stream().filter(ValueDef::hasChanged).toList();
		
		if (!changedHotkeys.isEmpty() || !changedSettings.isEmpty()) {
			
			logger.info("Scanning for XML files...");
			final DocumentBuilder dBuilder = XmlDomHelper.buildSecureDocumentBuilder(true, false);
			final Transformer transformer = XmlDomHelper.buildSecureTransformer();
			
			try {
				final String[] extensions = new String[] { GameDef.buildSc2GameDef().layoutFileEnding(),
						GameDef.buildHeroesGameDef().layoutFileEnding() };
				
				final FileVisitor<Path> visitor =
						new LayoutFileUpdater(dBuilder, transformer, extensions, changedHotkeys, changedSettings);
				
				Files.walkFileTree(projectInCache, visitor);
			} catch (final IOException e) {
				logger.error("Transforming to generate XML file failed.", e);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Clears stored data.
	 */
	public void clearData() {
		hotkeys.clear();
		settings.clear();
	}
	
	/**
	 * @param projectInCache
	 * @return true if changes were present
	 */
	public boolean updateGameStrings(final Path projectInCache) {
		final List<ValueDef> gamestringsAddSettings = new ArrayList<>();
		for (final ValueDef setting : settings) {
			if (setting.hasChanged() && !setting.getGamestringsAdd().isEmpty()) {
				gamestringsAddSettings.add(setting);
			}
		}
		if (!gamestringsAddSettings.isEmpty()) {
			logger.info("Scanning for GameStrings.txt files...");
			try {
				final FileVisitor<Path> visitor = new GameStringsUpdater(gamestringsAddSettings);
				
				Files.walkFileTree(projectInCache, visitor);
			} catch (final IOException e) {
				logger.error("Editing GameStrings.txt files failed..", e);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if there are value changes compared to the last loading/saving
	 */
	public boolean hasChanges() {
		for (final ValueDef setting : settings) {
			if (setting.hasChanged()) {
				return true;
			}
		}
		for (final ValueDef hotkey : hotkeys) {
			if (hotkey.hasChanged()) {
				return true;
			}
		}
		return false;
	}
}
