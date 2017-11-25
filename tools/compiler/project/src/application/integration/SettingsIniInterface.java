package application.integration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interface Class for the Settings .ini file.
 * 
 * @author Ahli
 *
 */
public class SettingsIniInterface {
	private static Logger logger = LogManager.getLogger(SettingsIniInterface.class);
	
	private String settingsFilePath = "";
	private String sc2Path = "", heroesPath = "", heroesPtrPath = "";
	private boolean ptrActive = false, sc2X64 = false, heroesX64 = false, heroesPtrX64 = false,
			heroesProtectMpq = false, sc2ProtectMpq = false, buildUnprotectedToo = false;
	
	/**
	 * Constructor.
	 * 
	 * @param settingsFilePath
	 */
	public SettingsIniInterface(final String settingsFilePath) {
		this.settingsFilePath = settingsFilePath;
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
	 * Returns path to settings file.
	 * 
	 * @return path as String
	 */
	public String getSettingsFilePath() {
		return settingsFilePath;
	}
	
	/**
	 * Read all Settings from the Settings file.
	 * 
	 * @throws FileNotFoundException
	 *             when no file exists
	 */
	public void readSettingsFromFile() throws FileNotFoundException {
		final InputStreamReader is = new InputStreamReader(new FileInputStream(settingsFilePath),
				StandardCharsets.UTF_8);
		final BufferedReader br = new BufferedReader(is);
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
			}
		}
	}
	
	/**
	 * Parse a line from the Settings file.
	 * 
	 * @param line
	 *            line of settings
	 */
	private void parseLine(final String line) {
		String val = "";
		if (line.startsWith("Heroes_Path")) {
			val = getValFromIniLine(line);
			heroesPath = val;
		} else if (line.startsWith("HeroesPTR_Path")) {
			val = getValFromIniLine(line);
			heroesPtrPath = val;
		} else if (line.startsWith("StarCraft2_Path")) {
			val = getValFromIniLine(line);
			sc2Path = val;
		} else if (line.startsWith("PTRactive")) {
			val = getValFromIniLine(line);
			ptrActive = Boolean.parseBoolean(val);
		} else if (line.startsWith("Heroes_use64bit")) {
			val = getValFromIniLine(line);
			heroesX64 = Boolean.parseBoolean(val);
		} else if (line.startsWith("HeroesPTR_use64bit")) {
			val = getValFromIniLine(line);
			heroesPtrX64 = Boolean.parseBoolean(val);
		} else if (line.startsWith("StarCraft2_use64bit")) {
			val = getValFromIniLine(line);
			sc2X64 = Boolean.parseBoolean(val);
		} else if (line.startsWith("Heroes_protectMPQ")) {
			val = getValFromIniLine(line);
			heroesProtectMpq = Boolean.parseBoolean(val);
		} else if (line.startsWith("StarCraft2_protectMPQ")) {
			val = getValFromIniLine(line);
			sc2ProtectMpq = Boolean.parseBoolean(val);
		} else if (line.startsWith("buildUnprotectedToo")) {
			val = getValFromIniLine(line);
			buildUnprotectedToo = Boolean.parseBoolean(val);
		}
	}
	
	/**
	 * Reads the Value from a line from the Settings file.
	 * 
	 * @param line
	 * @return
	 */
	private String getValFromIniLine(final String line) {
		return line.substring(line.indexOf('=') + 1);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSC2Path() {
		return sc2Path;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getHeroesPath() {
		return heroesPath;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getHeroesPtrPath() {
		return heroesPtrPath;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPtrActive() {
		return ptrActive;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSC264bit() {
		return sc2X64;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHeroesPtr64bit() {
		return heroesPtrX64;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHeroes64bit() {
		return heroesX64;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHeroesProtectMPQ() {
		return heroesProtectMpq;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSC2ProtectMPQ() {
		return sc2ProtectMpq;
	}
	
	/**
	 * 
	 */
	public boolean isBuildUnprotectedToo() {
		return buildUnprotectedToo;
	}
}
