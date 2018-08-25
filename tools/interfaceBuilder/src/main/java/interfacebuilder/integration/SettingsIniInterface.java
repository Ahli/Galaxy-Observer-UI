package interfacebuilder.integration;

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
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Interface Class for the Settings .ini file.
 *
 * @author Ahli
 */
public class SettingsIniInterface {
	private static final Logger logger = LogManager.getLogger(SettingsIniInterface.class);
	
	private static final String CATEGORY_GAME_PATHS = "GamePaths";
	private static final String HEROES_PATH = "Heroes_Path";
	private static final String HEROES_PTR_PATH = "HeroesPTR_Path";
	private static final String STARCRAFT2_PATH = "StarCraft2_Path";
	private static final String HEROES_USE64BIT = "Heroes_use64bit";
	private static final String HEROES_PTR_USE64BIT = "HeroesPTR_use64bit";
	private static final String STARCRAFT2_USE64BIT = "StarCraft2_use64bit";
	private static final String CATEGORY_COMMAND_LINE_TOOL = "CommandLineExecution";
	private static final String CATEGORY_GUI_TOOL = "GuiExecution";
	private static final String VERIFY_XML = "verifyXml";
	private static final String VERIFY_LAYOUT = "verifyLayout";
	private static final String REPAIR_LAYOUT_ORDER = "repairLayoutOrder";
	private static final String COMPRESS_XML = "compressXml";
	private static final String COMPRESS_MPQ = "compressMPQ";
	private static final String BUILD_UNPROTECTED_TOO = "buildUnprotectedToo";
	private static final String UTF_8 = "UTF-8";
	private static final String CATEGORY_INTERNAL_VARIABLES = "InternalVariables";
	private static final String HEROES_PTR_ACTIVE = "HeroesPTRactive";
	private String settingsFilePath;
	private String sc2Path = "";
	private String heroesPath = "";
	private String heroesPtrPath = "";
	private boolean heroesPtrActive;
	private boolean sc2X64;
	private boolean heroesX64;
	private boolean heroesPtrX64;
	private boolean cmdLineVerifyXml = true;
	private boolean cmdLineRepairLayoutOrder = true;
	private boolean cmdLineVerifyLayout = true;
	private boolean cmdLineCompressXml;
	/* compression: 0=None, 1=Blizz, 2=ExperimentalBest, 3=SystemDefault */
	private int cmdLineCompressMpq;
	private boolean cmdLineBuildUnprotectedToo;
	private boolean guiVerifyXml = true;
	private boolean guiRepairLayoutOrder = true;
	private boolean guiVerifyLayout = true;
	private boolean guiCompressXml = true;
	/* compression: 0=None, 1=Blizz, 2=ExperimentalBest, 3=SystemDefault */
	private int guiCompressMpq = 3;
	private boolean guiBuildUnprotectedToo;
	
	/**
	 * Constructor.
	 *
	 * @param settingsFilePath
	 */
	public SettingsIniInterface(final String settingsFilePath) {
		this.settingsFilePath = settingsFilePath;
		try {
			readSettingsFromFile();
		} catch (final IOException e) {
			logger.error("Failed to load settings from ini at construction.", e);
		}
	}
	
