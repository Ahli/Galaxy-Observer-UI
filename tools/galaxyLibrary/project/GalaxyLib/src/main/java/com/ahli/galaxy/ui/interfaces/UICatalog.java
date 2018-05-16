package com.ahli.galaxy.ui.interfaces;

import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.util.DeepCopyable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface UICatalog extends DeepCopyable, Serializable {
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * Clears the XML DOM Parser to release its memory.
	 */
	void clearParser();
	
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
	void processDescIndex(File f, String raceId)
			throws SAXException, IOException, ParserConfigurationException, InterruptedException;
	
	/**
	 * @param f
	 * 		layout file to process
	 * @param raceId
	 * 		to use to check constants starting with ##
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws UIException
	 * @throws InterruptedException
	 */
	void processLayoutFile(File f, String raceId, boolean isDevLayout) throws IOException;
	
	/**
	 * @param constantRef
	 * @param raceId
	 * 		id String of the viewer's Race
	 * @return
	 */
	String getConstantValue(String constantRef, String raceId, boolean isDevLayout);
	
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
	
	void processInclude(String path, boolean isDevLayout, String raceId);
}
