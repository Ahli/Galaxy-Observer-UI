package com.ahli.mpq;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.i18n.Messages;

/**
 * 
 * @author Ahli
 *
 */
public class MpqInterface {
	static Logger LOGGER = LogManager.getLogger("MpqInterface"); //$NON-NLS-1$

	private final String TEMP_DIR = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
	private String MPQ_EDITOR = "plugins" + File.separator + "mpq" + File.separator + "MPQEditor.exe"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private String mpqCachePath = TEMP_DIR + "ObserverUiSettingsEditor" + File.separator + "_ExtractedMpq"; //$NON-NLS-1$ //$NON-NLS-2$

	public String getMpqCachePath() {
		return mpqCachePath;
	}

	public void setMpqCachePath(String mpqCachePath) {
		this.mpqCachePath = mpqCachePath;
	}

	public void setMpqEditorPath(String editorPath) {
		this.MPQ_EDITOR = editorPath;
	}

	/**
	 * Returns a cloned instance of this.
	 */
	protected Object clone() {
		MpqInterface clone = new MpqInterface();
		clone.setMpqCachePath(this.mpqCachePath);
		clone.setMpqEditorPath(this.MPQ_EDITOR);
		return clone;
	}

	/**
	 * Build MPQ from cache.
	 * 
	 * @param buildPath
	 * @param buildFileName
	 * @param protectMPQ
	 * @param buildBoth
	 *            if protectMPQ, then this controls if an unprotected version is
	 *            build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(String buildPath, String buildFileName, boolean protectMPQ, boolean buildBoth)
			throws IOException, InterruptedException, MpqException {
		String absolutePath = buildPath + File.separator + buildFileName;
		buildMpq(absolutePath, protectMPQ, buildBoth);
	}

	/**
	 * Build MPQ from cache.
	 * 
	 * @param absolutePath
	 * @param protectMPQ
	 * @param buildBoth
	 *            if protectMPQ, then this controls if an unprotected version is
	 *            build, too
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MpqException
	 */
	public void buildMpq(String absolutePath, boolean protectMPQ, boolean buildBoth)
			throws IOException, InterruptedException, MpqException {
		// add 2 to be sure to have enough space for listfile and attributes
		int fileCount = 2 + getFileCountInFolder(new File(mpqCachePath));

		if (protectMPQ) {

			if (buildBoth) {
				// special unprotected file path
				String unprotectedAbsolutePath = getPathWithSuffix(absolutePath, "_unprtctd"); //$NON-NLS-1$

				// make way for unprotected file
				File fup = new File(unprotectedAbsolutePath);
				if (fup.exists() && fup.isFile()) {
					if (!fup.delete()) {
						String msg = "ERROR: Could not delete file " + unprotectedAbsolutePath; //$NON-NLS-1$
						LOGGER.error(msg);
						throw new MpqException(
								String.format(Messages.getString("MpqInterface.CouldNotOverwriteFile"), absolutePath)); //$NON-NLS-1$
					}
				}

				// build unprotected file
				newMpq(unprotectedAbsolutePath, fileCount);
				addToMpq(unprotectedAbsolutePath, mpqCachePath, ""); //$NON-NLS-1$
				compactMpq(unprotectedAbsolutePath);
			}

			// make way for protected file
			File f = new File(absolutePath);
			if (f.exists() && f.isFile()) {
				if (!f.delete()) {
					String msg = "ERROR: Could not delete file " + absolutePath; //$NON-NLS-1$
					LOGGER.error(msg);
					throw new MpqException(
							String.format(Messages.getString("MpqInterface.CouldNotOverwriteFile"), absolutePath)); //$NON-NLS-1$
				}
			}

			////////////////////////
			// PROTECTION MEASURES
			////////////////////////
			// doesn't work
			// deleteListFile(absolutePathTMP);
			// doesn't work
			// renameFileInMpq(absolutePathTMP, "_Ahli.StormLayout",
			//////////////////////// "_QQQ.StormLayout");
			// renameFileInMpq(absolutePathTMP, "(listfile)", "QQQ");
			// doesn't work
			// writeWrongFileAmount(absolutePathTMP, absolutePath);

			// extra compression
			// try {
			// XmlCompressor.processCache(mpqCachePath, 1);
			//
			// } catch (ParserConfigurationException | SAXException e) {
			// e.printStackTrace();
			// }

			// build protected file
			newMpq(absolutePath, fileCount);
			addToMpq(absolutePath, mpqCachePath, ""); //$NON-NLS-1$
			compactMpq(absolutePath);

		} else {
			// NO PROTECTION OPTION

			// make way for file
			File f = new File(absolutePath);
			if (f.exists() && f.isFile()) {
				if (!f.delete()) {
					String msg = "ERROR: Could not delete file " + absolutePath; //$NON-NLS-1$
					LOGGER.error(msg);
					throw new MpqException(
							String.format(Messages.getString("MpqInterface.CouldNotOverwriteFile"), absolutePath)); //$NON-NLS-1$
				}
			}

			// build unprotected file
			newMpq(absolutePath, fileCount);
			addToMpq(absolutePath, mpqCachePath, ""); //$NON-NLS-1$
			compactMpq(absolutePath);

		}

	}

