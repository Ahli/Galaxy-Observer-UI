package application.integration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsInterface {
	static Logger LOGGER = LogManager.getLogger(SettingsInterface.class);
	
	private String SETTINGS_FILE = "";
	private String SC2_Path = "", HEROES_Path = "", HEROES_PTR_Path = "";
	private boolean ptrActive = false, SC2_64bit = false, HEROES_64bit = false, HEROES_PTR_64bit = false,
			HEROES_protectMPQ = false, SC2_protectMPQ = false, buildUnprotectedToo = false;

	/**
	 * Set the File path of the Settings file.
	 * 
	 * @param path
	 */
	public void setSettingsFilePath(String path) {
		SETTINGS_FILE = path;
	}

	/**
	 * Read all Settings from the Settings file.
	 * 
	 * @throws FileNotFoundException
	 */
	public void readSettingsFromFile() throws FileNotFoundException {
		InputStreamReader is = new InputStreamReader(new FileInputStream(SETTINGS_FILE), StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(is);
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Parse a line from the Settings file.
	 * 
	 * @param line
	 */
	private void parseLine(String line) {
		String val = "";
		if (line.startsWith("Heroes_Path")) {
			val = getValFromIniLine(line);
			HEROES_Path = val;
		} else if (line.startsWith("HeroesPTR_Path")) {
			val = getValFromIniLine(line);
			HEROES_PTR_Path = val;
		} else if (line.startsWith("StarCraft2_Path")) {
			val = getValFromIniLine(line);
			SC2_Path = val;
		} else if (line.startsWith("PTRactive")) {
			val = getValFromIniLine(line);
			ptrActive = Boolean.parseBoolean(val);
		} else if (line.startsWith("Heroes_use64bit")) {
			val = getValFromIniLine(line);
			HEROES_64bit = Boolean.parseBoolean(val);
		} else if (line.startsWith("HeroesPTR_use64bit")) {
			val = getValFromIniLine(line);
			HEROES_PTR_64bit = Boolean.parseBoolean(val);
		} else if (line.startsWith("StarCraft2_use64bit")) {
			val = getValFromIniLine(line);
			SC2_64bit = Boolean.parseBoolean(val);
		}
	}

	/**
	 * Reads the Value from a line from the Settings file.
	 * 
	 * @param line
	 * @return
	 */
	private String getValFromIniLine(String line) {
		return line.substring(line.indexOf('=') + 1);
	}

	public String getSC2Path() {
		return SC2_Path;
	}

	public String getHeroesPath() {
		return HEROES_Path;
	}

	public String getHeroesPtrPath() {
		return HEROES_PTR_Path;
	}

	public boolean isPtrActive() {
		return ptrActive;
	}

	public boolean isSC264bit() {
		return SC2_64bit;
	}

	public boolean isHeroesPtr64bit() {
		return HEROES_PTR_64bit;
	}

	public boolean isHeroes64bit() {
		return HEROES_64bit;
	}

	public boolean isHeroesProtectMPQ() {
		return HEROES_protectMPQ;
	}

	public boolean isSC2ProtectMPQ() {
		return SC2_protectMPQ;
	}

	public boolean isBuildUnprotectedToo() {
		return buildUnprotectedToo;
	}
}
