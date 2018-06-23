package com.ahli.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Helper class for the executable jar file. It is capable of determining the position.
 *
 * @author Ahli
 */
public final class JarHelper {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Disabled Constructor.
	 */
	private JarHelper() {
	}
	
	/**
	 * from stackoverflow because why doesn't java have this functionality? It's not like nobody would need that or it
	 * is trivial to create...
	 *
	 * @param aclass
	 * @return File at base path
	 */
	public static File getJarDir(final Class<?> aclass) {
		logger.debug("_FINDING JAR'S PATH");
		
		// ATTEMPT #1
		final File f = new File(System.getProperty("java.class.path"));
		final File dir = f.getAbsoluteFile().getParentFile();
		String str = dir.toString();
		logger.debug("Attempt#1 java.class.path: " + str);
		
		// check if started in eclipse
		if (str.contains(File.separator + "target" + File.separator + "classes;")) {
			// get current working directory
			final URI uri = new File(".").toURI();
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			
			str = uri.getPath();
			logger.debug("_URI path:" + str);
			
			// fix for intellij
			if (str.endsWith("/tools/./")) {
				str = str.substring(0, str.length() - 2);
				final String dirStr = dir.toString();
				final String tools = "\\tools\\";
				str += dirStr.substring(dirStr.indexOf(tools) + tools.length(), dirStr.indexOf("\\target\\"));
				str = str.replace('\\', '/');
			}
			
			if (str.startsWith("file:/")) {
				str = str.substring(6);
			}
			if (str.startsWith("/")) {
				str = str.substring(1);
			}
			if (str.endsWith("/./")) {
				str = str.substring(0, str.length() - 3);
			}
			
			final URL url = aclass.getProtectionDomain().getCodeSource().getLocation();
			// class returns "rsrc:./", if 2nd option during jar export was
			// chosen
			if (!url.toString().startsWith("rsrc:./")) {
				// wild guess that we are in test environment
				str += "/testEnv/dev/";
				logger.debug("assuming Test Environment: " + str);
			}
			
		} else {
			if (str.contains(".jar;")) {
				str = str.substring(0, str.indexOf(".jar"));
				logger.debug("path before .jar: " + str);
				str = str.substring(0, str.lastIndexOf(File.separator));
			}
			
		}
		logger.debug("_RESULT PATH: " + str);
		
		return new File(str);
	}
	
}