	/**
	 * Returns path with suffix via changing file name.
	 * 
	 * @param absolutePath
	 * @return
	 */
	private String getPathWithSuffix(String absolutePath, String suffix) {
		int i = absolutePath.lastIndexOf('.');
		return absolutePath.substring(0, i) + suffix + absolutePath.substring(i);
	}

	/**
	 * Returns the file count in a folder including all subfolders.
	 * 
	 * @param file
	 * @return
	 */
	public static int getFileCountInFolder(File file) {
		File[] files = file.listFiles();
		int count = 0;
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory())
					count += getFileCountInFolder(f);
				else {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 
	 * @param mpqSourcePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void extractEntireMPQ(String mpqSourcePath) throws InterruptedException, IOException, MpqException {
		LOGGER.trace("mpqCachePath: " + mpqCachePath); //$NON-NLS-1$
		LOGGER.trace("mpqSourcePath: " + mpqSourcePath); //$NON-NLS-1$

		clearCacheExtractedMpq();

		extractFromMpq(mpqSourcePath, "*", mpqCachePath, true); //$NON-NLS-1$

		clearCacheListFile();
		clearCacheAttributesFile();

		if (getFileCountInFolder(new File(mpqCachePath)) <= 0) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.NoFilesExtracted"), mpqSourcePath)); //$NON-NLS-1$
		}
	}

	/**
	 * Removes the "(listfile)" file from the cache.
	 */
	private boolean clearCacheListFile() {
		File f = new File(mpqCachePath + File.separator + "(listfile)"); //$NON-NLS-1$
		if (f.exists() && !f.isDirectory()) {
			return f.delete();
		}
		return true;
	}

	/**
	 * Removes the "(attributes)" file from the cache.
	 * 
	 * @return
	 */
	private boolean clearCacheAttributesFile() {
		File f = new File(mpqCachePath + File.separator + "(attributes)"); //$NON-NLS-1$
		if (f.exists() && !f.isDirectory()) {
			return f.delete();
		}
		return true;
	}

