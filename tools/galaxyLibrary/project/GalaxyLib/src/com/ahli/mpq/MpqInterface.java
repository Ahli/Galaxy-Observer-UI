package com.ahli.mpq;

import java.io.File;

/**
 * Interface for the interaction of MPQ files.
 *
 * @author Ahli
 */
public interface MpqInterface {
	
	/**
	 * Returns a File from a mpq with the specified internal path.
	 *
	 * @param intPath
	 * @return File from the mpq with specified internal Path
	 */
	File getFileFromMpq(String internalPath);
	
	/**
	 * @return
	 * @throws MpqException
	 */
	boolean isHeroesMpq() throws MpqException;
	
	/**
	 * Returns the cache folder of the opened mpq.
	 *
	 * @return cache folder as File
	 */
	File getCache();
	
	/**
	 * Sets the cache folder.
	 *
	 * @param cache
	 *         Cache folder to temporarily store mpq content within
	 */
	void setCache(File cache);
	
}
