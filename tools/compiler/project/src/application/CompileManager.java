package application;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.ui.UICatalog;
import com.ahli.galaxy.ui.exception.UIException;

/**
 * Compiles MPQ stuff.
 * 
 * @author Ahli
 */
public class CompileManager {
	private static Logger logger = LogManager.getLogger(CompileManager.class);
	
	/**
	 * Constructor.
	 */
	public CompileManager() {
	}
	
	/**
	 * Compiles and updates the data in the cache.
	 * 
	 * @param mod
	 *            the mod
	 * @param raceId
	 *            the raceId used
	 * @throws InterruptedException
	 */
	public void compile(final ModData mod, final String raceId) throws InterruptedException {
		try {
			long startTime;
			long executionTime;
			
			startTime = System.currentTimeMillis();
			
			// manage descIndexData
			final DescIndexData descIndex = mod.getDescIndexData();
			manageOrderOfLayoutFiles(descIndex);
			
			executionTime = (System.currentTimeMillis() - startTime);
			logger.info("DescIndex management took " + executionTime + "ms.");
			
			// validate catalog
			final File descIndexFile = new File(
					mod.getCachePath() + File.separator + mod.getDescIndexData().getDescIndexIntPath());
			logger.trace("processing descIndexFile: " + descIndexFile);
			startTime = System.currentTimeMillis();
			
			final UICatalog catalogClone = getClonedUICatalog(mod);
			
			executionTime = (System.currentTimeMillis() - startTime);
			logger.info("BaseUI Cloning took " + executionTime + "ms.");
			
			startTime = System.currentTimeMillis();
			
			// apply mod's UI
			catalogClone.processDescIndex(descIndexFile, raceId);
			
			executionTime = (System.currentTimeMillis() - startTime);
			logger.info("Processing DescIndex took " + executionTime + "ms.");
			
		} catch (ParserConfigurationException | SAXException | IOException | UIException e) {
			logger.error("ERROR: encountered error while compiling.", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a correct ordering of the layout files based on their internal
	 * dependencies.
	 * 
	 * @param descIndex
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void manageOrderOfLayoutFiles(final DescIndexData descIndex)
			throws ParserConfigurationException, SAXException, IOException {
		// manage order of layout files in DescIndex
		descIndex.orderLayoutFiles();
		descIndex.persistDescIndexFile();
	}
	
	/**
	 * Clones the specified UICatalog.
	 * 
	 * @param mod
	 *            ModData that containing the source CatalogUI
	 * @return clone of mod's CatalogUI
	 */
	private UICatalog getClonedUICatalog(final ModData mod) {
		return (UICatalog) mod.getGameData().getUiCatalog().deepCopy();
	}
}
