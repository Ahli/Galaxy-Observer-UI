package interfacebuilder.compress;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.game.def.HeroesGameDef;
import com.ahli.galaxy.game.def.SC2GameDef;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import interfacebuilder.projects.enums.Game;

public class GameService {
	
	/**
	 * Returns a ModData instance containing the specified game definition.
	 *
	 * @param game
	 * @return
	 */
	public ModData getModData(final Game game) {
		return new ModData(new GameData(getGameDef(game)));
	}
	
	/**
	 * Returns a GameDef instance containing the specified game definition.
	 *
	 * @param game
	 * @return
	 */
	public GameDef getGameDef(final Game game) {
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
		return gameDef;
	}
	
	
	/**
	 * Returns the path of the image that reflects the specified game.
	 *
	 * @param game
	 * @return
	 */
	public String getGameItemPath(final Game game) {
		switch (game) {
			case SC2:
				return "res/sc2.png";
			case HEROES:
				return "res/heroes.png";
			default:
				return "res/ahli.png";
		}
	}
}
