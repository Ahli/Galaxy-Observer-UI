package interfacebuilder.compile;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.util.SilentXmlSaxErrorHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Compiles MPQ stuff.
 *
 * @author Ahli
 */
public class CompileService {
	private static final Logger logger = LogManager.getLogger(CompileService.class);
	
	/**
	 * Compiles and updates the data in the cache.
	 *
	 * @param mod
	 * 		the mod
	 * @param raceId
	 * 		the raceId used
	 * @param repairLayoutOrder
	 * @param verifyLayout
	 * @param verifyXml
	 * @return UICatalog describing the UI when verifyLayout was enabled; else null
	 * @throws InterruptedException
	 */
	public UICatalog compile(final ModData mod, final String raceId, final boolean repairLayoutOrder,
			final boolean verifyLayout, final boolean verifyXml) throws InterruptedException {
		UICatalog catalogClone = null;
		try {
			long startTime;
			long executionTime;
			
			// manage descIndexData
			final DescIndexData descIndex = mod.getDescIndexData();
			if (repairLayoutOrder) {
				startTime = System.currentTimeMillis();
				manageOrderOfLayoutFiles(descIndex);
				executionTime = (System.currentTimeMillis() - startTime);
				logger.info("Checking and repairing the Layout order took " + executionTime + "ms.");
			}
			if (verifyLayout) {
				startTime = System.currentTimeMillis();
				
				// validate catalog
				catalogClone = getClonedUICatalog(mod.getGameData().getUiCatalog());
				
				executionTime = (System.currentTimeMillis() - startTime);
				logger.info("BaseUI Cloning took " + executionTime + "ms.");
				startTime = System.currentTimeMillis();
				
				// apply mod's UI
				final File descIndexFile = new File(
						mod.getMpqCacheDirectory() + File.separator + mod.getDescIndexData().getDescIndexIntPath());
				catalogClone.processDescIndex(descIndexFile, raceId);
				catalogClone.clearParser();
				
				executionTime = (System.currentTimeMillis() - startTime);
				logger.info("Validating Layouts took " + executionTime + "ms.");
			} else {
				if (!repairLayoutOrder && verifyXml) {
					// only verify XML and nothing else
					final String[] extensions =
							{ "xml", "sc2layout", "stormlayout", "stormcomponents", "sc2components", "stormcutscene",
									"sc2cutscene", "stormstyle", "sc2style" };
					final Collection<File> filesOfCache =
							FileUtils.listFiles(mod.getMpqCacheDirectory(), extensions, true);
					verifyXml(filesOfCache);
				}
			}
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			logger.error("ERROR: encountered error while compiling.", e);
		}
		
		return catalogClone;
	}
	
	/**
	 * Creates a correct ordering of the layout files based on their internal dependencies.
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
	 * @param uiCatalog
	 * @return clone of the CatalogUI
	 */
	private UICatalog getClonedUICatalog(final UICatalog uiCatalog) {
		return (UICatalog) uiCatalog.deepCopy();
	}
	
	/**
	 * Verifies the syntax of the xml document.
	 */
	private void verifyXml(final Collection<File> files)
			throws IOException, SAXException, ParserConfigurationException {
		final DocumentBuilder dBuilder;
		dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());
		for (final File file : files) {
			dBuilder.parse(file);
		}
	}
}
