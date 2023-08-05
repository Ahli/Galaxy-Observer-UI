// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.INIBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Interface Class for the Settings .ini file.
 *
 * @author Ahli
 */
@Log4j2
public class SettingsIniInterface {
	
	private static final String CATEGORY_GAME_PATHS = "GamePaths";
	private static final String HEROES_PATH = "Heroes_Path";
	private static final String HEROES_PTR_PATH = "HeroesPTR_Path";
	private static final String STARCRAFT2_PATH = "StarCraft2_Path";
	private static final String STARCRAFT2_USE64BIT = "StarCraft2_use64bit";
	private static final String STARCRAFT2_PTR_PATH = "StarCraft2PTR_Path";
	private static final String STARCRAFT2_PTR_USE64BIT = "StarCraft2PTR_use64bit";
	private static final String CATEGORY_COMMAND_LINE_TOOL = "CommandLineExecution";
	private static final String CATEGORY_GUI_TOOL = "GuiExecution";
	private static final String VERIFY_XML = "verifyXml";
	private static final String VERIFY_LAYOUT = "verifyLayout";
	private static final String REPAIR_LAYOUT_ORDER = "repairLayoutOrder";
	private static final String COMPRESS_XML = "compressXml";
	private static final String COMPRESS_MPQ = "compressMPQ";
	private static final String BUILD_UNPROTECTED_TOO = "buildUnprotectedToo";
	private static final String UTF_8 = "UTF-8";
	private final Path settingsFilePath;
	@Getter
	@Setter
	private String sc2Path = "";
	@Getter
	@Setter
	private String sc2PtrPath = "";
	@Getter
	@Setter
	private String heroesPath = "";
	@Getter
	@Setter
	private String heroesPtrPath = "";
	@Getter
	@Setter
	private boolean sc2X64;
	@Getter
	@Setter
	private boolean sc2PtrX64;
	@Getter
	@Setter
	private boolean cmdLineVerifyXml = true;
	@Getter
	@Setter
	private boolean cmdLineRepairLayoutOrder = true;
	@Getter
	@Setter
	private boolean cmdLineVerifyLayout = true;
	@Getter
	@Setter
	private boolean cmdLineCompressXml;
	@Getter
	@Setter
	/* compression: 0=None, 1=Blizz, 2=ExperimentalBest, 3=SystemDefault */ private int cmdLineCompressMpq;
	@Getter
	@Setter
	private boolean cmdLineBuildUnprotectedToo;
	@Getter
	@Setter
	private boolean guiVerifyXml = true;
	@Getter
	@Setter
	private boolean guiRepairLayoutOrder = true;
	@Getter
	@Setter
	private boolean guiVerifyLayout = true;
	@Getter
	@Setter
	private boolean guiCompressXml = true;
	@Getter
	@Setter
	/* compression: 0=None, 1=Blizz, 2=ExperimentalBest, 3=SystemDefault */ private int guiCompressMpq = 3;
	@Getter
	@Setter
	private boolean guiBuildUnprotectedToo;
	
	/**
	 * Constructor.
	 *
	 * @param settingsFilePath
	 */
	public SettingsIniInterface(final Path settingsFilePath) {
		this.settingsFilePath = settingsFilePath;
		try {
			readSettingsFromFilePrivate();
		} catch (final IOException e) {
			log.error(
					String.format("Failed to load settings from ini at construction. Filepath=%s", settingsFilePath),
					e);
		}
	}
	
	/**
	 * Read all Settings from the Settings file. If that file does not exist, it will be created.
	 *
	 * @throws IOException
	 * 		when there is an error reading the file
	 */
	private void readSettingsFromFilePrivate() throws IOException {
		if (Files.exists(settingsFilePath)) {
			try {
				log.info("Loading settings from: {}", settingsFilePath);
				final INIBuilderParameters params =
						new Parameters().ini().setFile(settingsFilePath.toFile()).setEncoding(UTF_8);
				final FileBasedConfigurationBuilder<INIConfiguration> b =
						new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
				readValuesFromIni(b.getConfiguration());
			} catch (final ConfigurationException | ExceptionInInitializerError | IllegalArgumentException e) {
				throw new IOException("Could not read settings.ini.", e);
			}
		} else {
			writeSettingsToFile();
		}
	}
	
