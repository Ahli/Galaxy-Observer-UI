// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.SettingsIniInterface;
import interfacebuilder.projects.enums.Game;
import org.springframework.beans.factory.annotation.Autowired;

public class GameService {
	@Autowired
	private ConfigService configService;
	
	
	/**
	 * Returns a ModData instance containing the specified game definition.
	 *
	 * @param game
	 * @return
	 */
	public ModData getModData(final Game game) {
		return new ModData(new GameData(getNewGameDef(game)));
	}
	
	/**
	 * Returns a GameDef instance containing the specified game definition.
	 *
	 * @param game
	 * @return
	 */
	public GameDef getNewGameDef(final Game game) {
		return switch (game) {
			case SC2 -> new SC2GameDef();
			case HEROES -> new HeroesGameDef();
		};
	}
	
	
	/**
	 * Returns the path of the image that reflects the specified game.
	 *
	 * @param game
	 * @return
	 */
	public String getGameItemPath(final Game game) {
		return switch (game) {
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
		
		if (gameDef instanceof SC2GameDef) {
			return iniSettings.getSc2Path();
		}
		if (gameDef instanceof HeroesGameDef) {
			return isPtr ? iniSettings.getHeroesPtrPath() : iniSettings.getHeroesPath();
		}
		return null;
	}
}
