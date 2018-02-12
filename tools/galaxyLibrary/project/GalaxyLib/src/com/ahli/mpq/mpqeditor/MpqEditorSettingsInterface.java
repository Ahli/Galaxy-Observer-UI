package com.ahli.mpq.mpqeditor;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.INIBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Class to manage the settings of Ladik's MpqEditor.
 */
public class MpqEditorSettingsInterface {
	public static final String MPQEDITOR_RULESET_INI = "MPQEditor_Ruleset.ini";
	private static final String MPQEDITOR_INI = "MPQEditor.ini";
	private static final Logger logger = LogManager.getLogger();
	private static final String APPDATA = "APPDATA";
	private final File iniFile;
	private final File rulesetFile;
	private File iniFileBackUp = null;
	private File rulesetFileBackUp = null;
	private boolean backupActive = false;
	
	private MpqEditorCompression compression = MpqEditorCompression.BLIZZARD_SC2_HEROES;
	
	public MpqEditorSettingsInterface() {
		iniFile = new File(System.getenv(APPDATA) + File.separator + MPQEDITOR_INI);
		rulesetFile = new File(System.getenv(APPDATA) + File.separator + MPQEDITOR_RULESET_INI);
		logger.info("MpqEditor ini file: " + iniFile.getAbsolutePath());
	}
	
	/**
	 * Restored the original settings files that were backed up.
	 *
	 * @throws IOException
	 */
	public void restoreOriginalSettingFiles() throws IOException {
		if (!backupActive) {
			return;
		}
		
		restoreFileFromBackUp(rulesetFileBackUp, rulesetFile);
		rulesetFileBackUp = null;
		
		restoreFileFromBackUp(iniFileBackUp, iniFile);
		iniFileBackUp = null;
		
		backupActive = false;
	}
	
	/**
	 * Restores a file that was backed up.
	 *
	 * @param backUpFileName
	 * @param originalFileName
	 * @throws IOException
	 */
	private void restoreFileFromBackUp(final File backUpFileName, final File originalFileName) throws IOException {
		if (backUpFileName != null && backUpFileName.exists()) {
			if (originalFileName.exists()) {
				Files.delete(originalFileName.toPath());
			}
			if (!backUpFileName.renameTo(originalFileName)) {
				throw new IOException(
						"Could not restore original via renaming " + backUpFileName.getAbsolutePath() + " to " +
								originalFileName.getName());
			}
		}
	}
	
	/**
	 * Returns the state of file backups.
	 *
	 * @return
	 */
	public boolean isBackupActive() {
		return backupActive;
	}
	
	/**
	 * Returns the currently active compression method.
	 *
	 * @return
	 */
	public MpqEditorCompression getCompression() {
		return compression;
	}
	
	/**
	 * Sets the compression method.
	 *
	 * @param compression
	 */
	public void setCompression(final MpqEditorCompression compression) {
		this.compression = compression;
	}
	
	/**
	 * Applies the compression. Make sure to call <code>restoreOriginalSettingFiles()</code> afterwards to restore these
	 * files.
	 */
	public void applyCompression() throws IOException {
		switch (compression) {
			case CUSTOM:
				backUpOriginalSettingsFiles();
				break;
			
			case BLIZZARD_SC2_HEROES:
			case NONE:
				backUpOriginalSettingsFiles();
				useCustomRuleset();
				break;
			
			case SYSTEM_DEFAULT:
				break;
			
			default:
				throw new IOException("unknown compression setting");
		}
	}
	
	/**
	 * Backs up the original settings files
	 *
	 * @throws IOException
	 */
	private void backUpOriginalSettingsFiles() throws IOException {
		if (backupActive) {
			return;
		}
		
		final String directoryPath = iniFile.getParent();
		int i = 0;
		File backupFile;
		
		// ruleset file
		if (rulesetFile.exists()) {
			do {
				backupFile = new File(directoryPath + File.separator + "MPQEditor_Ruleset" + "_" + i + ".tmp");
				i++;
				if (i > 999) {
					throw new IOException("Could not find unique name for MPQEditor_Ruleset.ini's backup copy.");
				}
			} while (backupFile.exists());
			Files.copy(rulesetFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			rulesetFileBackUp = backupFile;
		}
		
		// ini file
		if (iniFile.exists()) {
			i = 0;
			do {
				backupFile = new File(directoryPath + File.separator + "MPQEditor" + "_" + i + ".tmp");
				i++;
				if (i > 999) {
					throw new IOException("Could not find unique name for MPQEditor.ini's backup copy.");
				}
			} while (backupFile.exists());
			Files.copy(iniFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			iniFileBackUp = backupFile;
		}
		
		backupActive = true;
	}
	
	/**
	 * Adjusts the custom ruleset.
	 */
	private void useCustomRuleset() throws IOException {
		if (!iniFile.exists()) {
			logger.error(
					"MpqEditor's ini file does not exist. It would be located at '" + iniFile.getAbsolutePath() + "'" +
							". The editor will run with its factory settings.");
			return;
		}
		int gameId = 6;
		try {
			final INIBuilderParameters params = new Parameters().ini().setFile(iniFile).setEncoding("UTF-8");
			final FileBasedConfigurationBuilder<INIConfiguration> b =
					new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
			final INIConfiguration ini = b.getConfiguration();
			
			final SubnodeConfiguration options = ini.getSection("Options");
			gameId = getGameIdPropertyValue(compression);
			options.setProperty("GameId", gameId);
			b.save();
		} catch (final ConfigurationException e) {
			logger.error("Error while applying custom ruleset usage entry.", e);
		}
		
		if (gameId == 13) {
			writeMpqRuleset();
		}
	}
	
	/**
	 * @param compression
	 * @return
	 */
	private int getGameIdPropertyValue(final MpqEditorCompression compression) {
		switch (compression) {
			case BLIZZARD_SC2_HEROES:
				return 11;
			case NONE:
			case CUSTOM:
			case SYSTEM_DEFAULT:
			default:
				return 13;
		}
	}
	
	/**
	 *
	 */
	private void writeMpqRuleset() throws IOException {
		INIConfiguration ini;
		try {
			final INIBuilderParameters params = new Parameters().ini().setFile(rulesetFile).setEncoding("UTF-8");
			final FileBasedConfigurationBuilder<INIConfiguration> b =
					new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
			ini = b.getConfiguration();
			ini.clear();
		} catch (final ConfigurationException e) {
			logger.error("Error while editing custom ruleset file.", e);
			ini = new INIConfiguration();
		}
		final SubnodeConfiguration section = ini.getSection("CustomRules");
		section.setProperty("MpqVersion", 3);
		section.setProperty("AttrFlags", 5);
		section.setProperty("SectorSize", 16384);
		section.setProperty("RawChunkSize", 16384);
		
		switch (compression) {
			case CUSTOM:
				// TODO implement custom ruleset
				break;
			case NONE:
				section.addProperty("Default", "0x02000000, 0x00000000, 0xFFFFFFFF");
				break;
			case SYSTEM_DEFAULT:
			case BLIZZARD_SC2_HEROES:
			default:
				break;
		}
		
		try (final FileWriter fw = new FileWriter(rulesetFile)) {
			ini.write(fw);
		} catch (final ConfigurationException | IOException e) {
			throw new IOException("Could not write '" + rulesetFile.getAbsolutePath() + "'.", e);
		}
	}
}
