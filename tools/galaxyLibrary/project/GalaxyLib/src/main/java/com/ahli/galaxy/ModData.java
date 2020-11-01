// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy;

import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.interfaces.UICatalog;

import java.nio.file.Path;

/**
 * @author Ahli
 */
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
	
	/**
	 * @return the descIndexData
	 */
	public DescIndexData getDescIndexData() {
		return descIndexData;
	}
	
	/**
	 * @param descIndexData
	 * 		the descIndexData to set
	 */
	public void setDescIndexData(final DescIndexData descIndexData) {
		this.descIndexData = descIndexData;
	}
	
	/**
	 * @return the gameData
	 */
	public GameData getGameData() {
		return gameData;
	}
	
	/**
	 * @param gameData
	 * 		the gameData to set
	 */
	public void setGameData(final GameData gameData) {
		this.gameData = gameData;
	}
	
	/**
	 * @return the componentListFile
	 */
	public Path getComponentListFile() {
		return componentListFile;
	}
	
	/**
	 * @param componentListFile
	 * 		the componentListFile to set
	 */
	public void setComponentListFile(final Path componentListFile) {
		this.componentListFile = componentListFile;
	}
	
	/**
	 * @return the sourceDirectory
	 */
	public Path getSourceDirectory() {
		return sourceDirectory;
	}
	
	/**
	 * @param sourceDirectory
	 * 		the sourceDirectory to set
	 */
	public void setSourceDirectory(final Path sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}
	
	/**
	 * @return the mpqCacheDirectory
	 */
	public Path getMpqCacheDirectory() {
		return mpqCacheDirectory;
	}
	
	/**
	 * @param mpqCacheDirectory
	 * 		the mpqCacheDirectory to set
	 */
	public void setMpqCacheDirectory(final Path mpqCacheDirectory) {
		this.mpqCacheDirectory = mpqCacheDirectory;
	}
	
	/**
	 * @return the targetFile
	 */
	public Path getTargetFile() {
		return targetFile;
	}
	
	/**
	 * @param targetFile
	 * 		the targetFile to set
	 */
	public void setTargetFile(final Path targetFile) {
		this.targetFile = targetFile;
	}
	
	/**
	 * @return
	 */
	public UICatalog getUi() {
		return uiCatalog;
	}
	
	/**
	 * @param uiCatalog
	 */
	public void setUi(final UICatalog uiCatalog) {
		this.uiCatalog = uiCatalog;
	}
	
}
