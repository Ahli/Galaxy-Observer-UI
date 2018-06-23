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
	private String baseDataFolderName = "Base.SC2Data";
	private String rootExeName = "StarCraft II.exe";
	private String ptrRootExeName = "StarCraft II Public Test.exe";
	private String switcherExeNameX32 = "SC2Switcher.exe";
	private String switcherExeNameX64 = "SC2Switcher_x64.exe";
	private String supportDirectoryX32 = "Support";
	private String supportDirectoryX64 = "Support64";
	
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
	
	/**
	 * Returns the name of the game's rootExecutable.
	 *
	 * @return
	 */
	public String getRootExeName() {
		return rootExeName;
	}
	
	/**
	 * Set the name of the game's rootExecutable.
	 *
	 * @param rootExeName
	 */
	public void setRootExeName(final String rootExeName) {
		this.rootExeName = rootExeName;
	}
	
	public String getPtrRootExeName() {
		return ptrRootExeName;
	}
	
	/**
	 * Set the name of the game's rootExecutable of the PTR client.
	 *
	 * @param ptrRootExeName
	 */
	public void setPtrRootExeName(final String ptrRootExeName) {
		this.ptrRootExeName = ptrRootExeName;
	}
	
	public String getSwitcherExeNameX32() {
		return switcherExeNameX32;
	}
	
	public void setSwitcherExeNameX32(final String switcherExeNameX32) {
		this.switcherExeNameX32 = switcherExeNameX32;
	}
	
	public String getSwitcherExeNameX64() {
		return switcherExeNameX64;
	}
	
	public void setSwitcherExeNameX64(final String switcherExeNameX64) {
		this.switcherExeNameX64 = switcherExeNameX64;
	}
	
	public String getSupportDirectoryX32() {
		return supportDirectoryX32;
	}
	
	public void setSupportDirectoryX32(final String supportDirectoryX32) {
		this.supportDirectoryX32 = supportDirectoryX32;
	}
	
	public String getSupportDirectoryX64() {
		return supportDirectoryX64;
	}
	
	public void setSupportDirectoryX64(final String supportDirectoryX64) {
		this.supportDirectoryX64 = supportDirectoryX64;
	}
}
