// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game.def;

import com.ahli.galaxy.game.def.abstracts.GameDef;

/**
 * @author Ahli
 */
public class SC2GameDef extends GameDef {
	
	/**
	 *
	 */
	public SC2GameDef() {
		super();
		name = "StarCraft II";
		nameHandle = "sc2";
		modFileEnding = "SC2Mod";
		coreModsOrDirectories = new String[] { "core.sc2mod" };
		defaultRaceId = "Terr";
		defaultConsoleSkinId = "ClassicTerran";
		documentsGameDirectoryName = "StarCraft II";
		layoutFileEnding = "SC2Layout";
		baseDataFolderName = "base.sc2data";
		rootExeName = "StarCraft II.exe";
		switcherExeNameX32 = "SC2Switcher.exe";
		switcherExeNameX64 = "SC2Switcher_x64.exe";
		supportDirectoryX32 = "Support";
		supportDirectoryX64 = "Support64";
	}
}
