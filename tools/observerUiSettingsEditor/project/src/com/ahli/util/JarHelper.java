package com.ahli.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.Main;

public class JarHelper {
	static Logger LOGGER = LogManager.getLogger("JarHelper"); //$NON-NLS-1$

	/**
	 * from stackoverflow because why doesn't java have this functionality? It's
	 * not like nobody would need that or it is trivial to create...
	 * 
	 * @param aclass
	 * @return File at base path
	 */
	public static File getJarDir(Class<Main> aclass) {
		LOGGER.debug("_FINDING JAR'S PATH"); //$NON-NLS-1$

		// ATTEMPT #1
		File f = new File(System.getProperty("java.class.path")); //$NON-NLS-1$
		File dir = f.getAbsoluteFile().getParentFile();
		String str = dir.toString();
		LOGGER.debug("Attempt#1 java.class.path: " + str); //$NON-NLS-1$

		// check if started in eclipse
		if (str.contains("tools" + File.separator + "observerUiSettingsEditor" + File.separator + "project" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ File.separator + "target" + File.separator + "classes;")) { //$NON-NLS-1$ //$NON-NLS-2$
			// get current working directory
			URI uri = new File(".").toURI(); //$NON-NLS-1$
			// results in: "file:/D:/GalaxyObsUI/dev/./"
			// but maybe results in something completely different like
			// notepad++'s directory...
			// addLogMessage("METHOD TEST: " + new File(".").toURI());

			str = uri.getPath();
			LOGGER.debug("_URI path:" + str); //$NON-NLS-1$

			if (str.startsWith("file:/")) { //$NON-NLS-1$
				str = str.substring(6);
			}
			if (str.startsWith("/")) { //$NON-NLS-1$
				str = str.substring(1);
			}
			if (str.endsWith("/./")) { //$NON-NLS-1$
				str = str.substring(0, str.length() - 3);
			}

			URL url = aclass.getProtectionDomain().getCodeSource().getLocation();
			// class returns "rsrc:./", if 2nd option during jar export was
			// chosen
			if (!url.toString().startsWith("rsrc:./")) { //$NON-NLS-1$
				// wild guess that we are in test environment
				str += "/testEnv/dev/"; //$NON-NLS-1$
				LOGGER.debug("assuming Test Environment: " + str); //$NON-NLS-1$
			}

		} else {
			if(str.contains(".jar;")) {
				str = str.substring(0, str.indexOf(".jar"));
				LOGGER.debug("path before .jar: "+str);
				str = str.substring(0, str.lastIndexOf(File.separator));
			}
			
			
			
		}
		LOGGER.debug("_RESULT PATH: " + str); //$NON-NLS-1$

		return new File(str);
	}

}