	/**
	 * @param ini
	 */
	private void readValuesFromIni(final INIConfiguration ini) {
		final String emptryStr = "";
		SubnodeConfiguration section = ini.getSection(CATEGORY_GAME_PATHS);
		heroesPath = section.getString(HEROES_PATH, emptryStr);
		heroesPtrPath = section.getString(HEROES_PTR_PATH, emptryStr);
		sc2Path = section.getString(STARCRAFT2_PATH, emptryStr);
		sc2PtrPath = section.getString(STARCRAFT2_PTR_PATH, emptryStr);
		sc2X64 = section.getBoolean(STARCRAFT2_USE64BIT, false);
		sc2PtrX64 = section.getBoolean(STARCRAFT2_PTR_USE64BIT, false);
		
		section = ini.getSection(CATEGORY_COMMAND_LINE_TOOL);
		cmdLineVerifyXml = section.getBoolean(VERIFY_XML, true);
		cmdLineVerifyLayout = section.getBoolean(VERIFY_LAYOUT, true);
		cmdLineRepairLayoutOrder = section.getBoolean(REPAIR_LAYOUT_ORDER, true);
		cmdLineCompressXml = section.getBoolean(COMPRESS_XML, false);
		cmdLineCompressMpq = section.getInt(COMPRESS_MPQ, 0);
		cmdLineBuildUnprotectedToo = section.getBoolean(BUILD_UNPROTECTED_TOO, false);
		
		section = ini.getSection(CATEGORY_GUI_TOOL);
		guiVerifyXml = section.getBoolean(VERIFY_XML, true);
		guiVerifyLayout = section.getBoolean(VERIFY_LAYOUT, true);
		guiRepairLayoutOrder = section.getBoolean(REPAIR_LAYOUT_ORDER, true);
		guiCompressXml = section.getBoolean(COMPRESS_XML, true);
		guiCompressMpq = section.getInt(COMPRESS_MPQ, 3);
		guiBuildUnprotectedToo = section.getBoolean(BUILD_UNPROTECTED_TOO, false);
	}
	
	/**
	 * Persists the Settings file.
	 *
	 * @throws IOException
	 */
	public void writeSettingsToFile() throws IOException {
		final Parameters params = new Parameters();
		final FileBasedConfigurationBuilder<INIConfiguration> b =
				new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params.ini()
						.setFile(settingsFilePath.toFile())
						.setEncoding(UTF_8));
		INIConfiguration ini;
		try {
			// load current file
			ini = b.getConfiguration();
		} catch (final ConfigurationException e) {
			log.trace("Failed to load ini file.", e);
			// create new one if not present
			ini = new INIConfiguration();
		}
		writeValuesToIni(ini);
		try (final BufferedWriter bw = Files.newBufferedWriter(settingsFilePath)) {
			ini.write(bw);
		} catch (final ConfigurationException | NoSuchFileException e) {
			throw new IOException("Could not write settings.ini.", e);
		}
	}
	
	/**
	 * @param ini
	 */
	private void writeValuesToIni(final INIConfiguration ini) {
		SubnodeConfiguration section = ini.getSection(CATEGORY_GAME_PATHS);
		section.setProperty(STARCRAFT2_PATH, sc2Path);
		section.setProperty(STARCRAFT2_PTR_PATH, sc2PtrPath);
		section.setProperty(HEROES_PATH, heroesPath);
		section.setProperty(HEROES_PTR_PATH, heroesPtrPath);
		section.setProperty(STARCRAFT2_USE64BIT, sc2X64);
		section.setProperty(STARCRAFT2_PTR_USE64BIT, sc2PtrX64);
		
		section = ini.getSection(CATEGORY_COMMAND_LINE_TOOL);
		section.setProperty(VERIFY_XML, cmdLineVerifyXml);
		section.setProperty(VERIFY_LAYOUT, cmdLineVerifyLayout);
		section.setProperty(REPAIR_LAYOUT_ORDER, cmdLineRepairLayoutOrder);
		section.setProperty(COMPRESS_XML, cmdLineCompressXml);
		section.setProperty(COMPRESS_MPQ, cmdLineCompressMpq);
		section.setProperty(BUILD_UNPROTECTED_TOO, cmdLineBuildUnprotectedToo);
		
		section = ini.getSection(CATEGORY_GUI_TOOL);
		section.setProperty(VERIFY_XML, guiVerifyXml);
		section.setProperty(VERIFY_LAYOUT, guiVerifyLayout);
		section.setProperty(REPAIR_LAYOUT_ORDER, guiRepairLayoutOrder);
		section.setProperty(COMPRESS_XML, guiCompressXml);
		section.setProperty(COMPRESS_MPQ, guiCompressMpq);
		section.setProperty(BUILD_UNPROTECTED_TOO, guiBuildUnprotectedToo);
	}
	
}
