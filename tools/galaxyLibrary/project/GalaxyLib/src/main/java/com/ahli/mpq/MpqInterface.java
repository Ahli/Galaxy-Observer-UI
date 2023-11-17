// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import java.nio.file.Path;

/**
 * Interface for the interaction of MPQ files.
 *
 * @author Ahli
 */
public interface MpqInterface {
	
	/**
	 * Returns a File from a mpq with the specified internal path.
	 *
	 * @param internalPath
	 * @return File from the mpq with specified internal Path
	 */
	Path getFilePathFromMpq(String internalPath);
	
	/**
	 * @return
	 * @throws MpqException
	 */
	boolean isHeroesMpq() throws MpqException;
	
	/**
	 * Returns the cache folder of the opened mpq.
	 *
	 * @return cache folder as a Path
	 */
	Path getCache();
	
//	/**
//	 * Sets the cache folder.
//	 *
//	 * @param cache
//	 * 		Cache folder to temporarily store mpq content within
//	 */
//	void setCache(Path cache);
	
}
