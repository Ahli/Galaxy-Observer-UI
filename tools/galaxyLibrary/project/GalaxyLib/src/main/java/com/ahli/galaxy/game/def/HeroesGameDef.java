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
		setName("Heroes of the Storm");
		setNameHandle("heroes");
		setModFileEnding("stormmod");
		setCoreModsOrDirectories("core.stormmod", "heroesdata.stormmod", "heromods");
		setDefaultRaceId("Terr");
		setDocumentsGameDirectoryName("Heroes of the Storm");
		setLayoutFileEnding("stormlayout");
		setBaseDataFolderName("base.stormdata");
		setRootExeName("Heroes of the Storm.exe");
		setPtrRootExeName("Heroes of the Storm Public Test.exe");
		setSwitcherExeNameX32("HeroesSwitcher.exe");
		setSwitcherExeNameX64("HeroesSwitcher_x64.exe");
		setSupportDirectoryX32("Support");
		setSupportDirectoryX64("Support64");
	}
	
}
