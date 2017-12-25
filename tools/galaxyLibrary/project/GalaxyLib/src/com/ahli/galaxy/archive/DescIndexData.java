package com.ahli.galaxy.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.ahli.mpq.MpqException;
import com.ahli.mpq.MpqInterface;
import com.ahli.util.Pair;

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
	
	private static final String XML_VERSION_1_0_ENCODING_UTF_8_STANDALONE_YES_DESC = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n<Desc>\r\n";
	
	private static final String UTF_8 = "UTF-8";
	
	private static Logger logger = LogManager.getLogger();
	
	private String descIndexIntPath = null;
	private ArrayList<Pair<File, String>> fileIntPathList = new ArrayList<>();
	private final MpqInterface mpqi;
	
	/**
	 * Constructor.
	 * 
	 * @param mpqi
	 *            MpqInterface
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
		return fileIntPathList.get(i).getValue();
	}
	
	/**
	 * @param intPath
	 * @throws MpqException
	 */
	public void addLayoutIntPath(final String intPath) throws MpqException {
		String intPath2 = intPath;
		File f = mpqi.getFileFromMpq(intPath);
		if (!f.exists()) {
			// add base folder to the path
			intPath2 = (mpqi.isHeroesMpq() ? "Base.StormData" : "Base.SC2Data") + "/" + intPath;
			f = mpqi.getFileFromMpq(intPath2);
		}
		if (!f.exists()) {
			return;
		}
		final Pair<File, String> p = new Pair<>(f, intPath2);
		fileIntPathList.add(p);
		logger.trace("added Layout path: " + intPath2);
		logger.trace("added File path: " + f.getAbsolutePath());
	}
	
	/**
	 * @param intPath
	 * @return
	 */
	public boolean removeLayoutIntPath(final String intPath) {
		for (int i = 0; i < fileIntPathList.size(); i++) {
			final Pair<File, String> p = fileIntPathList.get(i);
			if (p.getValue().equals(intPath)) {
				fileIntPathList.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 */
	public void clear() {
		fileIntPathList.clear();
	}
	
	/**
	 * @param descIndexPath
	 */
	public void setDescIndexPathAndClear(final String descIndexPath) {
		clear();
		setDescIndexIntPath(descIndexPath);
	}
	
	/**
	 * @param layoutPathList
	 * @throws MpqException
	 */
	public void addLayoutIntPath(final Iterable<String> layoutPathList) throws MpqException {
		final Iterator<String> it = layoutPathList.iterator();
		while (it.hasNext()) {
			this.addLayoutIntPath(it.next());
		}
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
	public File getLayoutFile(final String intPath) {
		for (int i = 0; i < fileIntPathList.size(); i++) {
			final Pair<File, String> p = fileIntPathList.get(i);
			if (p.getValue().equals(intPath)) {
				return p.getKey();
			}
		}
		return null;
	}
	
	/**
	 * @throws IOException
	 */
	public void persistDescIndexFile() throws IOException {
		final File f = mpqi.getFileFromMpq(descIndexIntPath);
		
		try (OutputStreamWriter bw = new OutputStreamWriter(new FileOutputStream(f, false), UTF_8)) {
			bw.write(XML_VERSION_1_0_ENCODING_UTF_8_STANDALONE_YES_DESC);
			
			for (final Pair<File, String> p : fileIntPathList) {
				bw.write(INCLUDE_PATH);
				bw.write(p.getValue());
				bw.write(STRING);
			}
			
			bw.write(DESC);
		} catch (final IOException e) {
			logger.error("ERROR writing DescIndex to disc" + e);
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
		final ArrayList<ArrayList<String>> dependencies = new ArrayList<>();
		final ArrayList<ArrayList<String>> ownConstants = new ArrayList<>();
		
		// grab dependencies and constant definitions for every layout file
		for (final Pair<File, String> pair : fileIntPathList) {
			ArrayList<String> layoutDeps = null;
			ArrayList<String> curConstants = null;
			
			curConstants = LayoutReader.getLayoutsConstantDefinitions(pair.getKey());
			
			// add calculated list of dependencies from layout file
			layoutDeps = LayoutReader.getDependencyLayouts(pair.getKey(), curConstants);
			logger.trace("Dependencies found: " + layoutDeps);
			
			ownConstants.add(curConstants);
			dependencies.add(layoutDeps);
		}
		
		boolean insertOccurred = true;
		for (int counter = 0; insertOccurred && counter < Math.pow(fileIntPathList.size(), 4); counter++) {
			logger.trace("counter=" + counter);
			insertOccurred = false;
			for (int i = 0; i < dependencies.size(); i++) {
				
				final ArrayList<String> curLayoutDepList = dependencies.get(i);
				final Pair<File, String> pair = fileIntPathList.get(i);
				
				for (int j = 0; j < curLayoutDepList.size(); j++) {
					
					String curDependencyTo = curLayoutDepList.get(j);
					
					if (curDependencyTo.startsWith(STRING2)) {
						logger.trace("DEPENDENCY: " + curDependencyTo);
						while (curDependencyTo.startsWith(STRING2)) {
							curDependencyTo = curDependencyTo.substring(1);
						}
						boolean constantDefinedBefore = false;
						// check if it appears before the template
						if (i > 0) {
							y: for (int i2 = 0; i2 < i; i2++) {
								// Pair<File, String> otherPair =
								// fileIntPathList.get(i2);
								// String fileName =
								// otherPair.getKey().getName();
								// fileName = fileName.substring(0,
								// fileName.lastIndexOf('.'));
								for (final String constant : ownConstants.get(i2)) {
									if (constant.equals(curDependencyTo)) {
										constantDefinedBefore = true;
										break y;
									}
								}
							}
						}
						if (!constantDefinedBefore) {
							y: for (int i2 = i + 1; i2 < dependencies.size(); i2++) {
								final Pair<File, String> otherPair = fileIntPathList.get(i2);
								String fileName = otherPair.getKey().getName();
								fileName = fileName.substring(0, fileName.lastIndexOf('.'));
								for (final String constant : ownConstants.get(i2)) {
									if (constant.equals(curDependencyTo)) {
										logger.trace("checked " + fileIntPathList.get(i).getKey().getName()
												+ " with dependency " + curDependencyTo + " and " + constant + " i=" + i
												+ " j=" + j + " i2=" + i2);
										logger.trace("fileIntPathList:" + fileIntPathList);
										// i's needs to be inserted after i2
										dependencies.remove(i);
										fileIntPathList.remove(i);
										i = i2;
										dependencies.add(i, curLayoutDepList);
										fileIntPathList.add(i, pair);
										constantDefinedBefore = true;
										logger.trace("inserted " + fileIntPathList.get(i).getKey().getName() + " after "
												+ fileName);
										logger.trace("fileIntPathList: " + fileIntPathList);
										break y;
									} else {
										logger.trace("checked " + fileIntPathList.get(i).getKey().getName()
												+ " with dependency " + curDependencyTo + " and " + constant + " i=" + i
												+ " j=" + j + " i2=" + i2);
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
		for (int counter = 0; insertOccurred && counter < Math.pow(fileIntPathList.size(), 4); counter++) {
			logger.trace("counter=" + counter);
			insertOccurred = false;
			x: for (int i = 0; i < dependencies.size(); i++) {
				
				final ArrayList<String> curLayoutDepList = dependencies.get(i);
				final Pair<File, String> pair = fileIntPathList.get(i);
				
				for (int j = 0; j < curLayoutDepList.size(); j++) {
					
					final String curDependencyTo = curLayoutDepList.get(j);
					
					if (!curDependencyTo.startsWith(STRING2)) {
						// check if it appears after the template
						for (int i2 = i + 1; i2 < dependencies.size(); i2++) {
							final Pair<File, String> otherPair = fileIntPathList.get(i2);
							String fileName = otherPair.getKey().getName();
							fileName = fileName.substring(0, fileName.lastIndexOf('.'));
							
							if (fileName.equals(curDependencyTo)) {
								logger.trace("checked " + fileIntPathList.get(i).getKey().getName()
										+ " with dependency " + curDependencyTo + " and " + fileName + " i=" + i + " j="
										+ j + " i2=" + i2);
								logger.trace("fileIntPathList:" + fileIntPathList);
								// i's needs to be inserted after i2
								dependencies.remove(i);
								fileIntPathList.remove(i);
								i = i2;
								dependencies.add(i, curLayoutDepList);
								fileIntPathList.add(i, pair);
								insertOccurred = true;
								logger.trace(
										"inserted " + fileIntPathList.get(i).getKey().getName() + " after " + fileName);
								logger.trace("fileIntPathList: " + fileIntPathList);
								break x;
							} else {
								logger.trace("checked " + fileIntPathList.get(i).getKey().getName()
										+ " with dependency " + curDependencyTo + " and " + fileName + " i=" + i + " j="
										+ j + " i2=" + i2);
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
