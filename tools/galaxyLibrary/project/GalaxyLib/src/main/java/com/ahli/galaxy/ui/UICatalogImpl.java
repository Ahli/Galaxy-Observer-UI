// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.parser.DeduplicationIntensity;
import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserVtd;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.galaxy.ui.interfaces.UIConstant;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***
 * Represents a container for UI frame.**
 *
 * @author Ahli
 */
public class UICatalogImpl implements UICatalog {
	
	private static final String UNDERSCORE = "_";
	private static final Logger logger = LogManager.getLogger(UICatalogImpl.class);
	// members
	private final List<UITemplate> templates;
	private final List<UITemplate> blizzOnlyTemplates;
	private final List<UIConstant> constants;
	private final List<UIConstant> blizzOnlyConstants;
	private final Map<String, UIFrame> handles;
	private List<String> blizzOnlyLayouts;
	private ParsedXmlConsumer parser;
	// internal, used during processing
	private String curBasePath;
	
	public UICatalogImpl() {
		templates = new ArrayList<>(2500);
		blizzOnlyTemplates = new ArrayList<>(10);
		constants = new ArrayList<>(800);
		blizzOnlyConstants = new ArrayList<>(10);
		blizzOnlyLayouts = new ArrayList<>(25);
		handles = new UnifiedMap<>(650 * 5 / 4);
	}
	
	public UICatalogImpl(final GameDef gameDef) {
		if (gameDef != null) {
			if ("sc2".equals(gameDef.nameHandle())) {
				templates = new ArrayList<>(2365);
				blizzOnlyTemplates = new ArrayList<>(17);
				constants = new ArrayList<>(1241);
				blizzOnlyConstants = new ArrayList<>(0);
				blizzOnlyLayouts = new ArrayList<>(14);
				handles = new UnifiedMap<>(242 * 5 / 4);
				return;
			} else if ("heroes".equals(gameDef.nameHandle())) {
				templates = new ArrayList<>(2088);
				blizzOnlyTemplates = new ArrayList<>(6);
				constants = new ArrayList<>(449);
				blizzOnlyConstants = new ArrayList<>(0);
				blizzOnlyLayouts = new ArrayList<>(11);
				handles = new UnifiedMap<>(367 * 5 / 4);
				return;
			}
		}
		templates = new ArrayList<>(2500);
		blizzOnlyTemplates = new ArrayList<>(10);
		constants = new ArrayList<>(800);
		blizzOnlyConstants = new ArrayList<>(10);
		blizzOnlyLayouts = new ArrayList<>(25);
		handles = new UnifiedMap<>(650 * 5 / 4);
	}
	
