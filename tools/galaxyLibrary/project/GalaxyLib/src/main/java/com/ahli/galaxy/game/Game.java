// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game;

import com.ahli.galaxy.ui.interfaces.UICatalog;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class containing the data of a game (Sc2/Heroes/...).
 *
 * @author Ahli
 */
@Data
public class Game {
	@NotNull
	private final GameDef gameDef;
	@Nullable
	private UICatalog uiCatalog;
	
	public Game(@NotNull final GameDef gameDef) {
		this.gameDef = gameDef;
	}
	
}
