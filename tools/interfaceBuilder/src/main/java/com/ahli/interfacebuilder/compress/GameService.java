// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.compress;

import com.ahli.galaxy.ModD;
import com.ahli.galaxy.game.Game;
import com.ahli.galaxy.game.GameDef;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import com.ahli.interfacebuilder.projects.enums.GameType;
import lombok.Getter;

public class GameService {
	private final ConfigService configService;
	@Getter
	private final GameDef gameDefSc2;
	@Getter
	private final GameDef gameDefHeroes;
	
	public GameService(final ConfigService configService) {
		this.configService = configService;
		gameDefSc2 = GameDef.buildSc2GameDef();
		gameDefHeroes = GameDef.buildHeroesGameDef();
	}
	
	/**
	 * Returns a ModData instance containing the specified game definition.
	 *
	 * @param gameType
	 * @return
	 */
	public ModD getModData(final GameType gameType) {
		return new ModD(new Game(getGameDef(gameType)));
	}
	
	/**
	 * Returns a GameDef instance containing the specified game definition.
	 *
	 * @param gameType
	 * @return
	 */
	public GameDef getGameDef(final GameType gameType) {
		return switch (gameType) {
			case SC2 -> gameDefSc2;
			case HEROES -> gameDefHeroes;
		};
	}
	
	/**
	 * Returns the path of the image that reflects the specified game.
	 *
	 * @param gameType
	 * @return
	 */
	public String getGameItemPath(final GameType gameType) {
		return switch (gameType) {
			case SC2 -> "classpath:res/sc2.png";
			case HEROES -> "classpath:res/heroes.png";
		};
	}
	
	/**
	 * Returns the Game Directory of a specified game Def.
	 *
	 * @param gameDef
	 * @param isPtr
	 * @return Path to the game's directory
	 */
	public String getGameDirPath(final GameDef gameDef, final boolean isPtr) {
		final SettingsIniInterface iniSettings = configService.getIniSettings();
		
		if (GameDef.isSc2(gameDef)) {
			return iniSettings.getSc2Path();
		}
		if (GameDef.isHeroes(gameDef)) {
			return isPtr ? iniSettings.getHeroesPtrPath() : iniSettings.getHeroesPath();
		}
		return null;
	}
}
