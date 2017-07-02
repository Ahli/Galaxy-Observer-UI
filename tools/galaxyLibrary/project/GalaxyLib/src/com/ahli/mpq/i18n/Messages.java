package com.ahli.mpq.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to internationalize messages.
 * 
 * Hint: FXML files can be localized via text="%key", if the FxmlLoader receives
 * the correct resource bundle.
 * 
 * @author Ahli
 *
 */
public class Messages {
	static Logger LOGGER = LogManager.getLogger("Messages"); //$NON-NLS-1$

	private static final String BUNDLE_NAME = "com.ahli.mpq.i18n.messages"; //$NON-NLS-1$

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

	private static Locale usedLocale = Locale.getDefault();

	private Messages() {
	}

	/**
	 * Returns the String of the key.
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Sets a bundle based on the specified Locale.
	 * 
	 * @param loc
	 */
	public static void setBundle(Locale loc) {
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, loc);
		usedLocale = loc;
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
	public static boolean checkIfTargetResourceIsUsed(Locale locale) {
		boolean result = resourceBundle.equals(ResourceBundle.getBundle(BUNDLE_NAME, locale));
		LOGGER.trace("compare used locale's resource '" + usedLocale + "' with one for locale '" + locale
				+ "', result: " + result);
		return result;
	}
}
