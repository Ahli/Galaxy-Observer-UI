// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserVtd;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/***
 * Represents a container for UI frame.**
 *
 * @author Ahli
 */
public class UICatalogImpl implements UICatalog {
	
	private static final String UNDERSCORE = "_";
	private static final Logger logger = LogManager.getLogger(UICatalogImpl.class);
	private UICatalogParser parser;
	
	// members
	private List<UITemplate> templates;
	private List<UITemplate> blizzOnlyTemplates;
	private List<UIConstant> constants;
	private List<UIConstant> blizzOnlyConstants;
	private List<String> blizzOnlyLayouts;
	
	// internal, used during processing
	private String curBasePath;
	
	/**
	 * Constructor.
	 *
	 * @throws ParserConfigurationException
	 */
	public UICatalogImpl() {
		templates = new ArrayList<>(2500);
		blizzOnlyTemplates = new ArrayList<>();
		constants = new ArrayList<>(800);
		blizzOnlyConstants = new ArrayList<>();
		blizzOnlyLayouts = new ArrayList<>(25);
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
			final int blizzOnlyLayoutsCapacity) {
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
	public Object deepCopy() {
		// clone with additional space for templates and constants
		final UICatalogImpl clone =
				new UICatalogImpl(templates.size() /* 3 / 2 + 1*/, blizzOnlyTemplates.size(), constants.size()
						/* 3 / 2 + 1*/, blizzOnlyConstants.size(), blizzOnlyLayouts.size());
		// testing shows that iterators are not faster and are not thread safe
		int i;
		int len;
		for (i = 0, len = templates.size(); i < len; i++) {
			clone.templates.add((UITemplate) templates.get(i).deepCopy());
		}
		for (i = 0, len = blizzOnlyTemplates.size(); i < len; i++) {
			clone.blizzOnlyTemplates.add((UITemplate) blizzOnlyTemplates.get(i).deepCopy());
		}
		for (i = 0, len = constants.size(); i < len; i++) {
			clone.constants.add((UIConstant) constants.get(i).deepCopy());
		}
		for (i = 0, len = blizzOnlyConstants.size(); i < len; i++) {
			clone.blizzOnlyConstants.add((UIConstant) blizzOnlyConstants.get(i).deepCopy());
		}
		for (i = 0, len = blizzOnlyLayouts.size(); i < len; i++) {
			clone.blizzOnlyLayouts.add(blizzOnlyLayouts.get(i));
		}
		clone.curBasePath = curBasePath;
		
		return clone;
	}
	
	@Override
	public void setParser(final UICatalogParser parser) {
		this.parser = parser;
	}
	
	@Override
	public void processDescIndex(final File f, final String raceId, final String consoleSkinId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException {
		
		final List<String> generalLayouts = DescIndexReader.getLayoutPathList(f, true);
		
		final List<String> combinedList = new ArrayList<>(DescIndexReader.getLayoutPathList(f, false));
		
		blizzOnlyLayouts.addAll(combinedList);
		blizzOnlyLayouts.removeAll(generalLayouts);
		
		final String descIndexPath = f.getAbsolutePath();
		final String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());
		logger.trace("descIndexPath={}", () -> descIndexPath);
		logger.trace("basePath={}", () -> basePath);
		
		processLayouts(combinedList, basePath, raceId, consoleSkinId);
		
		if (logger.isTraceEnabled()) {
			logger.trace(
					"UICatalogSizes: " + templates.size() + " " + blizzOnlyTemplates.size() + " " + constants.size() +
							" " + blizzOnlyConstants.size() + " " + blizzOnlyLayouts.size());
		}
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
			logger.trace("intPath={}", () -> intPath);
			logger.trace("isDevLayout={}", () -> isDevLayout);
			basePathTemp = basePath;
			int lastIndex = 0;
			while (!new File(basePathTemp + File.separator + intPath).exists() && lastIndex != -1) {
				lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
				if (lastIndex != -1) {
					basePathTemp = basePathTemp.substring(0, lastIndex);
					if (logger.isTraceEnabled()) {
						logger.trace("basePathTemp={}", basePathTemp);
					}
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
					logger.error(
							String.format("ERROR: encountered an Exception while processing the layout file '%s'.",
									layoutFilePath),
							e);
				}
				if (Thread.interrupted()) {
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
			final String path, final boolean isDevLayout, final String raceId, final String consoleSkinId) {
		if (logger.isTraceEnabled()) {
			logger.trace("processing Include appearing within a real layout");
		}
		
		String basePathTemp = getCurBasePath();
		while (!Files.exists(Path.of(basePathTemp, path))) {
			final int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
			if (lastIndex != -1) {
				basePathTemp = basePathTemp.substring(0, lastIndex);
				if (logger.isTraceEnabled()) {
					logger.trace("basePathTemp={}", basePathTemp);
				}
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
		final UICatalogParser parserTemp = new UICatalogParser(this, xmlParser, true);
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
		final List<UITemplate> foundTemplates = new ArrayList<>();
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
		return foundTemplates.toArray(new UITemplate[0]);
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
	 * @throws UIException
	 */
	@Override
	public void addTemplate(final String fileName, final UIElement thisElem, final boolean isDevLayout)
			throws UIException {
		if (thisElem == null) {
			throw new UIException("Cannot create Template definition for a 'null' UIElement.");
		}
		
		final List<UITemplate> list = isDevLayout ? blizzOnlyTemplates : templates;
		final UITemplate template = new UITemplate(fileName, thisElem);
		list.add(template);
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
		if (constantRef.length() > 0) {
			while (constantRef.charAt(i) == '#') {
				i++;
			}
		}
		// no constant tag
		if (i <= 0) {
			return constantRef;
		}
		final String prefix = constantRef.substring(0, i);
		final String constantName = constantRef.substring(i);
		logger.trace("Encountered Constant: prefix='{}', constantName='{}'", () -> prefix, () -> constantName);
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
	public void setTemplates(final List<UITemplate> templates) {
		this.templates = templates;
	}
	
	@Override
	public List<UITemplate> getBlizzOnlyTemplates() {
		return blizzOnlyTemplates;
	}
	
	@Override
	public void setBlizzOnlyTemplates(final List<UITemplate> blizzOnlyTemplates) {
		this.blizzOnlyTemplates = blizzOnlyTemplates;
	}
	
	@Override
	public List<UIConstant> getConstants() {
		return constants;
	}
	
	@Override
	public void setConstants(final List<UIConstant> constants) {
		this.constants = constants;
	}
	
	@Override
	public List<UIConstant> getBlizzOnlyConstants() {
		return blizzOnlyConstants;
	}
	
	@Override
	public void setBlizzOnlyConstants(final List<UIConstant> blizzOnlyConstants) {
		this.blizzOnlyConstants = blizzOnlyConstants;
	}
	
	@Override
	public List<String> getDevLayouts() {
		return blizzOnlyLayouts;
	}
	
	@Override
	public void setDevLayouts(final List<String> devLayouts) {
		blizzOnlyLayouts = devLayouts;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UICatalogImpl) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					logger.info("equals=false - object - i={}", i);
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					logger.info("equals=false - array - i={}", i);
					return false;
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { templates, constants, blizzOnlyTemplates, blizzOnlyConstants, blizzOnlyLayouts };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
