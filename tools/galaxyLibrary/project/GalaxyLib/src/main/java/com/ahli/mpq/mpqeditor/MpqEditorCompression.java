package com.ahli.mpq.mpqeditor;

/**
 * Compression mode for the Mpq Editor.
 */
public enum MpqEditorCompression {/**
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
	SYSTEM_DEFAULT}
