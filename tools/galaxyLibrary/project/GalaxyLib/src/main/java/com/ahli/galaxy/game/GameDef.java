// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Ahli
 */
public record GameDef(String name, String nameHandle, String modFileEnding, String[] coreModsOrDirectories,
                      String defaultRaceId, String defaultConsoleSkinId, String documentsGameDirectoryName,
                      String layoutFileEnding, String baseDataFolderName, String rootExeName, String switcherExeNameX32,
                      String switcherExeNameX64, String supportDirectoryX32, String supportDirectoryX64,
                      String documentsInterfaceSubdirectoryName, String modsSubDirectory, String ptrRootExeName,
                      String componentsFileEnding, String styleFileEnding, String cutsceneFileEnding) {
	
	public static GameDef buildSc2GameDef() {
		return new GameDef(
				"StarCraft II",
				"sc2",
				"SC2Mod",
				new String[] { "core.sc2mod" },
				"Terr",
				"ClassicTerran",
				"StarCraft II",
				"SC2Layout",
				"base.sc2data",
				"StarCraft II.exe",
				"SC2Switcher.exe",
				"SC2Switcher_x64.exe",
				"Support",
				"Support64",
				"Interfaces",
				"mods",
				null,
				"SC2Components",
				"SC2Style",
				"SC2Cutscene");
	}
	
	public static GameDef buildHeroesGameDef() {
		return new GameDef(
				"Heroes of the Storm",
				"heroes",
				"stormmod",
				new String[] { "core.stormmod", "heroesdata.stormmod", "heromods" },
				"Terr",
				"ClassicTerran",
				"Heroes of the Storm",
				"stormlayout",
				"base.stormdata",
				"Heroes of the Storm.exe",
				null,
				"HeroesSwitcher_x64.exe",
				null,
				"Support64",
				"Interfaces",
				"mods",
				"Heroes of the Storm Public Test.exe",
				"stormcomponents",
				"stormstyle",
				"stormcutscene");
	}
	
	/**
	 * Checks if a GameDef is SC2.
	 *
	 * @param gameDef
	 * @return
	 */
	public static boolean isSc2(final GameDef gameDef) {
		return "sc2".equals(gameDef.nameHandle());
	}
	
	/**
	 * Checks if a GameDef is Heroes.
	 *
	 * @param gameDef
	 * @return
	 */
	public static boolean isHeroes(final GameDef gameDef) {
		return "heroes".equals(gameDef.nameHandle());
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final GameDef gameDef)) {
			return false;
		}
		return Objects.equals(name, gameDef.name) && Objects.equals(nameHandle, gameDef.nameHandle) &&
				Objects.equals(modFileEnding, gameDef.modFileEnding) &&
				Arrays.equals(coreModsOrDirectories, gameDef.coreModsOrDirectories) &&
				Objects.equals(defaultRaceId, gameDef.defaultRaceId) &&
				Objects.equals(defaultConsoleSkinId, gameDef.defaultConsoleSkinId) &&
				Objects.equals(documentsGameDirectoryName, gameDef.documentsGameDirectoryName) &&
				Objects.equals(layoutFileEnding, gameDef.layoutFileEnding) &&
				Objects.equals(baseDataFolderName, gameDef.baseDataFolderName) &&
				Objects.equals(rootExeName, gameDef.rootExeName) &&
				Objects.equals(switcherExeNameX32, gameDef.switcherExeNameX32) &&
				Objects.equals(switcherExeNameX64, gameDef.switcherExeNameX64) &&
				Objects.equals(supportDirectoryX32, gameDef.supportDirectoryX32) &&
				Objects.equals(supportDirectoryX64, gameDef.supportDirectoryX64) &&
				Objects.equals(documentsInterfaceSubdirectoryName, gameDef.documentsInterfaceSubdirectoryName) &&
				Objects.equals(modsSubDirectory, gameDef.modsSubDirectory) &&
				Objects.equals(ptrRootExeName, gameDef.ptrRootExeName) &&
				Objects.equals(componentsFileEnding, gameDef.componentsFileEnding) &&
				Objects.equals(styleFileEnding, gameDef.styleFileEnding) &&
				Objects.equals(cutsceneFileEnding, gameDef.cutsceneFileEnding);
	}
	
	@Override
	public final int hashCode() {
		int result = Objects.hash(
				name,
				nameHandle,
				modFileEnding,
				defaultRaceId,
				defaultConsoleSkinId,
				documentsGameDirectoryName,
				layoutFileEnding,
				baseDataFolderName,
				rootExeName,
				switcherExeNameX32,
				switcherExeNameX64,
				supportDirectoryX32,
				supportDirectoryX64,
				documentsInterfaceSubdirectoryName,
				modsSubDirectory,
				ptrRootExeName,
				componentsFileEnding,
				styleFileEnding,
				cutsceneFileEnding);
		result = 31 * result + Arrays.hashCode(coreModsOrDirectories);
		return result;
	}
}
