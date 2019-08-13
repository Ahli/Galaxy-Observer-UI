// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import com.ahli.mpq.i18n.Messages;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorSettingsInterface;
import com.ahli.util.DeepCopyable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implements a MpqInterface for Ladislav Zezula's MpqEditor.exe.
 *
 * @author Ahli
 */
public class MpqEditorInterface implements MpqInterface, DeepCopyable {
	private static final String EXECUTING = "executing: {}";
	private static final String EXECUTION_FINISHED = "execution finished";
	private static final char QUOTE = '\"';
	private static final Logger logger = LogManager.getLogger(MpqEditorInterface.class);
	private static final String MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND = "MpqInterface.MpqEditorNotFound";
	private static final String CMD_C = "cmd /C ";
	private MpqEditorSettingsInterface settings;
	private String mpqEditorPath;
	private String mpqCachePath;
	
	/**
	 * Constructor.
	 *
	 * @param mpqCachePath
	 * 		path of the directory that will temporarily contain the extracted mpq
	 * @param mpqEditorPath
	 * 		the path of the MpqEditor.exe
	 */
	public MpqEditorInterface(final String mpqCachePath, final String mpqEditorPath) {
		this.mpqCachePath = mpqCachePath;
		this.mpqEditorPath = mpqEditorPath;
		settings = new MpqEditorSettingsInterface();
	}
	
	/**
	 * Returns the file count in a folder including all subfolders.
	 *
	 * @param file
	 * @return
	 */
	public static int getFileCountInFolder(final File file) {
		final File[] files = file.listFiles();
		int count = 0;
		if (files != null) {
			for (final File f : files) {
				if (f.isFile()) {
					count++;
				} else {
					count += getFileCountInFolder(f);
				}
			}
		}
		return count;
	}
	
	/**
	 * @param f
	 * @return
	 */
	public static boolean deleteDir(final File f) throws IOException {
		if (f.isDirectory()) {
			final File[] content = f.listFiles();
			if (content != null) {
				for (int i = 0; i < content.length; i++) {
					if (!deleteDir(content[i])) {
						return false;
					}
				}
			}
		}
		Files.delete(f.toPath());
		return true;
	}
	
	/**
	 * Returns path with suffix via changing file name.
	 *
	 * @param absolutePath
	 * @return
	 */
	private static String getPathWithUnprotectedSuffix(final String absolutePath) {
		final int i = absolutePath.lastIndexOf('.');
		return absolutePath.substring(0, i < 0 ? absolutePath.length() : i) + "_unprtctd" +
				(i < 0 ? "" : absolutePath.substring(i));
	}
	
	/**
	 * Deletes a file of the specified path.
	 *
	 * @param path
	 * 		file path
	 * @throws MpqException
	 * 		if file is existing but could not be deleted
	 */
	private static void deleteFile(final String path) throws MpqException {
		final File fup = new File(path);
		if (fup.exists()) {
			if (fup.isFile()) {
				if (fup.canWrite()) {
					try {
						Files.delete(fup.toPath());
					} catch (final IOException e) {
						logger.error(String.format("ERROR: Could not delete file '%s'.", path), e);
						throw new MpqException(
								String.format(Messages.getString("MpqInterface.CouldNotOverwriteFile"), path), e);
					}
				} else {
					throw new MpqException(
							String.format("ERROR: Could not delete file '%s'. It might be used by another process.",
									path));
				}
			} else {
				throw new MpqException(
						String.format("ERROR: Could not delete file '%s'. A directory with the same name exists.",
								path));
			}
		}
	}
	
	/**
	 * Returns the cache path as String.
	 *
	 * @return cache path as String
	 */
	public String getMpqCachePath() {
		return mpqCachePath;
	}
	
	/**
	 * Sets the cache path.
	 *
	 * @param mpqCachePath2
	 * 		new cache path as String
	 */
	public void setMpqCachePath(final String mpqCachePath2) {
		mpqCachePath = mpqCachePath2;
	}
	
	/**
	 * Returns a cloned instance of this.
	 */
	@Override
	public Object deepCopy() {
		final MpqEditorInterface clone = new MpqEditorInterface(mpqCachePath, mpqEditorPath);
		clone.settings = (MpqEditorSettingsInterface) settings.deepCopy();
		return clone;
	}
	
