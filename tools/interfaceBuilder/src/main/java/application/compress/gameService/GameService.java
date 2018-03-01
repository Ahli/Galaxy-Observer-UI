package application.compress.gameService;

import application.projects.enums.Game;
import com.ahli.galaxy.ModData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;

public class GameService {
	
	/**
	 * Returns a ModData instance containing the specified game definition.
	 *
	 * @param game
	 * @return
	 */
	public ModData getModData(final Game game) {
		final GameDef gameDef;
		switch (game) {
			case SC2:
				gameDef = new SC2GameDef();
				break;
			case HEROES:
				gameDef = new HeroesGameDef();
				break;
			default:
				throw new IllegalArgumentException("Unknown game value " + game);
		}
		return new ModData(new GameData(gameDef));
	}
}
