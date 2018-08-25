package com.ahli.hotkeyUi.application.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		logger.trace("_FINDING JAR'S PATH");
		
		// ATTEMPT #1
		final File f = new File(System.getProperty("java.class.path"));
		final File dir = f.getAbsoluteFile().getParentFile();
		String str = dir.toString();
		logger.trace("Attempt#1 java.class.path: {}", () -> dir.toString());
		
		// check if started in eclipse
		final String targetClasses = File.separator + "target" + File.separator + "classes;";
		int i = str.indexOf(targetClasses);
		if (i > 0) {
			final String check = str.substring(0, i);
			logger.trace("target/classes location: {}", () -> check);
			if (check.indexOf(';') < 0) {
				Path p = Paths.get(check).getParent().getParent();
				if (Files.exists(p)) {
					return p.toFile();
				}
			}
			
			// get current working directory
			final URI uri = new File(".").toURI();
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			
			str = uri.getPath();
			logger.trace("_URI path: {}", () -> uri.getPath());
			
			// fix for intellij
			if (str.endsWith("/tools/./")) {
				str = str.substring(0, str.length() - 2);
				final String dirStr = dir.toString();
				final String tools = "\\tools\\";
				str += dirStr.substring(dirStr.indexOf(tools) + tools.length(), dirStr.indexOf("\\target\\"));
				str = str.replace('\\', '/');
				if (logger.isTraceEnabled()) {
					logger.trace("after intellij #1: " + str);
				}
			}
			
			if (str.startsWith("file:/")) {
				str = str.substring(6);
			}
			if (str.charAt(0) == '/') {
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
				if (logger.isTraceEnabled()) {
					logger.trace("assuming Test Environment: " + str);
				}
			}
			
		} else {
			if (str.contains(".jar;")) {
				str = str.substring(0, str.indexOf(".jar"));
				if (logger.isTraceEnabled()) {
					logger.trace("path before .jar: " + str);
				}
				str = str.substring(0, str.lastIndexOf(File.separator));
			} else {
				// test if intellij
				if (f.getAbsolutePath().contains("\\lib\\idea_rt.jar")) {
					str = Paths.get("").toAbsolutePath().toString();
					if (logger.isTraceEnabled()) {
						logger.trace("current working directory: " + str);
					}
					str = System.getProperty("user.dir");
					if (logger.isTraceEnabled()) {
						logger.trace("current user.dir: " + str);
					}
				}
				// cant find a dynamic way
				str = "D:\\Galaxy-Observer-UI\\tools";
				
			}
			
		}
		if (logger.isTraceEnabled()) {
			logger.trace("_RESULT PATH: " + str);
		}
		
		return new File(str);
	}
	
}
