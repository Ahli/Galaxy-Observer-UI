package com.ahli.mpq.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to internationalize messages. Hint: FXML files can be localized via
 * text="%key", if the FxmlLoader receives the correct resource bundle.
 * 
 * @author Ahli
 */
public final class Messages {
	private static Logger logger = LogManager.getLogger();
	
	private static final String BUNDLE_NAME = "com.ahli.mpq.i18n.messages"; //$NON-NLS-1$
	
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private static Locale usedLocale = Locale.getDefault();
	
	/**
	 * The Default Constructor.
	 */
	private Messages() {
	}
	
	/**
	 * Returns the String of the key.
	 * 
	 * @param key
	 *            the key
	 * @return string of the key
	 */
	public static String getString(final String key) {
		try {
			return resourceBundle.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Sets a bundle based on the specified Locale.
	 * 
	 * @param loc
	 *            the Locale
	 */
	public static void setBundle(final Locale loc) {
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, loc);
		usedLocale = loc;
	}
	
	/**
	 * Returns the currently used bundle.
	 * 
	 * @return the ResourceBundle
	 */
	public static ResourceBundle getBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the Locale that was used to get the bundle in effect.
	 * 
	 * @return used Locale
	 */
	public static Locale getUsedLocale() {
		return usedLocale;
	}
	
	/**
	 * Checks if the Locale's resource bundle is the one used.
	 * 
	 * @param locale
	 *            a Locale
	 * @return whether the specified Locale is used or not
	 */
	public static boolean checkIfTargetResourceIsUsed(final Locale locale) {
		final boolean result = resourceBundle.equals(ResourceBundle.getBundle(BUNDLE_NAME, locale));
		logger.trace("compare used locale's resource '" + usedLocale + "' with one for locale '" + locale
				+ "', result: " + result);
		return result;
	}
}
