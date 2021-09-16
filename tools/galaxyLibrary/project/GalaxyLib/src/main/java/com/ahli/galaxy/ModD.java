// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy;

import com.ahli.galaxy.archive.DescIndex;
import com.ahli.galaxy.game.Game;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import lombok.Data;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Ahli
 */
@Data
public class ModD {
	
	private Game game;
	private Path sourceDirectory;
	private Path mpqCacheDirectory;
	private Path targetFile;
	private DescIndex descIndex;
	private Path componentListFile;
	private UICatalog uiCatalog;
	
	/**
	 * @param game
	 * 		GameData of this Mod
	 */
	public ModD(final Game game) {
		this.game = game;
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final ModD modD)) {
			return false;
		}
		return Objects.equals(game, modD.game) && Objects.equals(sourceDirectory, modD.sourceDirectory) &&
				Objects.equals(mpqCacheDirectory, modD.mpqCacheDirectory) &&
				Objects.equals(targetFile, modD.targetFile) && Objects.equals(descIndex, modD.descIndex) &&
				Objects.equals(componentListFile, modD.componentListFile) && Objects.equals(uiCatalog, modD.uiCatalog);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(game,
				sourceDirectory,
				mpqCacheDirectory,
				targetFile,
				descIndex,
				componentListFile,
				uiCatalog);
	}
}
