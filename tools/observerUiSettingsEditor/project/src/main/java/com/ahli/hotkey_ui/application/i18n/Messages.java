// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.i18n;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
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
	private static final String BUNDLE_NAME = "i18n.messages";
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private static Locale usedLocale = Locale.getDefault();
	
	private Messages() {
		// no instances allowed
	}
	
	/**
	 * Returns the String of the key.
	 *
	 * @param key
	 * @return
	 */
	public static String getString(final String key) {
		try {
			return resourceBundle.getString(key);
		} catch (final MissingResourceException e) {
			log.error(String.format("ERROR: failed to receive String for %s", key), e);
			return '!' + key + '!';
		}
	}
	
	/**
	 * Returns the currently used bundle.
	 *
	 * @return
	 */
	public static ResourceBundle getBundle() {
		return resourceBundle;
	}
	
	/**
	 * Sets a bundle based on the specified Locale.
	 *
	 * @param loc
	 */
	public static void setBundle(final Locale loc) {
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, loc);
		usedLocale = loc;
	}
	
	/**
	 * Checks if the Locale's resource bundle is the one used.
	 *
	 * @param locale
	 * @return
	 */
	public static boolean checkIfTargetResourceIsUsed(final Locale locale) {
		final boolean result = resourceBundle.equals(ResourceBundle.getBundle(BUNDLE_NAME, locale));
		log.trace("compare used locale's resource '{}' with one for locale '{}', result: {}",
				usedLocale,
				locale,
				result);
		return result;
	}
}
