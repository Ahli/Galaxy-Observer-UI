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
		super("StarCraft II", "sc2", "SC2Mod", new String[] { "core.sc2mod" }, "Terr", "ClassicTerran", "StarCraft II",
				"SC2Layout", "base.sc2data", "StarCraft II.exe", "SC2Switcher.exe", "SC2Switcher_x64.exe", "Support",
				"Support64", "Interfaces", "mods", null);
	}
}
