package com.ahli.galaxy.ui;

import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserVtd;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/***
 * Represents a container for UI frame.**
 *
 * @author Ahli
 */
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UICatalogImpl implements UICatalog {
	
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger();
	
	@JsonIgnore
	private transient UICatalogParser parser;
	
	// members
	private List<UITemplate> templates;
	private List<UITemplate> blizzOnlyTemplates;
	private List<UIConstant> constants;
	private List<UIConstant> blizzOnlyConstants;
	private List<String> blizzOnlyLayouts;
	
	// internal, used during processing
	@JsonIgnore
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
	public UICatalogImpl(final int templatesCapacity, final int blizzOnlyTemplatesCapacity, final int constantsCapacity,
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
	public Object deepCopy() {
		// clone with additional space for templates and constants
		final UICatalogImpl clone =
				new UICatalogImpl(templates.size() * 3 / 2 + 1, blizzOnlyTemplates.size(), constants.size() * 3 / 2 + 1,
						blizzOnlyConstants.size(), blizzOnlyLayouts.size());
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
	public void clearParser() {
		parser = null;
	}
	
	@Override
	public void processDescIndex(final File f, final String raceId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException {
		
		final List<String> generalLayouts = DescIndexReader.getLayoutPathList(f, true);
		
		final List<String> combinedList = new ArrayList<>(DescIndexReader.getLayoutPathList(f, false));
		
		blizzOnlyLayouts.addAll(combinedList);
		blizzOnlyLayouts.removeAll(generalLayouts);
		
		final String descIndexPath = f.getAbsolutePath();
		final String basePath = descIndexPath.substring(0, descIndexPath.length() - f.getName().length());
		logger.trace("descIndexPath={}", () -> descIndexPath);
		logger.trace("basePath={}", () -> basePath);
		
		processLayouts(combinedList, basePath, raceId);
		
		//		printDebugStats();
	}
	
	/**
	 * @param toProcessList
	 * @param basePath
	 * @throws InterruptedException
	 * 		if the current thread was interrupted
	 */
	private void processLayouts(final List<String> toProcessList, final String basePath, final String raceId)
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
					basePathTemp = basePathTemp.substring(0, basePathTemp.lastIndexOf(File.separatorChar));
					if (logger.isTraceEnabled()) {
						logger.trace("basePathTemp=" + basePathTemp);
					}
				} else {
					if (!isDevLayout) {
						logger.error("ERROR: Cannot find layout file: " + intPath);
					} else {
						logger.warn("WARNING: Cannot find Blizz-only layout file: " + intPath + ", so this is fine.");
					}
				}
			}
			if (lastIndex != -1) {
				curBasePath = basePathTemp;
				final File layoutFile = new File(basePathTemp + File.separator + intPath);
				try {
					processLayoutFile(layoutFile, raceId, isDevLayout);
				} catch (final IOException e) {
					logger.error(
							"ERROR: encountered an Exception while processing the layout file '" + layoutFile + "'.",
							e);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		}
	}
	
	@Override
	public void processLayoutFile(final File f, final String raceId, final boolean isDevLayout) throws IOException {
		prepareParser();
		parser.parseFile(f, raceId, isDevLayout);
	}
	
	private void prepareParser() {
		if (parser == null) {
			parser = new UICatalogParser(this, new XmlParserVtd(null));
		}
	}
	
	public void printDebugStats() {
		logger.info(
				"UICatalogSizes: " + templates.size() + " " + blizzOnlyTemplates.size() + " " + constants.size() + " " +
						blizzOnlyConstants.size() + " " + blizzOnlyLayouts.size());
	}
	
	@Override
	public void processInclude(final String path, final boolean isDevLayout, final String raceId) {
		if (logger.isTraceEnabled()) {
			logger.trace("processing Include appearing within a real layout");
		}
		
		String basePathTemp = getCurBasePath();
		while (!Files.exists(Paths.get(basePathTemp + File.separator + path))) {
			final int lastIndex = basePathTemp.lastIndexOf(File.separatorChar);
			if (lastIndex != -1) {
				basePathTemp = basePathTemp.substring(0, basePathTemp.lastIndexOf(File.separatorChar));
				if (logger.isTraceEnabled()) {
					logger.trace("basePathTemp=" + basePathTemp);
				}
			} else {
				if (!isDevLayout) {
					logger.error("ERROR: Cannot find layout file: " + path);
				} else {
					logger.warn("WARNING: Cannot find Blizz-only layout file: " + path + ", so this is fine.");
				}
				return;
			}
		}
		final File file = new File(basePathTemp + File.separator + path);
		final XmlParser xmlParser = new XmlParserVtd();
		final UICatalogParser parserTemp = new UICatalogParser(this, xmlParser);
		try {
			parserTemp.parseFile(file, raceId, isDevLayout);
		} catch (final IOException e) {
			logger.error("ERROR: while parsing include appearing within usual layouts.", e);
		}
		xmlParser.clear();
	}
	
	@Override
	public String getCurBasePath() {
		return curBasePath;
	}
	
	/**
	 * @param curBasePath
	 * 		the curBasePath to set
	 */
	public void setCurBasePath(final String curBasePath) {
		this.curBasePath = curBasePath;
	}
	
	/**
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
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
				logger.warn("WARNING: constant '" + name +
						"' overrides value from Blizz-only constant, so this might be fine.");
			}
		} else {
			// is blizz-only layout
			removeConstantFromList(name, blizzOnlyConstants);
			final boolean removedGeneral = removeConstantFromList(name, constants);
			blizzOnlyConstants.add(constant);
			if (removedGeneral) {
				logger.warn("WARNING: constant '" + name +
						"' from Blizz-only layout overrides a general constant, so this might be fine.");
			}
		}
	}
	
	/**
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
	
	@Override
	public String getConstantValue(final String constantRef, final String raceId, final boolean isDevLayout) {
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
			final String constantNameWithRacePostFix = constantName + "_" + raceId;
			for (final UIConstant c : constants) {
				if (c.getName().equalsIgnoreCase(constantNameWithRacePostFix)) {
					return c.getValue();
				}
			}
		}
		if (i >= 3) {
			logger.error("ERROR: Encountered a constant definition with three #'" + constantRef +
					"' when its maximum is two '#'.");
		}
		
		if (!isDevLayout) {
			logger.warn("WARNING: Did not find a constant definition for '" + constantRef + "', so '" + constantName +
					"' is used instead.");
		} else {
			// inside blizz-only
			for (final UIConstant c : blizzOnlyConstants) {
				if (c.getName().equalsIgnoreCase(constantName)) {
					return c.getValue();
				}
			}
			logger.warn("WARNING: Did not find a constant definition for '" + constantRef +
					"', but it is a Blizz-only layout, so this is fine.");
		}
		return constantName;
	}
	
	@Override
	public List<UITemplate> getTemplates() {
		return templates;
	}
	
	@Override
	public void setTemplates(final List<UITemplate> templates2) {
		templates = templates2;
	}
	
	@Override
	public List<UITemplate> getBlizzOnlyTemplates() {
		return blizzOnlyTemplates;
	}
	
	@Override
	public void setBlizzOnlyTemplates(final List<UITemplate> blizzOnlyTemplates2) {
		blizzOnlyTemplates = blizzOnlyTemplates2;
	}
	
	@Override
	public List<UIConstant> getConstants() {
		return constants;
	}
	
	@Override
	public void setConstants(final List<UIConstant> constants2) {
		constants = constants2;
	}
	
	@Override
	public List<UIConstant> getBlizzOnlyConstants() {
		return blizzOnlyConstants;
	}
	
	@Override
	public void setBlizzOnlyConstants(final List<UIConstant> blizzOnlyConstants2) {
		blizzOnlyConstants = blizzOnlyConstants2;
	}
	
	@Override
	public List<String> getDevLayouts() {
		return blizzOnlyLayouts;
	}
	
	@Override
	public void setDevLayouts(final List<String> devLayouts) {
		blizzOnlyLayouts = devLayouts;
	}
	
}
