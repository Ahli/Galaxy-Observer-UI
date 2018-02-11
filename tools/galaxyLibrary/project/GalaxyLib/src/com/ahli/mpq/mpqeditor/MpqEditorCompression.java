package com.ahli.mpq.mpqeditor;

/**
 * Compression mode for the Mpq Editor.
 */
public enum MpqEditorCompression {
	/**
	 *
	 */
	NONE,
	
	/**
	 * Blizzard's default compression for StarCraft II and Heroes of the Storm.
	 */
	BLIZZARD_SC2_HEROES,
	
	/**
	 * Custom compression ruleset.
	 */
	CUSTOM
}
