// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.compile;

import com.ahli.galaxy.ModD;
import com.ahli.galaxy.archive.DescIndex;
import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.parser.DeduplicationIntensity;
import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserVtd;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.util.XmlDomHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Compiles MPQ stuff.
 *
 * @author Ahli
 */
@Log4j2
public class CompileService {
	
	private final GameService gameService;
	
	public CompileService(@NonNull final GameService gameService) {
		this.gameService = gameService;
	}
	
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
	public UICatalog compile(
			@NonNull final ModD mod,
			@NonNull final String raceId,
			final boolean repairLayoutOrder,
			final boolean verifyLayout,
			final boolean verifyXml,
			@NonNull final String consoleSkinId) throws InterruptedException {
		UICatalog catalogClone = null;
		try {
			long startTime;
			long executionTime;
			
			// manage descIndexData
			final DescIndex descIndex = mod.getDescIndex();
			if (repairLayoutOrder) {
				startTime = System.currentTimeMillis();
				manageOrderOfLayoutFiles(descIndex);
				executionTime = (System.currentTimeMillis() - startTime);
				log.info("Checking and repairing the Layout order took {}ms.", executionTime);
			}
			if (verifyLayout) {
				startTime = System.currentTimeMillis();
				
				// validate catalog
				catalogClone = getClonedUICatalog(mod.getGame().getUiCatalog());
				
				executionTime = (System.currentTimeMillis() - startTime);
				log.info("BaseUI Cloning took {}ms.", executionTime);
				startTime = System.currentTimeMillis();
				
				if (catalogClone == null) {
					return null;
				}
				
				catalogClone.setParser(new UICatalogParser(catalogClone,
						new XmlParserVtd(),
						DeduplicationIntensity.SIMPLE));
				
				// apply mod's UI
				final Path descIndexFile =
						Path.of(mod.getMpqCacheDirectory() + File.separator + mod.getDescIndex().getDescIndexIntPath());
				catalogClone.processDescIndex(descIndexFile, raceId, consoleSkinId);
				catalogClone.postProcessParsing();
				catalogClone.setParser(null);
				
				executionTime = (System.currentTimeMillis() - startTime);
				log.info("Validating Layouts took {}ms.", executionTime);
			} else {
				if (!repairLayoutOrder && verifyXml) {
					// only verify XML and nothing else
					final DocumentBuilder dBuilder = XmlDomHelper.buildSecureDocumentBuilder(true, false);
					
					final GameDef sc2GameDef = gameService.getGameDef(GameType.SC2);
					final GameDef heroesGameDef = gameService.getGameDef(GameType.HEROES);
					
					final String[] extensions = { heroesGameDef.layoutFileEnding(), sc2GameDef.layoutFileEnding(),
							heroesGameDef.componentsFileEnding(), sc2GameDef.componentsFileEnding(),
							heroesGameDef.cutsceneFileEnding(), sc2GameDef.cutsceneFileEnding(),
							heroesGameDef.styleFileEnding(), sc2GameDef.styleFileEnding(), "xml" };
					
					final FileVisitor<Path> visitor = new XmlVerifier(dBuilder, extensions);
					Files.walkFileTree(mod.getMpqCacheDirectory(), visitor);
				}
			}
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			log.error("ERROR: encountered error while compiling.", e);
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
	private static void manageOrderOfLayoutFiles(@NonNull final DescIndex descIndex)
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
	
	private static final class XmlVerifier extends SimpleFileVisitor<Path> {
		
		private final DocumentBuilder dBuilder;
		private final String[] extensions;
		
		private XmlVerifier(final DocumentBuilder dBuilder, final String[] extensions) {
			this.dBuilder = dBuilder;
			this.extensions = extensions;
		}
		
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			final String fileName = file.getFileName().toString();
			final int i = fileName.lastIndexOf('.');
			if (i >= 0) {
				final String curExt = fileName.substring(i + 1);
				boolean found = false;
				for (final String ext : extensions) {
					if (ext.equalsIgnoreCase(curExt)) {
						found = true;
						break;
					}
				}
				if (found) {
					try (final InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
						
						dBuilder.parse(is);
						
					} catch (final SAXException e) {
						log.trace("Error while verifying XML.", e);
						throw new IOException("XML verification failed.", e);
					}
				}
			}
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
			log.error("Failed to access file: {}", file);
			throw exc;
		}
	}
}
