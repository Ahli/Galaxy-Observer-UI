package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import application.protection.XmlCompressor;

/**
 * 
 * @author Ahli
 *
 */
public class MpqInterface {

	private String MPQ_EDITOR = "plugins" + File.separator + "mpq" + File.separator + "MPQEditor.exe";
	// private String MPQ_EDITOR_OLD = "plugins" + File.separator + "mpq" +
	// File.separator + "MPQEditorOLD.exe";
	private final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private String mpqCachePath = TEMP_DIR + "InterfaceBuilder" + File.separator + "_ExtractedMpq";

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
	 * Build MPQ from cache.
	 * 
	 * @param buildPath
	 * @param buildFileName
	 * @param protectMPQ
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void buildMpq(String buildPath, String buildFileName, boolean protectMPQ)
			throws IOException, InterruptedException {
		String absolutePath = buildPath + File.separator + buildFileName;
		buildMpq(absolutePath, protectMPQ);
	}

	/**
	 * Build MPQ from cache.
	 * 
	 * @param absolutePath
	 * @param protectMPQ
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void buildMpq(String absolutePath, boolean protectMPQ) throws IOException, InterruptedException {
		// add 2 to be sure to have enough space for listfile and attributes
		int fileCount = 2 + getFileCountInFolder(new File(mpqCachePath));

		if (protectMPQ) {
			// special unprotected file path
			String unprotectedAbsolutePath = getPathWithSuffix(absolutePath, "_unprtctd");
			
			// make way for unprotected file
			File fup = new File(unprotectedAbsolutePath);
			if (fup.exists() && fup.isFile()) {
				if(!fup.delete()){
					throw new IOException("ERROR: Could not delete file "+unprotectedAbsolutePath);
				};
			}
			
			// build unprotected file
			newMpq(unprotectedAbsolutePath, fileCount);
			addToMpq(unprotectedAbsolutePath, mpqCachePath, "");
			compactMpq(unprotectedAbsolutePath);
			
			// make way for protected file
			File f = new File(absolutePath);
			if (f.exists() && f.isFile()) {
				if(!f.delete()){
					throw new IOException("ERROR: Could not delete file "+absolutePath);
				};
			}
			
			////////////////////////
			// PROTECTION MEASURES
			////////////////////////
				// doesn't work
					//deleteListFile(absolutePathTMP);
				// doesn't work
					//renameFileInMpq(absolutePathTMP, "_Ahli.StormLayout", "_QQQ.StormLayout");
					//renameFileInMpq(absolutePathTMP, "(listfile)", "QQQ");
				// doesn't work
					//writeWrongFileAmount(absolutePathTMP, absolutePath);
			
			// extra compression
			try {
				XmlCompressor.processCache(mpqCachePath, 1);
				
				
				
				
			} catch (ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}
			
			// build protected file
			newMpq(absolutePath, fileCount);
			addToMpq(absolutePath, mpqCachePath, "");
			compactMpq(absolutePath);

		} 
		else {
			// NO PROTECTION OPTION
			
			// make way for file
			File f = new File(absolutePath);
			if (f.exists() && f.isFile()) {
				if(!f.delete()){
					throw new IOException("ERROR: Could not delete file "+absolutePath);
				};
			}
						
			// build unprotected file
			newMpq(absolutePath, fileCount);
			addToMpq(absolutePath, mpqCachePath, "");
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
	 */
	public void extractEntireMPQ(String mpqSourcePath) throws InterruptedException, IOException {
		System.out.println(mpqCachePath);
		System.out.println(mpqSourcePath);

		clearCacheExtractedMpq();

		extractFromMpq(mpqSourcePath, "*", mpqCachePath, true);

		clearCacheListFile();
		clearCacheAttributesFile();
	}