	/**
	 * Build MPQ from cache.
	 *
	 * @param buildPath
	 * @param buildFileName
	 * @param compressXml
	 * @param compressMpq
	 * @param buildUnprotectedToo
	 * 		if protectMPQ, then this controls if an unprotected version is build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(final String buildPath, final String buildFileName, final boolean compressXml,
			final MpqEditorCompression compressMpq, final boolean buildUnprotectedToo)
			throws IOException, InterruptedException, MpqException {
		final String absolutePath = buildPath + File.separator + buildFileName;
		buildMpq(absolutePath, compressXml, compressMpq, buildUnprotectedToo);
	}
	
	/**
	 * Build MPQ from cache.
	 *
	 * @param absolutePath
	 * @param compressXml
	 * @param compressMpq
	 * @param buildUnprotectedToo
	 * 		if compressXml, then this controls if an unprotected version is build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(final String absolutePath, final boolean compressXml, final MpqEditorCompression compressMpq,
			final boolean buildUnprotectedToo) throws IOException, InterruptedException, MpqException {
		// create parent directory
		final File targetFile = new File(absolutePath);
		final File parentFolder = targetFile.getParentFile();
		if (parentFolder == null) {
			logger.error("ERROR: Could not receive parent directory of path: {}", absolutePath);
			throw new MpqException(String.format(Messages.getString("MpqInterface.CouldNotCreatePath"), absolutePath));
		}
		if (!parentFolder.exists() && !parentFolder.mkdirs()) {
			logger.error("ERROR: Could not create path: {}", parentFolder.getAbsolutePath());
			throw new MpqException(String.format(Messages.getString("MpqInterface.CouldNotCreatePath"),
					parentFolder.getAbsolutePath()));
		}
		
		// add 2 to be sure to have enough space for listfile and attributes
		final int fileCount = 2 + getFileCountInFolder(new File(mpqCachePath));
		
		if (compressXml) {
			
			if (buildUnprotectedToo) {
				// special unprotected file path
				final String unprotectedAbsolutePath = getPathWithUnprotectedSuffix(absolutePath);
				
				// make way for unprotected file
				deleteFile(unprotectedAbsolutePath);
				
				
				// build unprotected file
				buildMpqWithCompression(MpqEditorCompression.NONE, unprotectedAbsolutePath, fileCount);
			}
			
			// make way for protected file
			deleteFile(absolutePath);
			
			// extra file compression
			try {
				XmlCompressorDom.processCache(mpqCachePath, 1);
			} catch (final ParserConfigurationException | SAXException | TransformerConfigurationException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
			
			buildMpqWithCompression(compressMpq, absolutePath, fileCount);
			
		} else {
			// NO CONTENT COMPRESSION/PROTECTION OPTIONS
			
			// make way for file
			deleteFile(absolutePath);
			
			buildMpqWithCompression(compressMpq, absolutePath, fileCount);
		}
	}
	
	/**
	 * @param compressMpq
	 * @param absolutePath
	 * @param fileCount
	 * @throws IOException
	 * @throws MpqException
	 * @throws InterruptedException
	 */
	private void buildMpqWithCompression(final MpqEditorCompression compressMpq, final String absolutePath,
			final int fileCount) throws IOException, MpqException, InterruptedException {
		// mpq compression
		settings.setCompression(compressMpq);
		/* MpqEditor reads its settings from ini files in a specific location.
		   Multiple different compression settings would cause race conditions and problems. */
		synchronized (MpqEditorInterface.class) {
			settings.applyCompression();
			try {
				// build protected file
				newMpq(absolutePath, fileCount);
				addToMpq(absolutePath, mpqCachePath, "");
				compactMpq(absolutePath);
			} finally {
				settings.restoreOriginalSettingFiles();
			}
		}
	}
	
	/**
	 * @param mpqPath
	 * @param maxFileCount
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void newMpq(final String mpqPath, final int maxFileCount)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " n " + QUOTE + mpqPath + QUOTE + " " + maxFileCount +
						QUOTE;
		
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
	}
	
	/**
	 * @param mpqPath
	 * @param sourceFilePath
	 * @param targetName
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void addToMpq(final String mpqPath, final String sourceFilePath, final String targetName)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " a " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						sourceFilePath + QUOTE + " " + QUOTE + targetName + QUOTE + " /r" + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
	}
	
	/**
	 * @param mpqPath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void compactMpq(final String mpqPath) throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " compact " + QUOTE + mpqPath + QUOTE + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
		
	}
	
	/**
	 * @param mpqSourcePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void extractEntireMPQ(final String mpqSourcePath) throws InterruptedException, IOException, MpqException {
		logger.trace("mpqCachePath: {}", () -> mpqCachePath);
		logger.trace("mpqSourcePath: {}", () -> mpqSourcePath);
		
		clearCacheExtractedMpq();
		
		extractFromMpq(mpqSourcePath, "*", mpqCachePath, true);
		
		clearCacheListFile();
		clearCacheAttributesFile();
		
		if (getFileCountInFolder(new File(mpqCachePath)) <= 0) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.NoFilesExtracted"), mpqSourcePath));
		}
	}
	
	/**
	 * Removes the "(listfile)" file from the cache.
	 *
	 * @return whether the cache's list file was deleted
	 */
	private boolean clearCacheListFile() {
		final File f = new File(mpqCachePath + File.separator + "(listfile)");
		return !f.exists() || f.isDirectory() || f.delete();
	}
	
	/**
	 * Removes the "(attributes)" file from the cache.
	 *
	 * @return whether the cache's attribute file was deleted
	 */
	private boolean clearCacheAttributesFile() {
		final File f = new File(mpqCachePath + File.separator + "(attributes)");
		return !f.exists() || f.isDirectory() || f.delete();
	}
	