	/**
	 * Constructor.
	 *
	 * @throws ParserConfigurationException
	 */
	public UICatalogImpl(
			final int templatesCapacity,
			final int blizzOnlyTemplatesCapacity,
			final int constantsCapacity,
			final int blizzOnlyConstantsCapacity,
			final int blizzOnlyLayoutsCapacity,
			final int handlesCapacity) {
		templates = new ArrayList<>(templatesCapacity);
		blizzOnlyTemplates = new ArrayList<>(blizzOnlyTemplatesCapacity);
		constants = new ArrayList<>(constantsCapacity);
		blizzOnlyConstants = new ArrayList<>(blizzOnlyConstantsCapacity);
		blizzOnlyLayouts = new ArrayList<>(blizzOnlyLayoutsCapacity);
		handles = new UnifiedMap<>(handlesCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		// clone with additional space for templates, constants, handles
		final UICatalogImpl clone = new UICatalogImpl(templates.size() + 550,
				blizzOnlyTemplates.size(),
				constants.size() + 150,
				blizzOnlyConstants.size(),
				blizzOnlyLayouts.size(),
				handles.size() + 300);
		// testing shows that iterators are not faster and are not thread safe
		int i;
		int len;
		for (i = 0, len = templates.size(); i < len; ++i) {
			clone.templates.add((UITemplate) templates.get(i).deepCopy());
		}
		for (i = 0, len = blizzOnlyTemplates.size(); i < len; ++i) {
			clone.blizzOnlyTemplates.add((UITemplate) blizzOnlyTemplates.get(i).deepCopy());
		}
		for (i = 0, len = constants.size(); i < len; ++i) {
			clone.constants.add((UIConstant) constants.get(i).deepCopy());
		}
		for (i = 0, len = blizzOnlyConstants.size(); i < len; ++i) {
			clone.blizzOnlyConstants.add((UIConstant) blizzOnlyConstants.get(i).deepCopy());
		}
		clone.blizzOnlyLayouts = blizzOnlyLayouts;
		for (final var handleEntry : handles.entrySet()) {
			clone.handles.put(handleEntry.getKey(), handleEntry.getValue());
		}
		
		clone.curBasePath = curBasePath;
		
		return clone;
	}
	
	@Override
	public void setParser(final ParsedXmlConsumer parser) {
		this.parser = parser;
	}
	
	@Override
	public void processDescIndex(final File f, final String raceId, final String consoleSkinId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException {
		
		// TODO inefficient code, parses twice
		List<String> blizzLayouts = DescIndexReader.getLayoutPathList(f, DescIndexReader.Mode.ONLY_UNLOADABLE);
		blizzOnlyLayouts.addAll(blizzLayouts);
		blizzLayouts = null;
		
		final String descIndexPath = f.getAbsolutePath();
		final String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());
		logger.trace("descIndexPath={}\nbasePath={}", descIndexPath, basePath);
		
		final List<String> combinedList = DescIndexReader.getLayoutPathList(f, DescIndexReader.Mode.ALL);
		processLayouts(combinedList, basePath, raceId, consoleSkinId);
		
		logger.trace(
				"UICatalogSizes: templates={}, blizzTemplates={}, constants={}, blizzConstants={}, blizzLayouts={}",
				templates.size(),
				blizzOnlyTemplates.size(),
				constants.size(),
				blizzOnlyConstants.size(),
				blizzOnlyLayouts.size());
	}
	
	/**
	 * @param toProcessList
	 * @param basePath
	 * @param raceId
	 * @param consoleSkinId
	 * @throws InterruptedException
	 * 		if the current thread was interrupted
	 */
	private void processLayouts(
			final List<String> toProcessList, final String basePath, final String raceId, final String consoleSkinId)
			throws InterruptedException {
		String basePathTemp;
		for (final String intPath : toProcessList) {
			final boolean isDevLayout = blizzOnlyLayouts.contains(intPath);
			logger.trace("intPath={}\nisDevLayout={}", intPath, isDevLayout);
			basePathTemp = basePath;
			int lastIndex = 0;
			while (!new File(basePathTemp + File.separator + intPath).exists() && lastIndex != -1) {
				lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					basePathTemp = basePathTemp.substring(0, lastIndex);
					logger.trace("basePathTemp={}", basePathTemp);
				} else {
					if (!isDevLayout) {
						logger.error("ERROR: Cannot find layout file: {}", intPath);
					} else {
						logger.warn("WARNING: Cannot find Blizz-only layout file: {}, so this is fine.", intPath);
					}
				}
			}
			if (lastIndex != -1) {
				curBasePath = basePathTemp;
				final Path layoutFilePath = Path.of(basePathTemp, intPath);
				try {
					processLayoutFile(layoutFilePath, raceId, isDevLayout, consoleSkinId, parser);
				} catch (final IOException e) {
					final String msg = String.format(
							"ERROR: encountered an Exception while processing the layout file '%s'.",
							layoutFilePath);
					logger.error(msg, e);
				}
				if (Thread.interrupted()) {
					//noinspection NewExceptionWithoutArguments
					throw new InterruptedException();
				}
			}
		}
	}
	
	@Override
	public void processLayoutFile(
			final Path p,
			final String raceId,
			final boolean isDevLayout,
			final String consoleSkinId,
			final ParsedXmlConsumer parser) throws IOException {
		parser.parseFile(p, raceId, isDevLayout, consoleSkinId);
	}
	
