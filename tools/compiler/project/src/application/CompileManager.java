package application;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.ahli.galaxy.DescIndexData;

import application.util.ErrorTracker;

/**
 * Compiles MPQ stuff.
 * 
 * @author Ahli
 *
 */
public class CompileManager {
	static Logger LOGGER = LogManager.getLogger(CompileManager.class);
	
	private ErrorTracker errorTracker;
	
	public CompileManager(ErrorTracker errorTracker) {
		this.errorTracker = errorTracker;
	}
	
	/**
	 * Compiles and updates the data in the cache.
	 */
	public void compile(DescIndexData descIndex) {
		try {
			// manage order of layout files in DescIndex
			descIndex.orderLayoutFiles();
			descIndex.persistDescIndexFile();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			errorTracker.reportErrorEncounter(e);
			LOGGER.error("encountered error while compiling", e);
			e.printStackTrace();
		}
	}
	
}
