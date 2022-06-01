// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import com.ahli.mpq.i18n.Messages;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorSettingsInterface;
import com.ahli.util.DeepCopyable;
import com.ahli.util.FileCountingVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Implements a MpqInterface for Ladislav Zezula's MpqEditor.exe.
 *
 * @author Ahli
 */
public class MpqEditorInterface implements MpqInterface, DeepCopyable {
	private static final String EXECUTING = "executing: {}";
	private static final String EXECUTION_FINISHED = "execution finished";
	private static final char QUOTE = '\"';
	private static final String QUOTESTR = "\"";
	private static final Logger logger = LoggerFactory.getLogger(MpqEditorInterface.class);
	private static final String MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND = "MpqInterface.MpqEditorNotFound";
	private static final String CMD = "cmd";
	private static final String SLASH_C = "/C";
	private static final Object classWideLock = new Object();
	private MpqEditorSettingsInterface settings;
	private Path mpqEditorPath;
	private Path mpqCachePath;
	
	/**
	 * Constructor.
	 *
	 * @param mpqCachePath
	 * 		path of the directory that will temporarily contain the extracted mpq
	 * @param mpqEditorPath
	 * 		the path of the MpqEditor.exe
	 */
	public MpqEditorInterface(final Path mpqCachePath, final Path mpqEditorPath) {
		this.mpqCachePath = mpqCachePath;
		this.mpqEditorPath = mpqEditorPath;
		settings = new MpqEditorSettingsInterface();
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
	 * 		if buildUnprotectedToo, then this controls if an unprotected version is build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(
			final Path buildPath,
			final String buildFileName,
			final boolean compressXml,
			final MpqEditorCompression compressMpq,
			final boolean buildUnprotectedToo) throws IOException, InterruptedException, MpqException {
		final Path absolutePath = buildPath.resolve(buildFileName);
		buildMpq(absolutePath, compressXml, compressMpq, buildUnprotectedToo);
	}
	
	/**
	 * Build MPQ from cache.
	 *
	 * @param targetFile
	 * @param compressXml
	 * @param compressMpq
	 * @param buildUnprotectedToo
	 * 		if compressXml, then this controls if an unprotected version is build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(
			final Path targetFile,
			final boolean compressXml,
			final MpqEditorCompression compressMpq,
			final boolean buildUnprotectedToo) throws IOException, InterruptedException, MpqException {
		// create parent directory
		final Path parentFolder = targetFile.getParent();
		if (parentFolder == null) {
			logger.error("ERROR: Could not receive parent directory of path: {}", targetFile);
			throw new MpqException(String.format(Messages.getString("MpqInterface.CouldNotCreatePath"), targetFile));
		}
		if (!Files.exists(parentFolder)) {
			Files.createDirectories(parentFolder);
		}
		
		// add 2 to be sure to have enough space for listfile and attributes
		final long fileCount = 2L + getFileCountInFolder(mpqCachePath);
		
		if (compressXml) {
			
			if (buildUnprotectedToo) {
				// special unprotected file path
				final Path unprotectedPath = getPathWithUnprotectedSuffix(targetFile);
				
				// make way for unprotected file
				deleteFile(unprotectedPath);
				
				
				// build unprotected file
				buildMpqWithCompression(MpqEditorCompression.NONE, unprotectedPath.toString(), fileCount);
			}
			
			// make way for protected file
			deleteFile(targetFile);
			
			// extra file compression
			try {
				XmlCompressorDom.processCache(mpqCachePath, 1);
				//				XmlCompressorVtd.processCache(mpqCachePath, 1);
			} catch (final ParserConfigurationException | TransformerConfigurationException | IOException e) {
				logger.error("Error while compressing files in the cache", e);
			}
			
		} else {
			// NO CONTENT COMPRESSION/PROTECTION OPTIONS
			
			// make way for file
			deleteFile(targetFile);
			
		}
		buildMpqWithCompression(compressMpq, targetFile.toString(), fileCount);
	}
	
	/**
	 * Returns the file count in a folder including all subfolders.
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static int getFileCountInFolder(final Path path) throws IOException {
		if (!Files.exists(path)) {
			return 0;
		}
		final FileCountingVisitor fileVisitor = new FileCountingVisitor();
		Files.walkFileTree(path, fileVisitor);
		return fileVisitor.getCount();
	}
	
	/**
	 * Returns path with suffix via changing file name.
	 *
	 * @param targetFile
	 * @return
	 */
	private static Path getPathWithUnprotectedSuffix(final Path targetFile) {
		final String path = targetFile.toString();
		final int i = path.lastIndexOf('.');
		return Path.of(path.substring(0, i < 0 ? path.length() : i) + "_unprtctd" + (i < 0 ? "" : path.substring(i)));
	}
	
	/**
	 * Deletes a file of the specified path.
	 *
	 * @param path
	 * 		file path
	 * @throws MpqException
	 * 		if file does not exist, is not a file or the file could not be deleted
	 */
	private static void deleteFile(final Path path) throws MpqException {
		if (Files.exists(path)) {
			if (Files.isRegularFile(path)) {
				if (Files.isWritable(path)) {
					try {
						Files.delete(path);
					} catch (final IOException e) {
						logger.error(String.format("ERROR: Could not delete file '%s'.", path), e);
						throw new MpqException(String.format(Messages.getString("MpqInterface.CouldNotOverwriteFile"),
								path), e);
					}
				} else {
					throw new MpqException(String.format("ERROR: Could not delete file '%s'. It might be used by another process.",
							path));
				}
			} else {
				throw new MpqException(String.format("ERROR: Could not delete file '%s'. A directory with the same name exists.",
						path));
			}
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
	private void buildMpqWithCompression(
			final MpqEditorCompression compressMpq, final String absolutePath, final long fileCount)
			throws IOException, MpqException, InterruptedException {
		/* MpqEditor reads its settings from ini files in a specific location.
		   Multiple different compression settings would cause race conditions and problems. */
		final MpqEditorSettingsInterface settingsFinal = settings;
		// mpq compression
		settingsFinal.setCompression(compressMpq);
		final String cachePath = mpqCachePath.toString();
		synchronized (classWideLock) {
			settingsFinal.applyCompression();
			try {
				// build protected file
				newMpq(absolutePath, fileCount);
				addToMpq(absolutePath, cachePath, "");
				compactMpq(absolutePath);
			} finally {
				settingsFinal.restoreOriginalSettingFiles();
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
	public void newMpq(final String mpqPath, final long maxFileCount)
			throws InterruptedException, IOException, MpqException {
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " n " + QUOTE + mpqPath + QUOTE + " " + maxFileCount +
						QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
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
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " a " + QUOTE + mpqPath + QUOTE + " " + QUOTE +
						sourceFilePath + QUOTE + " " + QUOTE + targetName + QUOTE + " /r" + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
	}
	
	/**
	 * @param mpqPath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void compactMpq(final String mpqPath) throws InterruptedException, IOException, MpqException {
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " compact " + QUOTE + mpqPath + QUOTE + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
	}
	
	/**
	 * Verifies the existence of the MPQEditor.exe at the stored file path.
	 *
	 * @return
	 */
	private boolean isMissingMpqEditor() {
		return !Files.isExecutable(mpqEditorPath);
	}
	
	/**
	 * @param mpqSourcePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void extractEntireMPQ(final String mpqSourcePath) throws InterruptedException, IOException, MpqException {
		logger.trace("mpqCachePath: {}\nmpqSourcePath: {}", mpqCachePath, mpqSourcePath);
		
		clearCacheExtractedMpq();
		
		extractFromMpq(mpqSourcePath, "*", mpqCachePath.toString(), true);
		
		clearCacheListFile();
		clearCacheAttributesFile();
		
		if (getFileCountInFolder(mpqCachePath) <= 0L) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.NoFilesExtracted"), mpqSourcePath));
		}
	}
	
	/**
	 * Removes all files in the cache.
	 */
	public boolean clearCacheExtractedMpq() {
		final Path path = mpqCachePath;
		if (Files.exists(path)) {
			try {
				deleteDir(path);
			} catch (final IOException e) {
				logger.error("clearing Cache FAILED", e);
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
	public void extractFromMpq(
			final String mpqPath, final String fileName, final String targetPath, final boolean inclSubFolders)
			throws InterruptedException, IOException, MpqException {
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " e " + QUOTE + mpqPath + QUOTE + " " + QUOTE + fileName +
						QUOTE + " " + QUOTE + targetPath + QUOTE + (inclSubFolders ? " /fp" : "") + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
	}
	
	/**
	 * Removes the "(listfile)" file from the cache.
	 */
	private void clearCacheListFile() throws IOException {
		final Path path = mpqCachePath.resolve("(listfile)");
		if (!Files.isDirectory(path)) {
			Files.deleteIfExists(path);
		}
	}
	
	/**
	 * Removes the "(attributes)" file from the cache.
	 */
	private void clearCacheAttributesFile() throws IOException {
		final Path path = mpqCachePath.resolve("(attributes)");
		if (!Files.isDirectory(path)) {
			Files.deleteIfExists(path);
		}
	}
	
	/**
	 * @param path
	 * @return
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void deleteDir(final Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (final Stream<Path> walk = Files.walk(path)) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		} else {
			Files.delete(path);
		}
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
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " s " + QUOTE + mpqPath + QUOTE + " " + QUOTE + scriptPath +
						QUOTE + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
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
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " d " + QUOTE + mpqPath + QUOTE + " " + QUOTE + filePath +
						QUOTE + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
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
		if (isMissingMpqEditor()) {
			throw new MpqException(String.format(Messages.getString(MPQ_INTERFACE_MPQ_EDITOR_NOT_FOUND),
					mpqEditorPath));
		}
		final String[] cmd = new String[] { CMD, SLASH_C,
				QUOTESTR + QUOTE + mpqEditorPath + QUOTE + " r " + QUOTE + mpqPath + QUOTE + " " + QUOTE + oldfilePath +
						QUOTE + " " + QUOTE + newFilePath + QUOTE + QUOTE };
		if (logger.isTraceEnabled()) {
			logger.trace(EXECUTING, Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		logger.trace(EXECUTION_FINISHED);
	}
	
	/**
	 * Returns the path to a file from the cache with the specified internal path.
	 *
	 * @param internalPath
	 * 		internal path
	 * @return path
	 */
	@Override
	public Path getFilePathFromMpq(final String internalPath) {
		return mpqCachePath.resolve(internalPath);
	}
	
	/**
	 * Returns the componentList file.
	 *
	 * @return
	 */
	public Path getComponentListFile() {
		Path path = mpqCachePath.resolve("ComponentList.StormComponents");
		if (!Files.isRegularFile(path)) {
			path = mpqCachePath.resolve("ComponentList.SC2Components");
			if (!Files.isRegularFile(path)) {
				return null;
			}
		}
		return path;
	}
	
	/**
	 *
	 */
	@Override
	public boolean isHeroesMpq() throws MpqException {
		Path path = mpqCachePath.resolve("ComponentList.StormComponents");
		if (!Files.isRegularFile(path)) {
			path = mpqCachePath.resolve("ComponentList.SC2Components");
			if (!Files.isRegularFile(path)) {
				logger.error("ERROR: archive has no ComponentList file.");
				throw new MpqException("ERROR: Cannot identify if file belongs to Heroes or SC2.");
			}
			return false;
		}
		return true;
	}
	
	@Override
	public Path getCache() {
		return mpqCachePath;
	}
	
	@Override
	public void setCache(final Path cache) {
		mpqCachePath = cache;
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
	public Path getMpqEditorPath() {
		return mpqEditorPath;
	}
	
	/**
	 * Sets the MpqEditor path.
	 *
	 * @param editorPath
	 * 		new editor path as String
	 */
	public void setMpqEditorPath(final Path editorPath) {
		mpqEditorPath = editorPath;
	}
}
