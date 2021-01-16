// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy;

import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import lombok.Data;

import java.nio.file.Path;
import java.util.Objects;

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
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModData)) {
			return false;
		}
		final ModData modData = (ModData) o;
		return Objects.equals(gameData, modData.gameData) && Objects.equals(sourceDirectory, modData.sourceDirectory) &&
				Objects.equals(mpqCacheDirectory, modData.mpqCacheDirectory) &&
				Objects.equals(targetFile, modData.targetFile) &&
				Objects.equals(descIndexData, modData.descIndexData) &&
				Objects.equals(componentListFile, modData.componentListFile) &&
				Objects.equals(uiCatalog, modData.uiCatalog);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(gameData,
				sourceDirectory,
				mpqCacheDirectory,
				targetFile,
				descIndexData,
				componentListFile,
				uiCatalog);
	}
}
