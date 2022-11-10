// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.galaxy.ext;

import com.ahli.galaxy.game.GameDef;
import com.ahli.hotkey_ui.application.model.OptionValueDef;
import com.ahli.hotkey_ui.application.model.TextValueDef;
import com.ahli.hotkey_ui.application.model.TextValueDefType;
import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import com.ahli.xml.XmlDomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final String ATTRIBUTE_VALUES_DISPLAY_NAMES = "valuesdisplaynames";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_GAMESTRINGS_ADD = "gamestrings_add";
	private static final String HOTKEY = "@hotkey";
	private static final String SETTING = "@setting";
	private static final Logger logger = LoggerFactory.getLogger(LayoutExtensionReader.class);
	private static final Pattern HOTKEY_SETTING_REGEX_PATTERN = Pattern.compile("(?<=@hotkey|@setting)/i");
	private static final Pattern ATTRIBUTES_REGEX_PATTERN = Pattern.compile(
			"(?i)(?=(?:constant|default|description|values|valuesdisplaynames|type|gamestrings_add)[\\s]*=)");
	private final List<TextValueDef> hotkeys = new ArrayList<>();
	private final List<ValueDef> settings = new ArrayList<>();
	
	
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
	
	private static String[] parseListOfValues(final String part) {
		final String[] values = part.split("/");
		for (int i = 0; i < values.length; ++i) {
			values[i] = getValueWithinQuotes(values[i]);
		}
		return values;
	}
	
	/**
	 * @return the hotkeys
	 */
	public List<TextValueDef> getHotkeys() {
		return hotkeys;
	}
	
	/**
	 * @return the settings
	 */
	public List<ValueDef> getSettings() {
		return settings;
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
		
		for (int i = 0, len = layoutFiles.size(); i < len; ++i) {
			final Path path = layoutFiles.get(i);
			final Document doc;
			try {
				doc = dBuilder.parse(path.toString());
			} catch (final SAXParseException e) {
				logParseException(e, path);
				layoutFiles.remove(i);
				--i;
				--len;
				continue;
			} catch (final IOException e) {
				logParseException(e, path);
				continue;
			}
			
			logger.debug("comments - processing file: {}", path);
			
			// read comments
			final Element elem = doc.getDocumentElement();
			final NodeList childNodes = elem.getChildNodes();
			readComments(childNodes);
		}
		
		for (final Path path : layoutFiles) {
			final Document doc;
			try {
				// parse XML file
				doc = dBuilder.parse(path.toString());
			} catch (final SAXParseException | IOException e) {
				// should be an IOException now since layoutFiles was filtered
				logParseException(e, path);
				continue;
			}
			
			logger.debug("constants - processing file: {}", path);
			
			// read constants
			final Element elem = doc.getDocumentElement();
			final NodeList childNodes = elem.getChildNodes();
			LayoutFileUpdater.readConstants(childNodes, hotkeys, settings);
		}
	}
	
	private void logParseException(final Exception e, final Path path) {
		if (!"Content is not allowed in prolog.".equals(e.getMessage())) {
			logger.trace("Error parsing file {}.", path, e);
		} else {
			logger.trace("Error parsing file {}.", path);
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
			// split at keywords @hotkey or @setting without removing, case-insensitive
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
					String[] allowedValuesDisplayNames = null;
					
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
						} else if (partLower.startsWith(ATTRIBUTE_VALUES_DISPLAY_NAMES)) {
							allowedValuesDisplayNames = parseListOfValues(part);
							logger.trace("valuesDisplayNames = {}", part);
						} else if (partLower.startsWith(ATTRIBUTE_VALUES)) {
							allowedValues = parseListOfValues(part);
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
						addSettingsValueDef(
								constant,
								description,
								defaultValue,
								type,
								gamestringsAdd,
								allowedValues,
								allowedValuesDisplayNames);
					}
				}
			}
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			logger.trace("Parsing Comment failed.", e);
		}
	}
	
	private void addSettingsValueDef(
			final String constant,
			final String description,
			final String defaultValue,
			final String type,
			final String gamestringsAdd,
			final String[] allowedValues,
			final String[] allowedValuesDisplayNames) {
		
		final ValueDef valueDef;
		if (allowedValues != null || type.equalsIgnoreCase("boolean")) {
			valueDef = new OptionValueDef(
					constant,
					description,
					defaultValue,
					type,
					allowedValues,
					allowedValuesDisplayNames,
					gamestringsAdd);
		} else {
			valueDef = new TextValueDef(constant, description, defaultValue, type);
		}
		settings.add(valueDef);
	}
	
	private void addHotkeyValueDef(final String constant, final String description, final String defaultValue) {
		hotkeys.add(new TextValueDef(constant, description, defaultValue, TextValueDefType.HOTKEY));
	}
	
	/**
	 * @param projectInCache
	 * @return true if changes were present
	 * @throws ParserConfigurationException
	 * @throws TransformerConfigurationException
	 */
	public boolean updateLayoutFiles(final Path projectInCache)
			throws ParserConfigurationException, TransformerConfigurationException {
		
		final List<TextValueDef> changedHotkeys = hotkeys.stream().filter(ValueDef::getHasChanged).toList();
		final List<ValueDef> changedSettings = settings.stream().filter(ValueDef::getHasChanged).toList();
		
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
				// TODO do not eat this exception -> stop and show error
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
		final List<OptionValueDef> gamestringsAddSettings = new ArrayList<>();
		for (final ValueDef setting : settings) {
			if (setting instanceof OptionValueDef ovd && ovd.getHasChanged() && !ovd.getGamestringsAdd().isEmpty()) {
				gamestringsAddSettings.add(ovd);
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
			if (setting.getHasChanged()) {
				return true;
			}
		}
		for (final TextValueDef hotkey : hotkeys) {
			if (hotkey.getHasChanged()) {
				return true;
			}
		}
		return false;
	}
}
