// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import com.ahli.cloning.DeepCopyable;
import com.ahli.galaxy.parser.DeduplicationIntensity;
import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.exceptions.UIException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
	 * @param file
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
	void processDescIndex(Path file, String raceId, String consoleSkinId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException;
	
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
	 * @return the blizzOnlyTemplates
	 */
	List<UITemplate> getBlizzOnlyTemplates();
	
	/**
	 * @return the constants
	 */
	List<UIConstant> getConstants();
	
	/**
	 * @return the blizzOnlyConstants
	 */
	List<UIConstant> getBlizzOnlyConstants();
	
	/**
	 * @return Mapping of the UIFrame handles
	 */
	Map<String, UIFrame> getHandles();
	
	/**
	 * @return the devLayouts
	 */
	List<String> getDevLayouts();
	
	/**
	 * Adds a Constant to the correct list. It removes other values and loggs warnings, if problems arise.
	 *
	 * @param constant
	 * @param isDevLayout
	 */
	void addConstant(UIConstant constant, boolean isDevLayout);
	
	/**
	 * @param fileName
	 * @param thisElem
	 * @param isDevLayout
	 * @return
	 * @throws UIException
	 */
	UITemplate addTemplate(String fileName, UIElement thisElem, boolean isDevLayout) throws UIException;
	
	void processInclude(
			String path,
			boolean isDevLayout,
			String raceId,
			String consoleSkinId,
			DeduplicationIntensity deduplicationAllowed);
	
	UITemplate[] getTemplatesOfPath(String file);
	
	/**
	 * Post process the parsed UICatalog.
	 */
	void postProcessParsing();
}
