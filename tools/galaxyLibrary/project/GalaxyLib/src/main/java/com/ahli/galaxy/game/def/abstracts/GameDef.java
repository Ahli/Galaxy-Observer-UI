package com.ahli.galaxy.game.def.abstracts;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Ahli
 */
public abstract class GameDef implements Serializable {
	private String[] coreModsOrDirectories = null;
	private String defaultRaceId = "Terr";
	private String documentsGameDirectoryName = "StarCraft II";
	private String documentsInterfaceSubdirectoryName = "Interfaces";
	private String modFileEnding = ".sc2mod";
	private String modsSubDirectory = "mods";
	private String name = "StarCraft II";
	private String nameHandle = "sc2";
	private String layoutFileEnding = ".sc2layout";
	private String baseDataFolderName = "Base.SC2Data";
	
	/**
	 * @return the layoutFileEnding
	 */
	public String getLayoutFileEnding() {
		return layoutFileEnding;
	}
	
	/**
	 * @param layoutFileEnding
	 * 		the layoutFileEnding to set
	 */
	public void setLayoutFileEnding(final String layoutFileEnding) {
		this.layoutFileEnding = layoutFileEnding;
	}
	
	/**
	 * @return the coreModsOrDirectories
	 */
	public String[] getCoreModsOrDirectories() {
		return Arrays.copyOf(coreModsOrDirectories, coreModsOrDirectories.length);
	}
	
	/**
	 * Saves a copy of the specified array.
	 *
	 * @param coreModsOrDirectories
	 * 		the coreModsOrDirectories to set
	 */
	public void setCoreModsOrDirectories(final String[] coreModsOrDirectories) {
		this.coreModsOrDirectories = Arrays.copyOf(coreModsOrDirectories, coreModsOrDirectories.length);
	}
	
	/**
	 * @return the defaultRaceId
	 */
	public String getDefaultRaceId() {
		return defaultRaceId;
	}
	
	/**
	 * @param defaultRaceId
	 * 		the defaultRaceId to set
	 */
	public void setDefaultRaceId(final String defaultRaceId) {
		this.defaultRaceId = defaultRaceId;
	}
	
	/**
	 * @return
	 */
	public String getDocumentsGameDirectoryName() {
		return documentsGameDirectoryName;
	}
	
	/**
	 * @param documentsGameDirectoryName
	 * 		the documentsGameDirectoryName to set
	 */
	public void setDocumentsGameDirectoryName(final String documentsGameDirectoryName) {
		this.documentsGameDirectoryName = documentsGameDirectoryName;
	}
	
	/**
	 * @return the documentsInterfaceSubdirectoryName
	 */
	public String getDocumentsInterfaceSubdirectoryName() {
		return documentsInterfaceSubdirectoryName;
	}
	
	/**
	 * @param documentsInterfaceSubdirectoryName
	 * 		the documentsInterfaceSubdirectoryName to set
	 */
	public void setDocumentsInterfaceSubdirectoryName(final String documentsInterfaceSubdirectoryName) {
		this.documentsInterfaceSubdirectoryName = documentsInterfaceSubdirectoryName;
	}
	
	/**
	 * @return the modFileEnding
	 */
	public String getModFileEnding() {
		return modFileEnding;
	}
	
	/**
	 * @param modFileEnding
	 * 		the modFileEnding to set
	 */
	public void setModFileEnding(final String modFileEnding) {
		this.modFileEnding = modFileEnding;
	}
	
	/**
	 * @return the modsSubDirectory
	 */
	public String getModsSubDirectory() {
		return modsSubDirectory;
	}
	
	/**
	 * @param modsSubDirectory
	 * 		the modsSubDirectory to set
	 */
	public void setModsSubDirectory(final String modsSubDirectory) {
		this.modsSubDirectory = modsSubDirectory;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 * 		the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * @return the nameHandle
	 */
	public String getNameHandle() {
		return nameHandle;
	}
	
	/**
	 * @param nameHandle
	 * 		the nameHandle to set
	 */
	public void setNameHandle(final String nameHandle) {
		this.nameHandle = nameHandle;
	}
	
	/**
	 * @return
	 */
	public String getBaseDataFolderName() {
		return baseDataFolderName;
	}
	
	/**
	 * @param baseDataFolderName
	 * 		the baseDataFolderName to set
	 */
	public void setBaseDataFolderName(final String baseDataFolderName) {
		this.baseDataFolderName = baseDataFolderName;
	}
	
}
