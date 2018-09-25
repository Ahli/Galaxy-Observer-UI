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
		setName("StarCraft II");
		setNameHandle("sc2");
		setModFileEnding("SC2Mod");
		setCoreModsOrDirectories("Core.SC2Mod");
		setDefaultRaceId("Terr");
		setDocumentsGameDirectoryName("StarCraft II");
		setLayoutFileEnding("SC2Layout");
		setBaseDataFolderName("Base.SC2Data");
		setRootExeName("StarCraft II.exe");
		setSwitcherExeNameX32("SC2Switcher.exe");
		setSwitcherExeNameX64("SC2Switcher_x64.exe");
		setSupportDirectoryX32("Support");
		setSupportDirectoryX64("Support64");
	}
}