	@Override
	public void processInclude(
			final String path,
			final boolean isDevLayout,
			final String raceId,
			final String consoleSkinId,
			final DeduplicationIntensity deduplicationAllowed) {
		logger.trace("processing Include appearing within a real layout");
		
		String basePathTemp = getCurBasePath();
		while (!Files.exists(Path.of(basePathTemp, path))) {
			final int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
			if (lastIndex != -1) {
				basePathTemp = basePathTemp.substring(0, lastIndex);
				logger.trace("basePathTemp={}", basePathTemp);
			} else {
				if (!isDevLayout) {
					logger.error("ERROR: Cannot find layout file: {}", path);
				} else {
					logger.warn("WARNING: Cannot find Blizz-only layout file: {}, so this is fine.", path);
				}
				return;
			}
		}
		final Path filePath = Path.of(basePathTemp, path);
		final XmlParser xmlParser = new XmlParserVtd();
		final UICatalogParser parserTemp = new UICatalogParser(this, xmlParser, deduplicationAllowed);
		try {
			parserTemp.parseFile(filePath, raceId, isDevLayout, consoleSkinId);
		} catch (final IOException e) {
			logger.error("ERROR: while parsing include appearing within usual layouts.", e);
		}
		xmlParser.clear();
	}
	
	@Override
	public String getCurBasePath() {
		return curBasePath;
	}
	
	@Override
	public UITemplate[] getTemplatesOfPath(final String file) {
		final List<UITemplate> foundTemplates = new ArrayList<>(1);
		for (final var template : templates) {
			if (template.getFileName().equalsIgnoreCase(file)) {
				foundTemplates.add(template);
			}
		}
		for (final var template : blizzOnlyTemplates) {
			if (template.getFileName().equalsIgnoreCase(file)) {
				logger.error("ERROR: cannot modify Blizzard-only Template: {}", template.getFileName());
				break;
			}
		}
		if (foundTemplates.isEmpty()) {
			logger.warn("WARN: cannot find Layout file: {}", file);
		}
		return foundTemplates.toArray(new UITemplate[foundTemplates.size()]);
	}
	
	@Override
	public void postProcessParsing() {
		if (parser != null) {
			parser.deduplicate();
		}
	}
	
	/**
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
	 * @return
	 * @throws UIException
	 */
	@Override
	public UITemplate addTemplate(final String fileName, final UIElement thisElem, final boolean isDevLayout)
			throws UIException {
		if (thisElem == null) {
			throw new UIException("Cannot create Template definition for a 'null' UIElement.");
		}
		
		final List<UITemplate> list = isDevLayout ? blizzOnlyTemplates : templates;
		final UITemplate template = new UITemplate(fileName, thisElem);
		list.add(template);
		return template;
	}
	
	/**
	 * Adds a Constant to the correct list. It removes other values and loggs warnings, if problems arise.
	 *
	 * @param constant
	 * @param isDevLayout
	 */
	@Override
	public void addConstant(final UIConstant constant, final boolean isDevLayout) {
		final String name = constant.getName();
		// register with parent/templates overriding previous constant values
		if (!isDevLayout) {
			// is general layout
			final boolean removedBlizzOnly = removeConstantFromList(name, blizzOnlyConstants);
			removeConstantFromList(name, constants);
			constants.add(constant);
			if (removedBlizzOnly) {
				logger.warn("WARNING: constant '{}' overrides value from Blizz-only constant, so this might be fine.",
						name);
			}
		} else {
			// is blizz-only layout
			removeConstantFromList(name, blizzOnlyConstants);
			final boolean removedGeneral = removeConstantFromList(name, constants);
			blizzOnlyConstants.add(constant);
			if (removedGeneral) {
				logger.warn(
						"WARNING: constant '{}' from Blizz-only layout overrides a general constant, so this might be fine.",
						name);
			}
		}
	}
	
