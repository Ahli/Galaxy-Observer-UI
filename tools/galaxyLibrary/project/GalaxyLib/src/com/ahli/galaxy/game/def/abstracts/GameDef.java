package com.ahli.galaxy.game.def.abstracts;

import java.util.Arrays;

/**
 * @author Ahli
 */
public abstract class GameDef {
	private String[] coreModsOrDirectories = null;
	private String defaultRaceId = "Terr";
	private String documentsGameDirectoryName = "StarCraft II";
	private String documentsInterfaceSubdirectoryName = "Interfaces";
	private String modFileEnding = ".sc2mod";
	private String modsSubDirectory = "mods";
	private String name = "StarCraft II";
	private String nameHandle = "sc2";
	private String layoutFileEnding = ".sc2layout";
	
	/**
	 * @return the layoutFileEnding
	 */
	public String getLayoutFileEnding() {
		return layoutFileEnding;
	}
	
	/**
	 * @param layoutFileEnding
	 *            the layoutFileEnding to set
	 */
	public void setLayoutFileEnding(final String layoutFileEnding) {
		this.layoutFileEnding = layoutFileEnding;
	}
	
	/**
	 * @return the coreModsOrDirectories
	 */
	public String[] getCoreModsOrDirectories() {
		return Arrays.copyOf(coreModsOrDirectories, 1);
	}
	
	/**
	 * @return the defaultRaceId
	 */
	public String getDefaultRaceId() {
		return defaultRaceId;
	}
	
	/**
	 * @return
	 */
	public String getDocumentsGameDirectoryName() {
		return documentsGameDirectoryName;
	}
	
	/**
	 * @return the documentsInterfaceSubdirectoryName
	 */
	public String getDocumentsInterfaceSubdirectoryName() {
		return documentsInterfaceSubdirectoryName;
	}
	
	/**
	 * @return the modFileEnding
	 */
	public String getModFileEnding() {
		return modFileEnding;
	}
	
	/**
	 * @return the modsSubDirectory
	 */
	public String getModsSubDirectory() {
		return modsSubDirectory;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the nameHandle
	 */
	public String getNameHandle() {
		return nameHandle;
	}
	
	/**
	 * Saves a copy of the specified array.
	 * 
	 * @param coreModsOrDirectories
	 *            the coreModsOrDirectories to set
	 */
	public void setCoreModsOrDirectories(final String[] coreModsOrDirectories) {
		this.coreModsOrDirectories = Arrays.copyOf(coreModsOrDirectories, coreModsOrDirectories.length);
	}
	
	/**
	 * @param defaultRaceId
	 *            the defaultRaceId to set
	 */
	public void setDefaultRaceId(final String defaultRaceId) {
		this.defaultRaceId = defaultRaceId;
	}
	
	/**
	 * @param documentsGameDirectoryName
	 *            the documentsGameDirectoryName to set
	 */
	public void setDocumentsGameDirectoryName(final String documentsGameDirectoryName) {
		this.documentsGameDirectoryName = documentsGameDirectoryName;
	}
	
	/**
	 * @param documentsInterfaceSubdirectoryName
	 *            the documentsInterfaceSubdirectoryName to set
	 */
	public void setDocumentsInterfaceSubdirectoryName(final String documentsInterfaceSubdirectoryName) {
		this.documentsInterfaceSubdirectoryName = documentsInterfaceSubdirectoryName;
	}
	
	/**
	 * @param modFileEnding
	 *            the modFileEnding to set
	 */
	public void setModFileEnding(final String modFileEnding) {
		this.modFileEnding = modFileEnding;
	}
	
	/**
	 * @param modsSubDirectory
	 *            the modsSubDirectory to set
	 */
	public void setModsSubDirectory(final String modsSubDirectory) {
		this.modsSubDirectory = modsSubDirectory;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * @param nameHandle
	 *            the nameHandle to set
	 */
	public void setNameHandle(final String nameHandle) {
		this.nameHandle = nameHandle;
	}
	
}
