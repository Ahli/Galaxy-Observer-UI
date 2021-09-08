// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.archive;

import com.ahli.mpq.MpqException;
import com.ahli.mpq.MpqInterface;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the Data of a Desc Index File.
 *
 * @author Ahli
 */
// TODO rebuild to use the UI object model instead of parsing all layout files
// another time
public class DescIndexData {
	private static final String STRING2 = "#";
	
	private static final String DESC = "</Desc>\r\n";
	
	private static final String STRING = "\"/>\r\n";
	
	private static final String INCLUDE_PATH = "    <Include path=\"";
	
	private static final String XML_VERSION_1_0_ENCODING_UTF_8_STANDALONE_YES_DESC =
			"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n<Desc>\r\n";
	
	private static final Logger logger = LoggerFactory.getLogger(DescIndexData.class);
	private static final String CHECKED_WITH_DEPENDENCY_AND_I_J_I2 =
			"checked {} with dependency {} and {} i={} j={} i2={}";
	private static final String FILE_INT_PATH_LIST = "fileIntPathList: {}";
	
	private final MpqInterface mpqi;
	private final List<Pair<Path, String>> fileIntPathList;
	private String descIndexIntPath;
	
	/**
	 * Constructor.
	 *
	 * @param mpqi
	 * 		MpqInterface
	 */
	public DescIndexData(final MpqInterface mpqi) {
		this.mpqi = mpqi;
		fileIntPathList = new ArrayList<>();
	}
	
	/**
	 * @return
	 */
	public String getDescIndexIntPath() {
		return descIndexIntPath;
	}
	
