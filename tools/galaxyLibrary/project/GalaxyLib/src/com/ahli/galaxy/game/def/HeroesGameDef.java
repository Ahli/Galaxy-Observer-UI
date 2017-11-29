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
		setCoreModsOrDirectories(new String[] { "core.stormmod", "heroesdata.stormmod", "heromods" });
		setDefaultRaceId("Terr");
		setDocumentsGameDirectoryName("Heroes of the Storm");
		setLayoutFileEnding("stormlayout");
		setBaseDataFolderName("Base.StormData");
	}
}
