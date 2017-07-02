package com.ahli.mpq;

import java.io.File;

import com.ahli.mpq.MpqException;

/**
 * 
 * @author Ahli
 *
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
	 * 
	 * @return
	 * @throws MpqException
	 */
	boolean isHeroesMpq() throws MpqException;

	/**
	 * Returns the cache folder of the opened mpq.
	 * 
	 * @return
	 */
	File getCache();

	/**
	 * Sets the cache folder.
	 * @param cache
	 */
	void setCache(File cache);

}
