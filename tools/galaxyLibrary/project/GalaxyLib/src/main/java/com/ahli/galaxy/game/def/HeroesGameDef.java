// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game.def;

import com.ahli.galaxy.game.def.abstracts.GameDef;

/**
 * @author Ahli
 */
public class HeroesGameDef extends GameDef {
	
	/**
	 *
	 */
	public HeroesGameDef() {
		name = "Heroes of the Storm";
		nameHandle = "heroes";
		modFileEnding = "stormmod";
		coreModsOrDirectories = new String[] { "core.stormmod", "heroesdata.stormmod", "heromods" };
		defaultRaceId = "Terr";
		defaultConsoleSkinId = "ClassicTerran";
		documentsGameDirectoryName = "Heroes of the Storm";
		layoutFileEnding = "stormlayout";
		baseDataFolderName = "base.stormdata";
		rootExeName = "Heroes of the Storm.exe";
		ptrRootExeName = "Heroes of the Storm Public Test.exe";
		switcherExeNameX32 = "HeroesSwitcher.exe";
		switcherExeNameX64 = "HeroesSwitcher_x64.exe";
		supportDirectoryX32 = "Support";
		supportDirectoryX64 = "Support64";
	}
	
}
