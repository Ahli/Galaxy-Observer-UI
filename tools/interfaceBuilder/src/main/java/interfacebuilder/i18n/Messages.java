// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.i18n;

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
	private static final String BUNDLE_NAME = "i18n.messages";
	private static final Logger logger = LogManager.getLogger(Messages.class);
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
	 * 		the key
	 * @return string of the key
	 */
	public static String getString(final String key) {
		try {
			return resourceBundle.getString(key);
		} catch (final MissingResourceException e) {
			logger.error("ERROR: failed to receive String for " + key, e);
			return '!' + key + '!';
		}
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
	 * Sets a bundle based on the specified Locale.
	 *
	 * @param loc
	 * 		the Locale
	 */
	public static void setBundle(final Locale loc) {
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, loc);
		usedLocale = loc;
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
	 * 		a Locale
	 * @return whether the specified Locale is used or not
	 */
	public static boolean checkIfTargetResourceIsUsed(final Locale locale) {
		final boolean result = resourceBundle.equals(ResourceBundle.getBundle(BUNDLE_NAME, locale));
		logger.trace("compare used locale's resource '{}' with one for locale '{}', result: {}", () -> usedLocale,
				() -> locale, () -> result);
		return result;
	}
}
