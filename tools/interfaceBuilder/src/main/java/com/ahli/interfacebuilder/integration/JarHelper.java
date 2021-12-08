// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helper class for the executable jar file. It is capable of determining the position.
 *
 * @author Ahli
 */
public final class JarHelper {
	private static final Logger logger = LogManager.getLogger(JarHelper.class);
	
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
	public static Path getJarDir(final Class<?> aclass) {
		logger.trace("_FINDING JAR'S PATH");
		
		// ATTEMPT #1
		final File dir = new ApplicationHome(aclass).getDir();
		String str = dir.toString();
		logger.trace("Attempt#1 java.class.path: {}", dir::toString);
		
		// check if started in eclipse
		final int i = str.indexOf(File.separator + "target" + File.separator + "classes");
		if (i > 0) {
			final String check = str.substring(0, i);
			logger.trace("target/classes location: {}", check);
			if (check.indexOf(';') < 0) {
				final Path p = Path.of(check).getParent().getParent();
				if (Files.exists(p)) {
					return p.resolve("dev");
				}
			}
			
			// get current working directory
			final URI uri = new File(".").toURI();
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			
			str = uri.getPath();
			logger.trace("_URI path: {}", uri::getPath);
			
			// fix for intellij
			if (str.endsWith("/tools/./")) {
				str = str.substring(0, str.length() - 2);
				final String dirStr = dir.toString();
				final String tools = "\\tools\\";
				final int targetIndex = dirStr.indexOf("\\target\\");
				if (targetIndex >= 0) {
					str += dirStr.substring(dirStr.indexOf(tools) + tools.length(), targetIndex);
					str = str.replace('\\', '/');
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
			
		} else {
			if (str.contains(".jar;")) {
				str = str.substring(0, str.indexOf(".jar"));
				logger.trace("path before .jar: {}", str);
				final int lastFileSepIndex = str.lastIndexOf(File.separator);
				if (lastFileSepIndex >= 0) {
					str = str.substring(0, lastFileSepIndex);
				}
			}
			
		}
		logger.trace("_RESULT PATH: {}", str);
		
		return Path.of(str);
	}
	
}