	/**
	 * Read all Settings from the Settings file. If that file does not exist, it will be created.
	 *
	 * @throws IOException
	 * 		when there is an error reading the file
	 */
	public void readSettingsFromFile() throws IOException {
		final File f = new File(settingsFilePath);
		if (f.exists()) {
			try {
				logger.info("Loading settings from: " + settingsFilePath);
				final INIBuilderParameters params = new Parameters().ini().setFile(f).setEncoding(UTF_8);
				final FileBasedConfigurationBuilder<INIConfiguration> b =
						new FileBasedConfigurationBuilder<>(INIConfiguration.class).configure(params);
				readValuesFromIni(b.getConfiguration());
			} catch (final ConfigurationException | ExceptionInInitializerError | IllegalArgumentException | NullPointerException e) {
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
		heroesX64 = section.getBoolean(HEROES_USE64BIT, false);
		heroesPtrX64 = section.getBoolean(HEROES_PTR_USE64BIT, false);
		sc2X64 = section.getBoolean(STARCRAFT2_USE64BIT, false);
		
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
		
		section = ini.getSection(CATEGORY_INTERNAL_VARIABLES);
		heroesPtrActive = section.getBoolean(HEROES_PTR_ACTIVE, false);
	}
	
	/**
	 * Persists the Settings file.
	 *
	 * @throws IOException
	 */
	public void writeSettingsToFile() throws IOException {
		final Parameters params = new Parameters();
		final FileBasedConfigurationBuilder<INIConfiguration> b =
				new FileBasedConfigurationBuilder<>(INIConfiguration.class)
						.configure(params.ini().setFile(new File(settingsFilePath)).setEncoding(UTF_8));
		INIConfiguration ini;
		try {
			// load current file
			ini = b.getConfiguration();
		} catch (final ConfigurationException e) {
			// create new one if not present
			ini = new INIConfiguration();
		}
		writeValuesToIni(ini);
		try (final BufferedWriter bw = Files.newBufferedWriter(Paths.get(settingsFilePath))) {
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
		section.setProperty(HEROES_PATH, heroesPath);
		section.setProperty(HEROES_PTR_PATH, heroesPtrPath);
		section.setProperty(STARCRAFT2_USE64BIT, sc2X64);
		section.setProperty(HEROES_USE64BIT, heroesX64);
		section.setProperty(HEROES_PTR_USE64BIT, heroesPtrX64);
		
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
		
		section = ini.getSection(CATEGORY_INTERNAL_VARIABLES);
		section.setProperty(HEROES_PTR_ACTIVE, heroesPtrActive);
	}
	
	/**
	 * @return
	 */
	public boolean isGuiVerifyXml() {
		return guiVerifyXml;
	}
	
	/**
	 * @param guiVerifyXml
	 */
	public void setGuiVerifyXml(final boolean guiVerifyXml) {
		this.guiVerifyXml = guiVerifyXml;
	}
	
	/**
	 * @return
	 */
	public boolean isGuiRepairLayoutOrder() {
		return guiRepairLayoutOrder;
	}
	
	/**
	 * @param guiRepairLayoutOrder
	 */
	public void setGuiRepairLayoutOrder(final boolean guiRepairLayoutOrder) {
		this.guiRepairLayoutOrder = guiRepairLayoutOrder;
	}
	
	/**
	 * @return
	 */
	public boolean isGuiVerifyLayout() {
		return guiVerifyLayout;
	}
	
	/**
	 * @param guiVerifyLayout
	 */
	public void setGuiVerifyLayout(final boolean guiVerifyLayout) {
		this.guiVerifyLayout = guiVerifyLayout;
	}
	
	/**
	 * @return
	 */
	public boolean isGuiCompressXml() {
		return guiCompressXml;
	}
	
	/**
	 * @param guiCompressXml
	 */
	public void setGuiCompressXml(final boolean guiCompressXml) {
		this.guiCompressXml = guiCompressXml;
	}
	
	/**
	 * @return
	 */
	public int getGuiCompressMpq() {
		return guiCompressMpq;
	}
	
	/**
	 * @param guiCompressMpq
	 */
	public void setGuiCompressMpq(final int guiCompressMpq) {
		this.guiCompressMpq = guiCompressMpq;
	}
	
	/**
	 * @return
	 */
	public boolean isGuiBuildUnprotectedToo() {
		return guiBuildUnprotectedToo;
	}
	
	/**
	 * @param guiBuildUnprotectedToo
	 */
	public void setGuiBuildUnprotectedToo(final boolean guiBuildUnprotectedToo) {
		this.guiBuildUnprotectedToo = guiBuildUnprotectedToo;
	}
	
	/**
	 * Returns path to settings file.
	 *
	 * @return path as String
	 */
	public String getSettingsFilePath() {
		return settingsFilePath;
	}
	
	/**
	 * Constructor.
	 *
	 * @param path
	 */
	public void setSettingsFilePath(final String path) {
		settingsFilePath = path;
	}
	
	/**
	 * @return
	 */
	public String getHeroesPath() {
		return heroesPath;
	}
	
	/**
	 * @param heroesPath
	 */
	public void setHeroesPath(final String heroesPath) {
		this.heroesPath = heroesPath;
	}
	
	/**
	 * @return
	 */
	public String getHeroesPtrPath() {
		return heroesPtrPath;
	}
	
	/**
	 * @param heroesPtrPath
	 */
	public void setHeroesPtrPath(final String heroesPtrPath) {
		this.heroesPtrPath = heroesPtrPath;
	}
	
	/**
	 * @return
	 */
	public boolean isHeroesPtrActive() {
		return heroesPtrActive;
	}
	
	/**
	 * @param heroesPtrActive
	 */
	public void setIsHeroesPtrActive(final boolean heroesPtrActive) {
		this.heroesPtrActive = heroesPtrActive;
	}
	
	/**
	 * @return
	 */
	public boolean isSc64bit() {
		return sc2X64;
	}
	
	/**
	 * @return
	 */
	public boolean isHeroesPtr64bit() {
		return heroesPtrX64;
	}
	
	/**
	 * @return
	 */
	public boolean isHeroes64bit() {
		return heroesX64;
	}
	
	/**
	 * @param sc2Is64Bit
	 */
	public void setSc2Is64Bit(final boolean sc2Is64Bit) {
		sc2X64 = sc2Is64Bit;
	}
	
	/**
	 * @param heroesIs64Bit
	 */
	public void setHeroesIs64Bit(final boolean heroesIs64Bit) {
		heroesX64 = heroesIs64Bit;
	}
	
	/**
	 * @param heroesPtrIs64Bit
	 */
	public void setHeroesPtrIs64Bit(final boolean heroesPtrIs64Bit) {
		heroesPtrX64 = heroesPtrIs64Bit;
	}
	
	/**
	 * @return
	 */
	public boolean isCmdLineCompressXml() {
		return cmdLineCompressXml;
	}
	
	/**
	 * @param cmdLineCompressXml
	 */
	public void setCmdLineCompressXml(final boolean cmdLineCompressXml) {
		this.cmdLineCompressXml = cmdLineCompressXml;
	}
	
	/**
	 * @return
	 */
	public int getCmdLineCompressMpq() {
		return cmdLineCompressMpq;
	}
	
	/**
	 * @param cmdLineCompressMpq
	 */
	public void setCmdLineCompressMpq(final int cmdLineCompressMpq) {
		this.cmdLineCompressMpq = cmdLineCompressMpq;
	}
	
	/**
	 * @return
	 */
	public String getSc2Path() {
		return sc2Path;
	}
	
	/**
	 * @param sc2Path
	 */
	public void setSc2Path(final String sc2Path) {
		this.sc2Path = sc2Path;
	}
	
	/**
	 * @return
	 */
	public boolean isCmdLineRepairLayoutOrder() {
		return cmdLineRepairLayoutOrder;
	}
	
	/**
	 * @param cmdLineRepairLayoutOrder
	 */
	public void setCmdLineRepairLayoutOrder(final boolean cmdLineRepairLayoutOrder) {
		this.cmdLineRepairLayoutOrder = cmdLineRepairLayoutOrder;
	}
	
	/**
	 * @return
	 */
	public boolean isCmdLineVerifyLayout() {
		return cmdLineVerifyLayout;
	}
	
	/**
	 * @param cmdLineVerifyLayout
	 */
	public void setCmdLineVerifyLayout(final boolean cmdLineVerifyLayout) {
		this.cmdLineVerifyLayout = cmdLineVerifyLayout;
	}
	
	/**
	 * @return
	 */
	public boolean isCmdLineBuildUnprotectedToo() {
		return cmdLineBuildUnprotectedToo;
	}
	
	/**
	 * @param cmdLineBuildUnprotectedToo
	 */
	public void setCmdLineBuildUnprotectedToo(final boolean cmdLineBuildUnprotectedToo) {
		this.cmdLineBuildUnprotectedToo = cmdLineBuildUnprotectedToo;
	}
	
	/**
	 * @return
	 */
	public boolean isCmdLineVerifyXml() {
		return cmdLineVerifyXml;
	}
	
	/**
	 * @param cmdLineVerifyXml
	 */
	public void setCmdLineVerifyXml(final boolean cmdLineVerifyXml) {
		this.cmdLineVerifyXml = cmdLineVerifyXml;
	}
}