	/**
	 * @param name
	 * @param listOfConstants
	 * @return
	 */
	private static boolean removeConstantFromList(final String name, final List<UIConstant> listOfConstants) {
		boolean result = false;
		for (int i = listOfConstants.size() - 1; i >= 0; --i) {
			final UIConstant curConst = listOfConstants.get(i);
			if (curConst.getName().compareToIgnoreCase(name) == 0) {
				listOfConstants.remove(i);
				result = true;
			}
		}
		return result;
	}
	
	@Override
	public String getConstantValue(
			final String constantRef, final String raceId, final boolean isDevLayout, final String consoleSkinId) {
		int i = 0;
		if (!constantRef.isEmpty()) {
			while (constantRef.charAt(i) == '#') {
				++i;
			}
		}
		// no constant tag
		if (i <= 0) {
			return constantRef;
		}
		final String prefix = constantRef.substring(0, i);
		final String constantName = constantRef.substring(i);
		logger.trace("Encountered Constant: prefix='{}', constantName='{}'", prefix, constantName);
		for (final UIConstant c : constants) {
			if (c.getName().equalsIgnoreCase(constantName)) {
				return c.getValue();
			}
		}
		// constant tag with race suffix
		if (i == 2) {
			final String constantNameWithRacePostFix = constantName + UNDERSCORE + raceId;
			for (final UIConstant c : constants) {
				if (c.getName().equalsIgnoreCase(constantNameWithRacePostFix)) {
					return c.getValue();
				}
			}
		} else if (i == 3) {
			final String constantNameWithConsolePostFix = constantName + UNDERSCORE + consoleSkinId;
			for (final UIConstant c : constants) {
				if (c.getName().equalsIgnoreCase(constantNameWithConsolePostFix)) {
					return c.getValue();
				}
			}
		} else if (i >= 4) {
			logger.error(
					"ERROR: Encountered a constant definition with three #'{}' when its maximum is two '#'.",
					constantRef);
		}
		
		if (!isDevLayout) {
			logger.warn("WARNING: Did not find a constant definition for '{}', so '{}' is used instead.",
					constantRef,
					constantName);
		} else {
			// inside blizz-only
			for (final UIConstant c : blizzOnlyConstants) {
				if (c.getName().equalsIgnoreCase(constantName)) {
					return c.getValue();
				}
			}
			logger.warn(
					"WARNING: Did not find a constant definition for '{}', but it is a Blizz-only layout, so this is fine.",
					constantRef);
		}
		return constantName;
	}
	
	@Override
	public List<UITemplate> getTemplates() {
		return templates;
	}
	
	@Override
	public List<UITemplate> getBlizzOnlyTemplates() {
		return blizzOnlyTemplates;
	}
	
	@Override
	public List<UIConstant> getConstants() {
		return constants;
	}
	
	@Override
	public List<UIConstant> getBlizzOnlyConstants() {
		return blizzOnlyConstants;
	}
	
	@Override
	public Map<String, UIFrame> getHandles() {
		return handles;
	}
	
	@Override
	public List<String> getDevLayouts() {
		return blizzOnlyLayouts;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final UICatalogImpl uiCatalog)) {
			return false;
		}
		return Objects.equals(parser, uiCatalog.parser) && Objects.equals(templates, uiCatalog.templates) &&
				Objects.equals(blizzOnlyTemplates, uiCatalog.blizzOnlyTemplates) &&
				Objects.equals(constants, uiCatalog.constants) &&
				Objects.equals(blizzOnlyConstants, uiCatalog.blizzOnlyConstants) &&
				Objects.equals(blizzOnlyLayouts, uiCatalog.blizzOnlyLayouts) &&
				Objects.equals(handles, uiCatalog.handles) && Objects.equals(curBasePath, uiCatalog.curBasePath);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(parser,
				templates,
				blizzOnlyTemplates,
				constants,
				blizzOnlyConstants,
				blizzOnlyLayouts,
				handles,
				curBasePath);
	}
}
