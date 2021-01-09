// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import com.ahli.util.DeepCopyable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.INIBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to manage the settings of Ladik's MpqEditor.
 */
public class MpqEditorSettingsInterface implements DeepCopyable {
	private static final String MPQEDITOR_RULESET_INI = "MPQEditor_Ruleset.ini";
	private static final String CUSTOM_RULE_PROPERTY_KEY = "CustomRules. ";
	private static final String MPQEDITOR_INI = "MPQEditor.ini";
	private static final Logger logger = LogManager.getLogger(MpqEditorSettingsInterface.class);
	private static final String APPDATA = "APPDATA";
	private static final String NO_COMPRESSION_CUSTOM_RULE = "0x01000000, 0x00000002, 0xFFFFFFFF";
	private static final String DEFAULT = "Default";
	private static final String MPQ_VERSION = "MpqVersion";
	private static final String ATTR_FLAGS = "AttrFlags";
	private static final String SECTOR_SIZE = "SectorSize";
	private static final String RAW_CHUNK_SIZE = "RawChunkSize";
	private static final String CUSTOM_RULES = "CustomRules";
	private static final String SPACESPACEEQUALSSPACE = "  = ";
	private static final String GAME_ID = "GameId";
	private static final String OPTIONS = "Options";
	private static final String UTF_8 = "UTF-8";
	private final Path iniFile;
	private final Path rulesetFile;
	private Path iniFileBackUp;
	private Path rulesetFileBackUp;
	private boolean backupActive;
	private MpqEditorCompressionRule[] customRules;
	
	private MpqEditorCompression compression = MpqEditorCompression.BLIZZARD_SC2_HEROES;
	
