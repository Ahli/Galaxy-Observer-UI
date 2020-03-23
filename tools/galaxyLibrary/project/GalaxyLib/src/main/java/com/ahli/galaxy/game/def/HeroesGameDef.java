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
		super("Heroes of the Storm", "heroes", "stormmod",
				new String[] { "core.stormmod", "heroesdata.stormmod", "heromods" }, "Terr", "ClassicTerran",
				"Heroes of the Storm", "stormlayout", "Heroes of the Storm", "base.stormdata",
				"Heroes of the Storm.exe", "HeroesSwitcher.exe", "HeroesSwitcher_x64.exe", "Support", "Support64",
				"mods", "Heroes of the Storm Public Test.exe");
	}
	
}
