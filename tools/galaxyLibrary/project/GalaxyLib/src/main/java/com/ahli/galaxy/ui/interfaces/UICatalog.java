// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.util.DeepCopyable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface UICatalog extends DeepCopyable {
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * Sets/Clears the XML Parser.
	 */
	void setParser(ParsedXmlConsumer parser);
	
	/**
	 * @param f
	 * 		descIndex file to process
	 * @param raceId
	 * 		to use to check constants starting with ##
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 * @throws InterruptedException
	 * 		if the current Thread was interrupted
	 */
	void processDescIndex(File f, String raceId, String consoleSkinId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException;
	
	/**
	 * @param p
	 * 		layout file to process
	 * @param raceId
	 * 		to use to check constants starting with ##
	 * @param isDevLayout
	 * @param consoleSkinId
	 * @param parser
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 * @throws InterruptedException
	 */
	void processLayoutFile(Path p, String raceId, boolean isDevLayout, String consoleSkinId, ParsedXmlConsumer parser)
			throws IOException;
	
	/**
	 * @param constantRef
	 * @param raceId
	 * 		id String of the viewer's Race
	 * @return
	 */
	String getConstantValue(String constantRef, String raceId, boolean isDevLayout, String consoleSkinId);
	
	/**
	 * @return the templates
	 */
	List<UITemplate> getTemplates();
	
	/**
	 * @param templates
	 * 		the templates to set
	 */
	void setTemplates(List<UITemplate> templates);
	
	/**
	 * @return the blizzOnlyTemplates
	 */
	List<UITemplate> getBlizzOnlyTemplates();
	
	/**
	 * @param blizzOnlyTemplates
	 * 		the blizzOnlyTemplates to set
	 */
	void setBlizzOnlyTemplates(List<UITemplate> blizzOnlyTemplates);
	
	/**
	 * @return the constants
	 */
	List<UIConstant> getConstants();
	
	/**
	 * @param constants
	 * 		the constants to set
	 */
	void setConstants(List<UIConstant> constants);
	
	/**
	 * @return the blizzOnlyConstants
	 */
	List<UIConstant> getBlizzOnlyConstants();
	
	/**
	 * @param blizzOnlyConstants
	 * 		the blizzOnlyConstants to set
	 */
	void setBlizzOnlyConstants(List<UIConstant> blizzOnlyConstants);
	
	Map<String, UIFrame> getHandles();
	
	/**
	 * @return the devLayouts
	 */
	List<String> getDevLayouts();
	
	/**
	 * @param devLayouts
	 * 		the devLayouts to set
	 */
	void setDevLayouts(List<String> devLayouts);
	
	/**
	 * @return the curBasePath
	 */
	String getCurBasePath();
	
	/**
	 * Adds a Constant to the correct list. It removes other values and loggs warnings, if problems arise.
	 *
	 * @param constant
	 * @param isDevLayout
	 */
	void addConstant(final UIConstant constant, final boolean isDevLayout);
	
	/**
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
	 * @return
	 * @throws UIException
	 */
	UITemplate addTemplate(final String fileName, final UIElement thisElem, final boolean isDevLayout)
			throws UIException;
	
	void processInclude(String path, boolean isDevLayout, String raceId, String consoleSkinId);
	
	UITemplate[] getTemplatesOfPath(final String file);
	
	/**
	 * Post process the parsed UICatalog.
	 */
	void postProcessParsing();
}