	public MpqEditorSettingsInterface() {
		iniFile = Path.of(System.getenv(APPDATA) + File.separator + MPQEDITOR_INI);
		rulesetFile = Path.of(System.getenv(APPDATA) + File.separator + MPQEDITOR_RULESET_INI);
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
	private static void restoreFileFromBackUp(final Path backUpFileName, final Path originalFileName)
			throws IOException {
		if (backUpFileName != null && Files.exists(backUpFileName)) {
			if (Files.exists(originalFileName)) {
				Files.delete(originalFileName);
			}
			Files.move(backUpFileName, originalFileName);
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
			// custom, blizz & none use custom ruleset
			case CUSTOM:
			case BLIZZARD_SC2_HEROES:
			case NONE:
				backUpOriginalSettingsFiles();
				applyChangesToFiles();
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
		
		final Path directoryPath = iniFile.getParent();
		int i = 0;
		Path backupFile;
		
		// ruleset file
		if (Files.exists(rulesetFile)) {
			do {
				//noinspection ObjectAllocationInLoop
				backupFile = directoryPath.resolve("MPQEditor_Ruleset_" + i + ".tmp");
				++i;
				if (i > 999) {
					throw new IOException("Could not find unique name for MPQEditor_Ruleset.ini's backup copy.");
				}
			} while (Files.exists(backupFile));
			Files.copy(rulesetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
			rulesetFileBackUp = backupFile;
		}
		
		// ini file
		if (Files.exists(iniFile)) {
			i = 0;
			do {
				//noinspection ObjectAllocationInLoop
				backupFile = directoryPath.resolve("MPQEditor_" + i + ".tmp");
				++i;
				if (i > 999) {
					throw new IOException("Could not find unique name for MPQEditor.ini's backup copy.");
				}
			} while (Files.exists(backupFile));
			Files.copy(iniFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
			iniFileBackUp = backupFile;
		}
		
		backupActive = true;
	}
	
	/**
	 * Applies the necessary changes to the ini files.
	 */
	private void applyChangesToFiles() throws IOException {
		if (!Files.exists(iniFile)) {
			logger.error(
					"MpqEditor's ini file does not exist. It would be located at '{}'. The editor will run with its factory settings.",
					iniFile.toAbsolutePath());
			return;
		}
		int gameId = 6;
		try {
			final INIBuilderParameters params = new Parameters().ini().setFile(iniFile.toFile()).setEncoding(UTF_8);
			final FileBasedConfigurationBuilder<INIConfiguration> b =
					new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
			final INIConfiguration ini = b.getConfiguration();
			
			final SubnodeConfiguration options = ini.getSection(OPTIONS);
			gameId = getGameIdPropertyValue(compression);
			options.setProperty(GAME_ID, gameId);
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
	private static int getGameIdPropertyValue(final MpqEditorCompression compression) {
		if (compression == MpqEditorCompression.BLIZZARD_SC2_HEROES) {
			return 11;
		}
		return 13;
	}
	
	/**
	 *
	 */
	private void writeMpqRuleset() throws IOException {
		INIConfiguration ini = null;
		if (Files.exists(rulesetFile)) {
			try {
				final INIBuilderParameters params =
						new Parameters().ini().setFile(rulesetFile.toFile()).setEncoding(UTF_8);
				final FileBasedConfigurationBuilder<INIConfiguration> b =
						new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
				ini = b.getConfiguration();
				ini.clear();
			} catch (final ConfigurationException e) {
				logger.error("Error while editing custom ruleset file.", e);
				ini = null;
			}
		}
		if (ini == null) {
			ini = new INIConfiguration();
		}
		final SubnodeConfiguration section = ini.getSection(CUSTOM_RULES);
		section.setProperty(MPQ_VERSION, 3); // possible: 0,1,2,3 (higher has bigger header); sc2 maps use 3
		// 1 adds longer archives, 2 adds replacement that is supposed to be more effective, 3 adds more like md5
		section.setProperty(ATTR_FLAGS, 0); // default 5 "(attributes)" file becomes shorter, 0 = no file
		// (Attributes) file is not required for at least Obs UI
		section.setProperty(SECTOR_SIZE, 16_384); // default 16384, 1024-4096 made AhliObs bigger, SC2 maps use 16384
		section.setProperty(RAW_CHUNK_SIZE, 0); // default 0
		
		switch (compression) {
			case CUSTOM:
				if (customRules != null) {
					for (final MpqEditorCompressionRule customRule : customRules) {
						if (customRule != null) {
							ini.addProperty(CUSTOM_RULE_PROPERTY_KEY, customRule.toString());
						} else {
							throw new IllegalArgumentException(
									"Compression Rules in MpqEditorSettingsInterface has null entry");
						}
					}
				} else {
					section.addProperty(DEFAULT, NO_COMPRESSION_CUSTOM_RULE);
				}
				break;
			case NONE:
				section.addProperty(DEFAULT, NO_COMPRESSION_CUSTOM_RULE);
				break;
			case SYSTEM_DEFAULT:
			case BLIZZARD_SC2_HEROES:
			default:
				break;
		}
		
		try (final BufferedWriter bw = Files.newBufferedWriter(rulesetFile)) {
			ini.write(bw);
		} catch (final ConfigurationException | IOException e) {
			throw new IOException("Could not write '" + rulesetFile.toAbsolutePath() + "'.", e);
		}
		
		// remove custom ruleset line beginnings
		if (compression == MpqEditorCompression.CUSTOM) {
			final List<String> editedLines;
			try (final Stream<String> lineStream = Files.lines(rulesetFile)) {
				editedLines =
						lineStream.map(line -> line.replace(SPACESPACEEQUALSSPACE, "")).collect(Collectors.toList());
			}
			try (final BufferedWriter bw = Files.newBufferedWriter(rulesetFile)) {
				final String separator = System.lineSeparator();
				for (final String line : editedLines) {
					bw.write(line);
					bw.write(separator);
				}
			}
		}
	}
	
	/**
	 * Returns the ruleset array used by this class.
	 *
	 * @return
	 */
	public MpqEditorCompressionRule[] getCustomRuleSet() {
		return customRules;
	}
	
	/**
	 * @param customRules
	 */
	public void setCustomRules(final MpqEditorCompressionRule... customRules) {
		this.customRules = customRules;
	}
	
	@Override
	public Object deepCopy() {
		final MpqEditorSettingsInterface clone = new MpqEditorSettingsInterface();
		if (customRules != null) {
			clone.customRules = new MpqEditorCompressionRule[customRules.length];
			for (int i = 0, len = customRules.length; i < len; ++i) {
				customRules[i] = customRules[i] == null ? null : (MpqEditorCompressionRule) customRules[i].deepCopy();
			}
		}
		clone.compression = compression;
		clone.iniFileBackUp = iniFileBackUp;
		clone.rulesetFileBackUp = rulesetFileBackUp;
		clone.backupActive = backupActive;
		return clone;
	}
}