	/**
	 * @param descIndexIntPath
	 */
	public void setDescIndexIntPath(final String descIndexIntPath) {
		this.descIndexIntPath = descIndexIntPath;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public String getLayoutIntPath(final int i) {
		return fileIntPathList.get(i).getTwo();
	}
	
	/**
	 * @param intPath
	 * @return
	 */
	public boolean removeLayoutIntPath(final String intPath) {
		for (int i = 0, len = fileIntPathList.size(); i < len; ++i) {
			final Pair<Path, String> p = fileIntPathList.get(i);
			if (p.getTwo().equals(intPath)) {
				fileIntPathList.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param descIndexPath
	 */
	public void setDescIndexPathAndClear(final String descIndexPath) {
		clear();
		setDescIndexIntPath(descIndexPath);
	}
	
	/**
	 *
	 */
	public void clear() {
		fileIntPathList.clear();
	}
	
	/**
	 * @param layoutPathList
	 * @throws MpqException
	 */
	public void addLayoutIntPath(final Iterable<String> layoutPathList) throws MpqException {
		for (final String aLayoutPathList : layoutPathList) {
			addLayoutIntPath(aLayoutPathList);
		}
	}
	
	/**
	 * @param intPath
	 * @throws MpqException
	 */
	public void addLayoutIntPath(final String intPath) throws MpqException {
		String intPath2 = intPath;
		Path p = mpqi.getFilePathFromMpq(intPath);
		if (!Files.exists(p)) {
			// add base folder to the path
			intPath2 = (mpqi.isHeroesMpq() ? "base.stormdata" : "Base.SC2Data") + File.separator + intPath;
			p = mpqi.getFilePathFromMpq(intPath2);
			if (!Files.exists(p)) {
				return;
			}
		}
		fileIntPathList.add(Tuples.pair(p, intPath2));
		
		logger.trace("added Layout path: {}\nadded File path: {}", intPath2, p);
	}
	
	/**
	 * @return
	 */
	public int getLayoutCount() {
		return fileIntPathList.size();
	}
	
	/**
	 * @param intPath
	 * @return
	 */
	public Path getLayoutFilePath(final String intPath) {
		for (final Pair<Path, String> p : fileIntPathList) {
			if (p.getTwo().equals(intPath)) {
				return p.getOne();
			}
		}
		return null;
	}
	
	/**
	 * @throws IOException
	 */
	public void persistDescIndexFile() throws IOException {
		final Path p = mpqi.getFilePathFromMpq(descIndexIntPath);
		try (final BufferedWriter bw = Files.newBufferedWriter(p)) {
			bw.write(XML_VERSION_1_0_ENCODING_UTF_8_STANDALONE_YES_DESC);
			
			for (final Pair<Path, String> curPair : fileIntPathList) {
				bw.write(INCLUDE_PATH);
				bw.write(curPair.getTwo());
				bw.write(STRING);
			}
			
			bw.write(DESC);
		} catch (final IOException e) {
			logger.error("ERROR writing DescIndex to disc.", e);
			throw e;
		}
	}
	
	/**
	 * Orders layout files in DescIndex.
	 *
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void orderLayoutFiles() throws ParserConfigurationException, SAXException, IOException {
		final List<List<String>> dependencies = new ArrayList<>();
		final List<List<String>> ownConstants = new ArrayList<>();
		
		// grab dependencies and constant definitions for every layout file
		List<String> layoutDeps;
		List<String> curConstants;
		for (final Pair<Path, String> pair : fileIntPathList) {
			final File f = pair.getOne().toFile();
			curConstants = LayoutReaderDom.getLayoutsConstantDefinitions(f);
			
			// add calculated list of dependencies from layout file
			layoutDeps = LayoutReaderDom.getDependencyLayouts(f, curConstants);
			logger.trace("Dependencies found: {}", layoutDeps);
			
			ownConstants.add(curConstants);
			dependencies.add(layoutDeps);
		}
		
		boolean insertOccurred = true;
		for (int counter = 0; insertOccurred && counter < Math.pow(fileIntPathList.size(), 4); ++counter) {
			logger.trace("counter={}", counter);
			insertOccurred = false;
			for (int i = 0, len = dependencies.size(); i < len; ++i) {
				
				final List<String> curLayoutDepList = dependencies.get(i);
				final Pair<Path, String> pair = fileIntPathList.get(i);
				
				for (int j = 0, len2 = curLayoutDepList.size(); j < len2; ++j) {
					
					String curDependencyTo = curLayoutDepList.get(j);
					
					if (curDependencyTo.startsWith(STRING2)) {
						logger.trace("DEPENDENCY: {}", curDependencyTo);
						while (curDependencyTo.startsWith(STRING2)) {
							curDependencyTo = curDependencyTo.substring(1);
						}
						boolean constantNotDefinedBefore = true;
						// check if it appears before the template
						if (i > 0) {
							y:
							for (int i2 = 0; i2 < i; ++i2) {
								// Pair<File, String> otherPair =
								// fileIntPathList.get(i2);
								// String fileName =
								// otherPair.getKey().getName();
								// fileName = fileName.substring(0,
								// fileName.lastIndexOf('.'));
								for (final String constant : ownConstants.get(i2)) {
									if (constant.equals(curDependencyTo)) {
										constantNotDefinedBefore = false;
										break y;
									}
								}
							}
						}
						if (constantNotDefinedBefore) {
							y:
							for (int i2 = i + 1, len3 = dependencies.size(); i2 < len3; ++i2) {
								final Pair<Path, String> otherPair = fileIntPathList.get(i2);
								String fileName = otherPair.getOne().getFileName().toString();
								final int dotIndex = fileName.lastIndexOf('.');
								if (dotIndex > 0) {
									fileName = fileName.substring(0, dotIndex);
								}
								for (final String constant : ownConstants.get(i2)) {
									if (constant.equals(curDependencyTo)) {
										if (logger.isTraceEnabled()) {
											logger.trace(
													CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
													fileIntPathList.get(i).getOne().getFileName(),
													curDependencyTo,
													constant,
													i,
													j,
													i2);
											logger.trace(FILE_INT_PATH_LIST, fileIntPathList);
										}
										// i's needs to be inserted after i2
										dependencies.remove(i);
										fileIntPathList.remove(i);
										i = i2;
										dependencies.add(i, curLayoutDepList);
										fileIntPathList.add(i, pair);
										//constantDefinedBefore = true;
										if (logger.isTraceEnabled()) {
											logger.trace(
													"inserted {} after {}",
													fileIntPathList.get(i).getOne().getFileName(),
													fileName);
											logger.trace(FILE_INT_PATH_LIST, fileIntPathList);
										}
										break y;
									} else {
										if (logger.isTraceEnabled()) {
											logger.trace(
													CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
													fileIntPathList.get(i).getOne().getFileName(),
													curDependencyTo,
													constant,
													i,
													j,
													i2);
										}
									}
								}
							}
						}
					}
					// else {
					// // templates
					// }
				}
				
			}
		}
		
		// change order according to templates
		insertOccurred = true;
		for (int counter = 0; insertOccurred && counter < Math.pow(fileIntPathList.size(), 4); ++counter) {
			logger.trace("counter={}", counter);
			insertOccurred = false;
			x:
			for (int i = 0; i < dependencies.size(); ++i) {
				
				final List<String> curLayoutDepList = dependencies.get(i);
				final Pair<Path, String> pair = fileIntPathList.get(i);
				
				for (int j = 0; j < curLayoutDepList.size(); ++j) {
					
					final String curDependencyTo = curLayoutDepList.get(j);
					
					if (!curDependencyTo.startsWith(STRING2)) {
						// check if it appears after the template
						for (int i2 = i + 1; i2 < dependencies.size(); ++i2) {
							final Pair<Path, String> otherPair = fileIntPathList.get(i2);
							String fileName = otherPair.getOne().getFileName().toString();
							final int dotIndex = fileName.lastIndexOf('.');
							if (dotIndex >= 0) {
								fileName = fileName.substring(0, dotIndex);
							}
							
							if (fileName.equals(curDependencyTo)) {
								if (logger.isTraceEnabled()) {
									logger.trace(
											CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
											fileIntPathList.get(i).getOne().getFileName(),
											curDependencyTo,
											fileName,
											i,
											j,
											i2);
									logger.trace(FILE_INT_PATH_LIST, fileIntPathList);
								}
								// i's needs to be inserted after i2
								dependencies.remove(i);
								fileIntPathList.remove(i);
								i = i2;
								dependencies.add(i, curLayoutDepList);
								fileIntPathList.add(i, pair);
								insertOccurred = true;
								if (logger.isTraceEnabled()) {
									logger.trace(
											"inserted {} after {}",
											fileIntPathList.get(i).getOne().getFileName(),
											fileName);
									logger.trace(FILE_INT_PATH_LIST, fileIntPathList);
								}
								break x;
							} else {
								if (logger.isTraceEnabled()) {
									logger.trace(
											CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
											fileIntPathList.get(i).getOne().getFileName(),
											curDependencyTo,
											fileName,
											i,
											j,
											i2);
								}
							}
						}
						
					}
					// else {
					// // constants
					// }
				}
				
			}
		}
		
		// 1. layouts durchgehen und Abhaengigkeiten speichern
		// Abhaengigkeit:
		// - template aus anderem layout aus dieser Liste benutzen
		// (template="filename/...")
		// - constant benutzen, die in anderem layout definiert wird und nicht
		// im eigenen (#abc oder ##abc)
		// 2. layouts anordnen, sodass keine Abhaengigkeit mehr vorhanden sind
		
	}
	
}
