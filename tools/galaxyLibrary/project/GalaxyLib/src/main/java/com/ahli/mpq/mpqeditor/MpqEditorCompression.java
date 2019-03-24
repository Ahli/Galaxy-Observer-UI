// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

/**
 * Compression mode for the Mpq Editor.
 */
public enum MpqEditorCompression {
	
	/**
	 * Files will not be compressed.
	 */
	NONE,
	
	/**
	 * Blizzard's default compression for StarCraft II and Heroes of the Storm.
	 */
	BLIZZARD_SC2_HEROES,
	
	/**
	 * Custom compression ruleset.
	 */
	CUSTOM,
	
	/**
	 * Uses the existing settings files.
	 */
	SYSTEM_DEFAULT
}
