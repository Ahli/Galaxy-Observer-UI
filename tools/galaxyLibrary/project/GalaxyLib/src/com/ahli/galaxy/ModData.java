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
	private File sourcePath = null;
	private File mpqCachePath = null;
	private File targetPath = null;
	private DescIndexData descIndexData = null;
	private File componentListFile = null;
	private UICatalog catalog = null;
	
	/**
	 * Constructor.
	 *
	 * @param game
	 *         GameData of this Mod
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
	 *         the descIndexData to set
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
	 *         the gameData to set
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
	 *         the componentListFile to set
	 */
	public void setComponentListFile(final File componentListFile) {
		this.componentListFile = componentListFile;
	}
	
	/**
	 * @return the sourcePath
	 */
	public File getSourcePath() {
		return sourcePath;
	}
	
	/**
	 * @param sourcePath
	 *         the sourcePath to set
	 */
	public void setSourcePath(final File sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	/**
	 * @return the mpqCachePath
	 */
	public File getCachePath() {
		return mpqCachePath;
	}
	
	/**
	 * @param mpqCachePath
	 *         the mpqCachePath to set
	 */
	public void setMpqCachePath(final File mpqCachePath) {
		this.mpqCachePath = mpqCachePath;
	}
	
	/**
	 * @return the targetPath
	 */
	public File getTargetPath() {
		return targetPath;
	}
	
	/**
	 * @param targetPath
	 *         the targetPath to set
	 */
	public void setTargetPath(final File targetPath) {
		this.targetPath = targetPath;
	}
	
	/**
	 * @return
	 */
	public UICatalog getUi() {
		return catalog;
	}
	
	/**
	 * @param catalog
	 */
	public void setUi(final UICatalog catalog) {
		this.catalog = catalog;
	}
}
