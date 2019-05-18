// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.interfaces.UICatalog;

/**
 * Class containing the data of a game (Sc2/Heroes/...).
 *
 * @author Ahli
 */
public class GameData {
	
	private GameDef gameDef;
	private UICatalog uiCatalog;
	
	/**
	 * Constructor.
	 *
	 * @param gameDef
	 * 		game described by the data
	 */
	public GameData(final GameDef gameDef) {
		this.gameDef = gameDef;
		uiCatalog = null;
	}
	
	/**
	 * @return the uiCatalog
	 */
	public UICatalog getUiCatalog() {
		return uiCatalog;
	}
	
	/**
	 * @param uiCatalog
	 * 		the uiCatalog to set
	 */
	public void setUiCatalog(final UICatalog uiCatalog) {
		this.uiCatalog = uiCatalog;
	}
	
	/**
	 * @return the gameDef
	 */
	public GameDef getGameDef() {
		return gameDef;
	}
	
	/**
	 * @param gameDef
	 * 		the gameDef to set
	 */
	public void setGameDef(final GameDef gameDef) {
		this.gameDef = gameDef;
	}
	
}
