// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.i18n;

import lombok.extern.slf4j.Slf4j;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to internationalize messages. Hint: FXML files can be localized via text="%key", if the FxmlLoader receives the
 * correct resource bundle.
 *
 * @author Ahli
 */
@Slf4j
public final class Messages {
	private static final String BUNDLE_NAME = "galaxylib_i18n.galaxyLibMessages";
	private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages() {
		// no instances allowed
	}
	
	/**
	 * Returns the String of the key.
	 *
	 * @param key
	 * 		the key
	 * @return string of the key
	 */
	public static String getString(final String key) {
		try {
			return bundle.getString(key);
		} catch (final MissingResourceException e) {
			log.error(String.format("ERROR: failed to receive String for %s", key), e);
			return '!' + key + '!';
		}
	}
	
}
