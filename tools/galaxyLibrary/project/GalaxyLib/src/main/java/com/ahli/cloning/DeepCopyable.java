// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.cloning;

public interface DeepCopyable {
	/**
	 * Returns a deep copy of the Object.
	 *
	 * @return
	 */
	Object deepCopy();
}
