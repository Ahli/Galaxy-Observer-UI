// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.archive;

import com.ahli.mpq.MpqException;
import com.ahli.mpq.MpqInterface;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
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
@Slf4j
public class DescIndex {
	private static final String STRING2 = "#";
	
	private static final String DESC = "</Desc>\r\n";
	
	private static final String STRING = "\"/>\r\n";
	
	private static final String INCLUDE_PATH = "    <Include path=\"";
	
	private static final String XML_VERSION_1_0_ENCODING_UTF_8_STANDALONE_YES_DESC =
			"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n<Desc>\r\n";
	
	private static final String CHECKED_WITH_DEPENDENCY_AND_I_J_I2 =
			"checked {} with dependency {} and {} i={} j={} i2={}";
	private static final String FILE_INT_PATH_LIST = "fileIntPathList: {}";
	
	private final MpqInterface mpqi;
	private final List<Pair<Path, String>> fileIntPathList;
	/**
	 * -- GETTER --
	 *
	 * @return
	 */
	@Getter
	private String descIndexIntPath;
	
	/**
	 * Constructor.
	 *
	 * @param mpqi
	 * 		MpqInterface
	 */
	public DescIndex(@NotNull final MpqInterface mpqi) {
		this.mpqi = mpqi;
		fileIntPathList = new ArrayList<>();
	}
	
	/**
	 * @param descIndexPath
	 */
	public void setDescIndexPathAndClear(@NotNull final String descIndexPath) {
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
	 * @param descIndexIntPath
	 */
	public void setDescIndexIntPath(@NotNull final String descIndexIntPath) {
		this.descIndexIntPath = descIndexIntPath;
	}
	
	/**
	 * @param layoutPathList
	 * @throws MpqException
	 */
	public void addLayoutIntPath(@NotNull final Iterable<String> layoutPathList) throws MpqException {
		for (final String aLayoutPathList : layoutPathList) {
			addLayoutIntPath(aLayoutPathList);
		}
	}
	
	/**
	 * @param intPath
	 * @throws MpqException
	 */
	public void addLayoutIntPath(@NotNull final String intPath) throws MpqException {
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
		
		log.trace("added Layout path: {}\nadded File path: {}", intPath2, p);
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
			throw new IOException("ERROR writing Descindex to disc.", e);
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
		for (final Pair<Path, String> pair : fileIntPathList) {
			final File f = pair.getOne().toFile();
			final List<String> curConstants = LayoutReaderDom.getLayoutsConstantDefinitions(f);
			
			// add calculated list of dependencies from layout file
			final List<String> layoutDeps = LayoutReaderDom.getDependencyLayouts(f, curConstants);
			log.trace("Dependencies found: {}", layoutDeps);
			
			ownConstants.add(curConstants);
			dependencies.add(layoutDeps);
		}
		
		boolean insertOccurred = true;
		final int end = (int) Math.pow(fileIntPathList.size(), 4);
		for (int counter = 0; insertOccurred && counter < end; ++counter) {
			log.trace("counter={}", counter);
			insertOccurred = false;
			for (int i = 0, len = dependencies.size(); i < len; ++i) {
				
				final List<String> curLayoutDepList = dependencies.get(i);
				final Pair<Path, String> pair = fileIntPathList.get(i);
				
				for (int j = 0, len2 = curLayoutDepList.size(); j < len2; ++j) {
					
					String curDependencyTo = curLayoutDepList.get(j);
					
					if (curDependencyTo.startsWith(STRING2)) {
						log.trace("DEPENDENCY: {}", curDependencyTo);
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
										if (log.isTraceEnabled()) {
											log.trace(
													CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
													fileIntPathList.get(i).getOne().getFileName(),
													curDependencyTo,
													constant,
													i,
													j,
													i2);
											log.trace(FILE_INT_PATH_LIST, fileIntPathList);
										}
										// i's needs to be inserted after i2
										dependencies.remove(i);
										fileIntPathList.remove(i);
										i = i2;
										dependencies.add(i, curLayoutDepList);
										fileIntPathList.add(i, pair);
										//constantDefinedBefore = true;
										if (log.isTraceEnabled()) {
											log.trace(
													"inserted {} after {}",
													fileIntPathList.get(i).getOne().getFileName(),
													fileName);
											log.trace(FILE_INT_PATH_LIST, fileIntPathList);
										}
										break y;
									} else {
										if (log.isTraceEnabled()) {
											log.trace(
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
		for (int counter = 0; insertOccurred && counter < end; ++counter) {
			log.trace("counter={}", counter);
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
								if (log.isTraceEnabled()) {
									log.trace(
											CHECKED_WITH_DEPENDENCY_AND_I_J_I2,
											fileIntPathList.get(i).getOne().getFileName(),
											curDependencyTo,
											fileName,
											i,
											j,
											i2);
									log.trace(FILE_INT_PATH_LIST, fileIntPathList);
								}
								// i's needs to be inserted after i2
								dependencies.remove(i);
								fileIntPathList.remove(i);
								i = i2;
								dependencies.add(i, curLayoutDepList);
								fileIntPathList.add(i, pair);
								insertOccurred = true;
								if (log.isTraceEnabled()) {
									log.trace(
											"inserted {} after {}",
											fileIntPathList.get(i).getOne().getFileName(),
											fileName);
									log.trace(FILE_INT_PATH_LIST, fileIntPathList);
								}
								break x;
							} else {
								if (log.isTraceEnabled()) {
									log.trace(
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
