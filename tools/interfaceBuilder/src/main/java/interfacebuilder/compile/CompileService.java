// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compile;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserVtd;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.util.SilentXmlSaxErrorHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
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
			final boolean verifyLayout, final boolean verifyXml, final String consoleSkinId)
			throws InterruptedException {
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
				logger.info("Checking and repairing the Layout order took {}ms.", executionTime);
			}
			if (verifyLayout) {
				startTime = System.currentTimeMillis();
				
				// validate catalog
				catalogClone = getClonedUICatalog(mod.getGameData().getUiCatalog());
				
				executionTime = (System.currentTimeMillis() - startTime);
				logger.info("BaseUI Cloning took {}ms.", executionTime);
				startTime = System.currentTimeMillis();
				
				catalogClone.setParser(new UICatalogParser(catalogClone, new XmlParserVtd(), true));
				
				// apply mod's UI
				final File descIndexFile = new File(
						mod.getMpqCacheDirectory() + File.separator + mod.getDescIndexData().getDescIndexIntPath());
				catalogClone.processDescIndex(descIndexFile, raceId, consoleSkinId);
				catalogClone.postProcessParsing();
				catalogClone.setParser(null);
				
				executionTime = (System.currentTimeMillis() - startTime);
				logger.info("Validating Layouts took {}ms.", executionTime);
			} else {
				if (!repairLayoutOrder && verifyXml) {
					// only verify XML and nothing else
					final String[] suffixes =
							{ ".xml", ".SC2Layout", ".stormlayout", ".stormcomponents", ".SC2Components",
									".stormcutscene", ".SC2Cutscene", ".stormstyle", ".SC2Style" };
					final Collection<File> filesOfCache = FileUtils
							.listFiles(mod.getMpqCacheDirectory(), new SuffixFileFilter(suffixes, IOCase.INSENSITIVE),
									TrueFileFilter.INSTANCE);
					
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
	private static void manageOrderOfLayoutFiles(final DescIndexData descIndex)
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
	private static UICatalog getClonedUICatalog(final UICatalog uiCatalog) {
		return uiCatalog != null ? (UICatalog) uiCatalog.deepCopy() : null;
	}
	
	/**
	 * Verifies the syntax of the xml document.
	 */
	private static void verifyXml(final Collection<File> files)
			throws IOException, SAXException, ParserConfigurationException {
		final DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setNamespaceAware(false);
		dbFac.setValidating(false);
		dbFac.setAttribute("http://xml.org/sax/features/external-general-entities", false);
		dbFac.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbFac.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbFac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbFac.setXIncludeAware(false);
		dbFac.setExpandEntityReferences(false);
		dbFac.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		final DocumentBuilder dBuilder = dbFac.newDocumentBuilder();
		dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());
		for (final File file : files) {
			dBuilder.parse(file);
		}
	}
}
