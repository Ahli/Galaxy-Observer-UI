package com.ahli.mpq.mpqeditor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MpqEditorSettingsInterface {
	private static final Logger logger = LogManager.getLogger();
	
	private final File iniFile;
	private final File rulesetFile;
	private boolean backupActive = false;
	
	private int saveMode = 0;
	
	public MpqEditorSettingsInterface() {
		iniFile = new File(System.getenv("APPDATA") + File.separator + "MPQEditor.ini");
		rulesetFile = new File(System.getenv("APPDATA") + File.separator + "MPQEditor_Ruleset.ini");
		logger.info("MpqEditor ini file: " + iniFile.getAbsolutePath());
	}
	
	public void useCustomRuleset() {
		try {
			final File iniFile = backupActive ? new File(this.iniFile.getParent() + File.separator + "MPQEditor.ini")
					: this.iniFile;
			
			final Parameters params = new Parameters();
			final FileBasedConfigurationBuilder<INIConfiguration> b = new FileBasedConfigurationBuilder<>(
					INIConfiguration.class).configure(params.ini().setFile(iniFile).setEncoding("UTF-8"));
			final INIConfiguration ini = b.getConfiguration();
			
			final SubnodeConfiguration options = ini.getSection("Options");
			final String gameId = options.getString("GameId");
			logger.info("GameId value of MpqEditor: " + gameId);
			options.setProperty("GameId", "13");
			b.save();
			
		} catch (final ConfigurationException e) {
			logger.error("Error while applying custom ruleset.", e);
		}
	}
	
	public void saveOriginalSettingFiles() throws IOException {
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
			rulesetFile.renameTo(backupFile);
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
			iniFile.renameTo(backupFile);
		}
		
		backupActive = true;
	}
	
	public void restoreOriginalSettingFiles() {
		if (!backupActive) {
			return;
		}
		final String directoryPath = iniFile.getParent();
		File originalName;
		
		// ruleset file
		if (rulesetFile.exists()) {
			originalName = new File(directoryPath + File.separator + "MPQEditor_Ruleset.ini");
			rulesetFile.renameTo(originalName);
		}
		
		// ini file
		if (iniFile.exists()) {
			originalName = new File(directoryPath + File.separator + "MPQEditor.ini");
			iniFile.renameTo(originalName);
		}
		
		backupActive = false;
	}
	
	public boolean isBackupActive() {
		return backupActive;
	}
	
	public void applySettings(final int saveMode) {
		this.saveMode = saveMode;
	}
}