	/**
	 * Removes all files in the cache.
	 */
	public boolean clearCacheExtractedMpq() {
		final File f = new File(mpqCachePath);
		if (f.exists()) {
			try {
				deleteDir(f);
			} catch (final IOException e) {
				if (logger.isTraceEnabled()) {
					logger.error("clearing Cache FAILED", e);
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param mpqPath
	 * @param fileName
	 * @param targetPath
	 * @param inclSubFolders
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void extractFromMpq(final String mpqPath, final String fileName, final String targetPath,
			final boolean inclSubFolders) throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " e " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						fileName + QUOTE + " " + QUOTE + targetPath + QUOTE + (inclSubFolders ? " /fp" : "") + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
		
		// // MONITOR https://github.com/inwc3/JMPQ3 if it can handle sc2 files
		// someday to potentially replace MpqEditor.exe
		// File mpq = new File(mpqPath);
		// logging.trace("Extract from Mpq: "+mpq.getAbsolutePath());
		// JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.FORCE_V0);
		// File cache = new File(mpqCachePath);
		// cache.mkdirs();
		// logging.trace("Extract into: "+cache.getAbsolutePath());
		// editor.extractAllFiles(cache);
	}
	
	/**
	 * @param mpqPath
	 * @param scriptPath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void scriptMpq(final String mpqPath, final String scriptPath)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " s " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						scriptPath + QUOTE + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
	}
	
	/**
	 * Verifies the existence of the MPQEditor.exe at the stored file path.
	 *
	 * @return
	 */
	private boolean verifyMpqEditor() {
		final File f = new File(mpqEditorPath);
		return f.exists() && f.isFile();
	}
	
	/**
	 * @param mpqPath
	 * @param filePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void deleteFileInMpq(final String mpqPath, final String filePath)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " d " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						filePath + QUOTE + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
	}
	
	/**
	 * @param mpqPath
	 * @param oldfilePath
	 * @param newFilePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void renameFileInMpq(final String mpqPath, final String oldfilePath, final String newFilePath)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(
					String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND), mpqEditorPath));
		}
		final String cmd =
				CMD_C + QUOTE + QUOTE + mpqEditorPath + QUOTE + " r " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						oldfilePath + QUOTE + " " + QUOTE + newFilePath + QUOTE + QUOTE;
		logger.trace(EXECUTING, () -> cmd);
		Runtime.getRuntime().exec(cmd).waitFor();
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTION_FINISHED);
		}
	}
	
	/**
	 * Returns the path to a file from the cache with the specified internal path.
	 *
	 * @param intPath
	 * 		internal path
	 * @return path
	 */
	@Override
	public Path getFilePathFromMpq(final String intPath) {
		return Paths.get(mpqCachePath + File.separator + intPath);
	}
	
	/**
	 * Returns the componentList file.
	 *
	 * @return
	 */
	public File getComponentListFile() {
		File f = new File(mpqCachePath + File.separator + "ComponentList.StormComponents");
		if (!f.exists() || f.isDirectory()) {
			f = new File(mpqCachePath + File.separator + "ComponentList.SC2Components");
			if (!f.exists() || f.isDirectory()) {
				return null;
			}
		}
		return f;
	}
	
	/**
	 *
	 */
	@Override
	public boolean isHeroesMpq() throws MpqException {
		File f = new File(mpqCachePath + File.separator + "ComponentList.StormComponents");
		if (!f.exists() || f.isDirectory()) {
			if (logger.isTraceEnabled()) {
				logger.trace("file not found in archive: {}", f.getAbsolutePath());
			}
			f = new File(mpqCachePath + File.separator + "ComponentList.SC2Components");
			if (!f.exists() || f.isDirectory()) {
				logger.error("ERROR: archive has no ComponentList file.");
				throw new MpqException("ERROR: Cannot identify if file belongs to Heroes or SC2.");
			}
			return false;
		}
		return true;
	}
	
	@Override
	public File getCache() {
		return new File(mpqCachePath);
	}
	
	@Override
	public void setCache(final File cache) {
		mpqCachePath = cache.getPath();
	}
	
	/**
	 * Returns the custom ruleset for the file attributes and compression.
	 *
	 * @return
	 */
	public MpqEditorCompressionRule[] getCustomRuleSet() {
		return settings.getCustomRuleSet();
	}
	
	/**
	 * Sets custom rules for the file attributes and compression. To use it, the archive needs to use
	 * MpqEditorCompression.CUSTOM.
	 *
	 * @param rules
	 */
	public void setCustomCompressionRules(final MpqEditorCompressionRule... rules) {
		settings.setCustomRules(rules);
	}
	
	/**
	 * @return
	 */
	public String getMpqEditorPath() {
		return mpqEditorPath;
	}
	
	/**
	 * Sets the MpqEditor path.
	 *
	 * @param editorPath
	 * 		new editor path as String
	 */
	public void setMpqEditorPath(final String editorPath) {
		mpqEditorPath = editorPath;
	}
}