	/**
	 * Removes the "(listfile)" file from the cache.
	 */
	private boolean clearCacheListFile() {
		File f = new File(mpqCachePath + File.separator + "(listfile)");
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
		File f = new File(mpqCachePath + File.separator + "(attributes)");
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
				System.out.println("clearing Cache succeeded");
				return true;
			} else {
				System.out.println("ERROR: clearing Cache FAILED");
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
			for (int i = 0; i < content.length; i++) {
				if (!deleteDir(content[i])) {
					return false;
				}
			}
		}
		boolean result = f.delete();
		if(!result){
			System.out.println("ERROR: Deleting file/folder "+f.getPath()+" failed.");
		}
		return result;
	}

	/**
	 * 
	 * @param mpqPath
	 * @param maxFileCount
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void newMpq(String mpqPath, int maxFileCount) throws InterruptedException, IOException {
		Runtime.getRuntime().exec("cmd /C " + MPQ_EDITOR + " /n " + "\"" + mpqPath + "\"" + " " + maxFileCount)
				.waitFor();
		System.out.println("cmd /C " + MPQ_EDITOR + " /n " + "\"" + mpqPath + "\"" + " " + maxFileCount);
	}

	/**
	 * 
	 * @param mpqPath
	 * @param sourceFilePath
	 * @param targetName
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void addToMpq(String mpqPath, String sourceFilePath, String targetName)
			throws InterruptedException, IOException {
		Runtime.getRuntime().exec("cmd /C " + MPQ_EDITOR + " /a " + "\"" + mpqPath + "\"" + " " + "\"" + sourceFilePath
				+ "\"" + " " + "\"" + targetName + "\"" + " /r").waitFor();
		System.out.println("cmd /C " + MPQ_EDITOR + " /a " + "\"" + mpqPath + "\"" + " " + "\"" + sourceFilePath + "\""
				+ " " + "\"" + targetName + "\"" + "/auto /r");
	}

	/**
	 * 
	 * @param mpqPath
	 * @param fileName
	 * @param targetPath
	 * @param inclSubFolders
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void extractFromMpq(String mpqPath, String fileName, String targetPath, boolean inclSubFolders)
			throws InterruptedException, IOException {
		Runtime.getRuntime().exec("cmd /C " + MPQ_EDITOR + " /e " + "\"" + mpqPath + "\"" + " " + "\"" + fileName + "\""
				+ " " + "\"" + targetPath + "\"" + " " + (inclSubFolders ? "/fp" : "")).waitFor();
	}

	/**
	 * 
	 * @param mpqPath
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void compactMpq(String mpqPath) throws InterruptedException, IOException {
		Runtime.getRuntime().exec("cmd /C " + MPQ_EDITOR + " /compact " + "\"" + mpqPath + "\"").waitFor();
	}

	/**
	 * 
	 * @param mpqPath
	 * @param scriptPath
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void scriptMpq(String mpqPath, String scriptPath) throws InterruptedException, IOException {
		Runtime.getRuntime()
				.exec("cmd /C " + MPQ_EDITOR + " /s " + "\"" + mpqPath + "\"" + " " + "\"" + scriptPath + "\"")
				.waitFor();
	}

	/**
	 * 
	 * @param mpqPath
	 * @param filePath
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void deleteFileInMpq(String mpqPath, String filePath) throws InterruptedException, IOException {
		Runtime.getRuntime()
				.exec("cmd /C " + MPQ_EDITOR + " /d " + "\"" + mpqPath + "\"" + " " + "\"" + filePath + "\"").waitFor();
	}
	
	/**
	 * 
	 * @param mpqPath
	 * @param oldfilePath
	 * @param newFilePath
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void renameFileInMpq(String mpqPath, String oldfilePath, String newFilePath) throws InterruptedException, IOException {
		Runtime.getRuntime()
				.exec("cmd /C " + MPQ_EDITOR + " /r " + "\"" + mpqPath + "\"" + " " + "\"" + oldfilePath + "\"" + " " + "\"" + newFilePath + "\"").waitFor();
	}

	/**
	 * Returns a file from the cache with the specified internal path.
	 * 
	 * @param descIndexIntPath
	 * @return
	 */
	public File getCachedFile(String intPath) {
		return new File(mpqCachePath + "//" + intPath);
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

	public boolean isHeroesNamespace() throws MpqException {
		File f = new File(mpqCachePath + File.separator + "ComponentList.StormComponents");
		if (!f.exists() || f.isDirectory()) {
			f = new File(mpqCachePath + File.separator + "ComponentList.SC2Components");
			if (!f.exists() || f.isDirectory()) {
				return false;
			}
			System.out.println("failed path: " + f.getAbsolutePath());
			// throw new MpqException("ERROR: cannot identify if file belongs to
			// Heroes or SC2");
			return false;
		}
		return true;
	}

	public void deleteListFile(String absolutePath) throws InterruptedException, IOException {
		// MPQ Editor does not allow tinkering with listfile

		// Runtime.getRuntime()
		// .exec("cmd /C " + MPQ_EDITOR_OLD + " /d " + "\"" + absolutePath +
		// "\"" + " " + "\"" + "(listfile)" + "\"")
		// .waitFor();

		// String sourceFilePath = "plugins" + File.separator + "mpq" +
		// File.separator + "listfile";
		// addToMpq(MPQ_EDITOR, sourceFilePath, absolutePath);
		
//		 Runtime.getRuntime()
//		 .exec("cmd /C " + MPQ_EDITOR + " /rename " + "\"" + absolutePath +
//		 "\"" + " " + "\"" + "(listfile)" + "\"" + " " + "\"" + "(listfile2)" + "\"")
//		 .waitFor();

	}

	public void writeWrongFileAmount(String readAbsolutePath, String writeAbsolutePath) {
		// DOES NOT WORK, GAME DOES NOT READ MPQ ANYMORE
		FileInputStream fis = null;
		OutputStream out = null;

		try {
			fis = new FileInputStream(new File(readAbsolutePath));
			out = new FileOutputStream(new File(writeAbsolutePath));

			//// print hex
			// char[] line = new char[16];
			// for (int i=0; i < 16; i++) {
			// int readByte = fis.read();
			// String paddingZero = (readByte < 16) ? "0" : "";
			// System.out.print(paddingZero + Integer.toHexString(readByte)
			// + " ");
			// line[i] = (readByte >= 33 && readByte <= 126) ? (char)
			// readByte : '.';
			// }
			// System.out.println(new String(line));

			// skip parts of header
			for (int i = 0; i < 25; i++) {
				int readByte = fis.read();
				out.write(readByte);
			}
			// read file count bytes
			int[] fileCountByte = new int[4];
			for (int i = 0; i < 4; i++) {
				fileCountByte[i] = fis.read();
			}
			// increment bytes
			for (int i = 3; i > 0; i--) {
				if (fileCountByte[i] == 255) {
					fileCountByte[i] = 0;
					fileCountByte[i - 1]++;
				} else {
					fileCountByte[i]++;
				}
			}
			// write filecount bytes
			for (int i = 0; i < 4; i++) {
				out.write(fileCountByte[i]);
			}
			// write rest of file
			while (fis.available() > 0) {
				out.write(fis.read());
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			// close streams no matter what
			if (fis != null)
				try {
					fis.close();
				} catch (Exception e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (Exception e) {
				}
		}
	}

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