	/**
	 * Removes all files in the cache.
	 */
	public boolean clearCacheExtractedMpq() {
		File f = new File(mpqCachePath);
		if (f.exists()) {
			if (deleteDir(f)) {
				LOGGER.debug("clearing Cache succeeded"); //$NON-NLS-1$
				return true;
			} else {
				LOGGER.error("clearing Cache FAILED"); //$NON-NLS-1$
				return false;
			}
		} else
			return true;
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public static boolean deleteDir(File f) {
		if (f.isDirectory()) {
			File[] content = f.listFiles();
			if (content != null) {
				for (int i = 0; i < content.length; i++) {
					if (!deleteDir(content[i])) {
						return false;
					}
				}
			}
		}
		boolean result = f.delete();
		if (!result) {
			LOGGER.error("Deleting file/folder " + f.getPath() + " failed."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}

	/**
	 * 
	 * @param mpqPath
	 * @param maxFileCount
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void newMpq(String mpqPath, int maxFileCount) throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /n " + q + mpqPath + q + " " + maxFileCount + q; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$
	}

	/**
	 * Verifies the existence of the MPQEditor.exe at the stored file path.
	 * 
	 * @return
	 */
	private boolean verifyMpqEditor() {
		File f = new File(MPQ_EDITOR);
		return f.exists() && f.isFile();
	}

	/**
	 * 
	 * @param mpqPath
	 * @param sourceFilePath
	 * @param targetName
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void addToMpq(String mpqPath, String sourceFilePath, String targetName)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /a " + q + mpqPath + q + " " + q + sourceFilePath + q + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ q + targetName + q + " /r" + q; //$NON-NLS-1$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param mpqPath
	 * @param fileName
	 * @param targetPath
	 * @param inclSubFolders
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void extractFromMpq(String mpqPath, String fileName, String targetPath, boolean inclSubFolders)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /e " + q + mpqPath + q + " " + q + fileName + q + " " + q //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ targetPath + q + (inclSubFolders ? " /fp" : "") + q; //$NON-NLS-1$ //$NON-NLS-2$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$

		// // MONITOR https://github.com/inwc3/JMPQ3 if it can handle sc2 files
		// someday to potentially replace MpqEditor.exe
		// File mpq = new File(mpqPath);
		// LOGGER.trace("Extract from Mpq: "+mpq.getAbsolutePath());
		// JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.FORCE_V0);
		// File cache = new File(mpqCachePath);
		// cache.mkdirs();
		// LOGGER.trace("Extract into: "+cache.getAbsolutePath());
		// editor.extractAllFiles(cache);
	}

	/**
	 * 
	 * @param mpqPath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void compactMpq(String mpqPath) throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /compact " + q + mpqPath + q + q; //$NON-NLS-1$ //$NON-NLS-2$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$

	}

	/**
	 * 
	 * @param mpqPath
	 * @param scriptPath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void scriptMpq(String mpqPath, String scriptPath) throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /s " + q + mpqPath + q + " " + q + scriptPath + q + q; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param mpqPath
	 * @param filePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void deleteFileInMpq(String mpqPath, String filePath)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /d " + q + mpqPath + q + " " + q + filePath + q + q; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param mpqPath
	 * @param oldfilePath
	 * @param newFilePath
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MpqException
	 */
	public void renameFileInMpq(String mpqPath, String oldfilePath, String newFilePath)
			throws InterruptedException, IOException, MpqException {
		if (!verifyMpqEditor()) {
			throw new MpqException(String.format(Messages.getString("MpqInterface.MPQEditorNotFound"), MPQ_EDITOR)); //$NON-NLS-1$
		}
		String q = "\""; //$NON-NLS-1$
		String cmd = "cmd /C " + q + q + MPQ_EDITOR + q + " /r " + q + mpqPath + q + " " + q + oldfilePath + q + " " + q //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ newFilePath + q + q;
		LOGGER.debug("executing: " + cmd); //$NON-NLS-1$
		Runtime.getRuntime().exec(cmd).waitFor();
		LOGGER.debug("execution finished"); //$NON-NLS-1$
	}

	/**
	 * Returns a file from the cache with the specified internal path.
	 * 
	 * @param descIndexIntPath
	 * @return
	 */
	public File getCachedFile(String intPath) {
		return new File(mpqCachePath + "//" + intPath); //$NON-NLS-1$
	}

	/**
	 * Returns the componentList file.
	 * 
	 * @return
	 */
	public File getComponentListFile() {
		File f = new File(mpqCachePath + File.separator + "ComponentList.StormComponents"); //$NON-NLS-1$
		if (!f.exists() || f.isDirectory()) {
			f = new File(mpqCachePath + File.separator + "ComponentList.SC2Components"); //$NON-NLS-1$
			if (!f.exists() || f.isDirectory()) {
				return null;
			}
		}
		return f;
	}

	/**
	 * 
	 * @return
	 * @throws MpqException
	 */
	public boolean isHeroesNamespace() throws MpqException {
		File f = new File(mpqCachePath + File.separator + "ComponentList.StormComponents"); //$NON-NLS-1$
		if (!f.exists() || f.isDirectory()) {
			LOGGER.debug("file not found in archive: " + f.getAbsolutePath());
			f = new File(mpqCachePath + File.separator + "ComponentList.SC2Components"); //$NON-NLS-1$
			if (!f.exists() || f.isDirectory()) {
				LOGGER.error("ERROR: archive has no ComponentList file."); //$NON-NLS-1$
				throw new MpqException("ERROR: Cannot identify if file belongs to Heroes or SC2."); //$NON-NLS-1$
			}
			return false;
		}
		return true;
	}

	// public void deleteListFile(String absolutePath) throws
	// InterruptedException, IOException {
	// // MPQ Editor does not allow tinkering with listfile
	//
	// // Runtime.getRuntime()
	// // .exec("cmd /C " + MPQ_EDITOR_OLD + " /d " + "\"" + absolutePath +
	// // "\"" + " " + "\"" + "(listfile)" + "\"")
	// // .waitFor();
	//
	// // String sourceFilePath = "plugins" + File.separator + "mpq" +
	// // File.separator + "listfile";
	// // addToMpq(MPQ_EDITOR, sourceFilePath, absolutePath);
	//
	// // Runtime.getRuntime()
	// // .exec("cmd /C " + MPQ_EDITOR + " /rename " + "\"" + absolutePath +
	// // "\"" + " " + "\"" + "(listfile)" + "\"" + " " + "\"" + "(listfile2)"
	// // + "\"")
	// // .waitFor();
	//
	// }

	// public void writeWrongFileAmount(String readAbsolutePath, String
	// writeAbsolutePath) {
	// // DOES NOT WORK, GAME DOES NOT READ MPQ ANYMORE
	// FileInputStream fis = null;
	// OutputStream out = null;
	//
	// try {
	// fis = new FileInputStream(new File(readAbsolutePath));
	// out = new FileOutputStream(new File(writeAbsolutePath));
	//
	// //// print hex
	// // char[] line = new char[16];
	// // for (int i=0; i < 16; i++) {
	// // int readByte = fis.read();
	// // String paddingZero = (readByte < 16) ? "0" : "";
	// // System.out.print(paddingZero + Integer.toHexString(readByte)
	// // + " ");
	// // line[i] = (readByte >= 33 && readByte <= 126) ? (char)
	// // readByte : '.';
	// // }
	// // System.out.println(new String(line));
	//
	// // skip parts of header
	// for (int i = 0; i < 25; i++) {
	// int readByte = fis.read();
	// out.write(readByte);
	// }
	// // read file count bytes
	// int[] fileCountByte = new int[4];
	// for (int i = 0; i < 4; i++) {
	// fileCountByte[i] = fis.read();
	// }
	// // increment bytes
	// for (int i = 3; i > 0; i--) {
	// if (fileCountByte[i] == 255) {
	// fileCountByte[i] = 0;
	// fileCountByte[i - 1]++;
	// } else {
	// fileCountByte[i]++;
	// }
	// }
	// // write filecount bytes
	// for (int i = 0; i < 4; i++) {
	// out.write(fileCountByte[i]);
	// }
	// // write rest of file
	// while (fis.available() > 0) {
	// out.write(fis.read());
	// }
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// } finally {
	// // close streams no matter what
	// if (fis != null)
	// try {
	// fis.close();
	// } catch (Exception e) {
	// }
	// if (out != null)
	// try {
	// out.close();
	// } catch (Exception e) {
	// }
	// }
	// }

	// public void batchFileExecutionExample() throws IOException,
	// InterruptedException{
	// String filepath = "Temp\\Script.bat";
	//
	// Process p = Runtime.getRuntime().exec("cmd /C "+filepath);
	//
	// InputStream is = p.getInputStream();
	//
	// // get results
	// int i = 0;
	// StringBuffer sb = new StringBuffer();
	// while ( (i = is.read()) != -1){
	// sb.append((char)i);
	// }
	//
	// System.out.println(sb.toString());
	//
	// // wait for the script to finish (optional?)
	// int exitVal = p.waitFor();
	// }

}
