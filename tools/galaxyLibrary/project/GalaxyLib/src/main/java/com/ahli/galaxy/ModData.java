package com.ahli.galaxy;

import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.interfaces.UICatalog;

import java.io.File;

/**
 * @author Ahli
 */
public class ModData {
	
	private GameData gameData;
	private File sourceDirectory;
	private File mpqCacheDirectory;
	private File targetFile;
	private DescIndexData descIndexData;
	private File componentListFile;
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
	public File getComponentListFile() {
		return componentListFile;
	}
	
	/**
	 * @param componentListFile
	 * 		the componentListFile to set
	 */
	public void setComponentListFile(final File componentListFile) {
		this.componentListFile = componentListFile;
	}
	
	/**
	 * @return the sourceDirectory
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}
	
	/**
	 * @param sourceDirectory
	 * 		the sourceDirectory to set
	 */
	public void setSourceDirectory(final File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}
	
	/**
	 * @return the mpqCacheDirectory
	 */
	public File getMpqCacheDirectory() {
		return mpqCacheDirectory;
	}
	
	/**
	 * @param mpqCacheDirectory
	 * 		the mpqCacheDirectory to set
	 */
	public void setMpqCacheDirectory(final File mpqCacheDirectory) {
		this.mpqCacheDirectory = mpqCacheDirectory;
	}
	
	/**
	 * @return the targetFile
	 */
	public File getTargetFile() {
		return targetFile;
	}
	
	/**
	 * @param targetFile
	 * 		the targetFile to set
	 */
	public void setTargetFile(final File targetFile) {
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
