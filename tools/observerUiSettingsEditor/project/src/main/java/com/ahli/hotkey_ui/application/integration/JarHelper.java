// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.integration;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Helper class for the executable jar file. It is capable of determining the position.
 *
 * @author Ahli
 */
@Slf4j
public final class JarHelper {
	
	private JarHelper() {
		// no instances allowed
	}
	
	/**
	 * @param aclass
	 * @return File at base path
	 */
	public static Path getJarDir(final Class<?> aclass) {
		final String modulePath = System.getProperty("jdk.module.path");
		if (modulePath == null) {
			// case: jlink VM
			return Path.of(System.getProperty("user.dir"));
		}
		// case: IDE
		final ProtectionDomain domain = aclass.getProtectionDomain();
		final CodeSource codeSource = domain.getCodeSource();
		final URL location = codeSource.getLocation();
		final String path = location.getPath();
		
		// check if started in eclipse
		final int i = path.indexOf("/target/classes/");
		if (i > 0) {
			final String check = path.substring(0, i);
			log.trace("target/classes location: {}", check);
			if (check.charAt(0) == '/') {
				final Path p = Path.of(check.substring(1)).getParent().getParent();
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		// this failed
		if (log.isErrorEnabled()) {
			log.error(
					"""
					java.class.path={}
					jdk.module.path={}
					jdk.module.upgrade.path={}
					java.home={}
					user.dir={}
					class-domain-codesource-location-path={}""",
					System.getProperty("java.class.path"),
					System.getProperty("jdk.module.path"),
					System.getProperty("jdk.module.upgrade.path"),
					System.getProperty("java.home"),
					System.getProperty("user.dir"),
					path);
		}
		return null;
	}
}
