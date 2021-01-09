// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy;

import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import lombok.Data;

import java.nio.file.Path;

/**
 * @author Ahli
 */
@Data
public class ModData {
	
	private GameData gameData;
	private Path sourceDirectory;
	private Path mpqCacheDirectory;
	private Path targetFile;
	private DescIndexData descIndexData;
	private Path componentListFile;
	private UICatalog uiCatalog;
	
	/**
	 * Constructor.
	 *
	 * @param game
	 * 		GameData of this Mod
	 */
	public ModData(final GameData game) {
		gameData = game;
	}
}
