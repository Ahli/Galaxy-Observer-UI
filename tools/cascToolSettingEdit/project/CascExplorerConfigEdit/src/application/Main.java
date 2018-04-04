package application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Application that edits the config file of the CascExplorerConsole.exe.
 *
 * @author Ahli
 */
public class Main {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		logger.info("Command Line Parameters: [ConfigFilePath] [StoragePath] [OnlineMode] [Product] [Locale]");
		logger.info("Parameters provided:");
		logger.info("ConfigFilePath: " + args[0]);
		logger.info("StoragePath: " + args[1]);
		logger.info("OnlineMode: " + args[2]);
		logger.info("Product: " + args[3]);
		logger.info("Locale: " + args[4]);
		
		final String path = args[0];
		final String storagePath = args[1];
		final String onlineMode = args[2];
		final String product = args[3];
		final String locale = args[4];
		final File f = new File(path);
		CascExplorerConfigFileEditor.write(f, storagePath, onlineMode, product, locale);
	}
	
}
