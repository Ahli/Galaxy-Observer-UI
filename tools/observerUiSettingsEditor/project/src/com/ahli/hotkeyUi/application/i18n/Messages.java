package com.ahli.hotkeyUi.application.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to internationalize messages. Hint: FXML files can be localized via text="%key", if the FxmlLoader receives the
 * correct resource bundle.
 *
 * @author Ahli
 */
public final class Messages {
	private static final String BUNDLE_NAME = "com.ahli.hotkeyUi.application.i18n.messages";
	private static final Logger logger = LogManager.getLogger();
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private static Locale usedLocale = Locale.getDefault();
	
	/**
	 * Disabled Constructor.
	 */
	private Messages() {
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
	 * Returns the Locale that was used to get the bundle in effect.
	 *
	 * @return
	 */
	public static Locale getUsedLocale() {
		return usedLocale;
	}
	
	/**
	 * Checks if the Locale's resource bundle is the one used.
	 *
	 * @param locale
	 * @return
	 */
	public static boolean checkIfTargetResourceIsUsed(final Locale locale) {
		final boolean result = resourceBundle.equals(ResourceBundle.getBundle(BUNDLE_NAME, locale));
		if (logger.isTraceEnabled()) {
			logger.trace("compare used locale's resource '" + usedLocale + "' with one for locale '" + locale +
					"', result: " + result);
		}
		return result;
	}
}
