package com.ahli.galaxy.game;

import java.util.HashMap;
import java.util.Map;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.galaxy.ui.UICatalog;

/**
 * Class containing the data of a game (Sc2/Heroes/...).
 * 
 * @author Ahli
 */
public class GameData {
	
	private GameDef gameDef;
	private UICatalog uiCatalog = new UICatalog();
	private Map<Object, Object> keyValueStore = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param gameDef
	 *            game described by the data
	 */
	public GameData(final GameDef gameDef) {
		this.gameDef = gameDef;
	}
	
	/**
	 * @return the uiCatalog
	 */
	public UICatalog getUiCatalog() {
		return uiCatalog;
	}
	
	/**
	 * @param uiCatalog
	 *            the uiCatalog to set
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
	 *            the gameDef to set
	 */
	public void setGameDef(final GameDef gameDef) {
		this.gameDef = gameDef;
	}
	
	/**
	 * @return
	 */
	public Map<Object, Object> getKeyValueStore() {
		return keyValueStore;
	}
	
	/**
	 * @param keyValueStore
	 */
	public void setKeyValueStore(final Map<Object, Object> keyValueStore) {
		this.keyValueStore = keyValueStore;
	}
	
}
