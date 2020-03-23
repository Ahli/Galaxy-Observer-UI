// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game.def.abstracts;

import java.util.Arrays;

/**
 * @author Ahli
 */
public abstract class GameDef {
	protected final String[] coreModsOrDirectories;
	protected final String defaultRaceId;
	protected final String defaultConsoleSkinId;
	protected final String documentsGameDirectoryName;
	protected final String documentsInterfaceSubdirectoryName;
	protected final String modFileEnding;
	protected final String modsSubDirectory;
	protected final String name;
	protected final String nameHandle;
	protected final String layoutFileEnding;
	protected final String baseDataFolderName;
	protected final String rootExeName;
	protected final String ptrRootExeName;
	protected final String switcherExeNameX32;
	protected final String switcherExeNameX64;
	protected final String supportDirectoryX32;
	protected final String supportDirectoryX64;
	
	public GameDef(final String name, final String nameHandle, final String modFileEnding,
			final String[] coreModsOrDirectories, final String defaultRaceId, final String defaultConsoleSkinId,
			final String documentsGameDirectoryName, final String layoutFileEnding, final String baseDataFolderName,
			final String rootExeName, final String switcherExeNameX32, final String switcherExeNameX64,
			final String supportDirectoryX32, final String supportDirectoryX64,
			final String documentsInterfaceSubdirectoryName, final String modsSubDirectory,
			final String ptrRootExeName) {
		this.name = name;
		this.nameHandle = nameHandle;
		this.modFileEnding = modFileEnding;
		this.coreModsOrDirectories = coreModsOrDirectories;
		this.defaultRaceId = defaultRaceId;
		this.defaultConsoleSkinId = defaultConsoleSkinId;
		this.documentsGameDirectoryName = documentsGameDirectoryName;
		this.layoutFileEnding = layoutFileEnding;
		this.baseDataFolderName = baseDataFolderName;
		this.rootExeName = rootExeName;
		this.switcherExeNameX32 = switcherExeNameX32;
		this.switcherExeNameX64 = switcherExeNameX64;
		this.supportDirectoryX32 = supportDirectoryX32;
		this.supportDirectoryX64 = supportDirectoryX64;
		this.documentsInterfaceSubdirectoryName = documentsInterfaceSubdirectoryName;
		this.modsSubDirectory = modsSubDirectory;
		this.ptrRootExeName = ptrRootExeName;
	}
	
	/**
	 * @return the layoutFileEnding
	 */
	public String getLayoutFileEnding() {
		return layoutFileEnding;
	}
	
	/**
	 * @return the coreModsOrDirectories
	 */
	public String[] getCoreModsOrDirectories() {
		return Arrays.copyOf(coreModsOrDirectories, coreModsOrDirectories.length);
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
	 * @return
	 */
	public String getBaseDataFolderName() {
		return baseDataFolderName;
	}
	
	/**
	 * Returns the name of the game's rootExecutable.
	 *
	 * @return
	 */
	public String getRootExeName() {
		return rootExeName;
	}
	
	public String getPtrRootExeName() {
		return ptrRootExeName;
	}
	
	public String getSwitcherExeNameX32() {
		return switcherExeNameX32;
	}
	
	public String getSwitcherExeNameX64() {
		return switcherExeNameX64;
	}
	
	public String getSupportDirectoryX32() {
		return supportDirectoryX32;
	}
	
	public String getSupportDirectoryX64() {
		return supportDirectoryX64;
	}
	
	public String getDefaultConsoleSkinId() {
		return defaultConsoleSkinId;
	}
	
}
