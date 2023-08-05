// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.config;

import com.ahli.galaxy.game.GameDef;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;

public class ConfigService {
	
	@Getter
	private final Path mpqCachePath;
	@Getter
	private final Path basePath;
	@Getter
	private final Path documentsPath;
	@Getter
	private final Path mpqEditorPath;
	@Getter
	private final SettingsIniInterface iniSettings;
	@Getter
	private final String raceId;
	@Getter
	private final String consoleSkinId;
	private final Path baseUiPath;
	@Getter
	private final File cascExtractorExeFile;
	@Getter
	private final Path cachePath;
	@Getter
	private final Path miningTempPath;
	
	public ConfigService(
			final Path mpqCachePath,
			final Path basePath,
			final Path documentsPath,
			final Path mpqEditorPath,
			final SettingsIniInterface iniSettings,
			final String raceId,
			final String consoleSkinId,
			final Path baseUiPath,
			final File cascExtractorExeFile,
			final Path cachePath,
			final Path miningTempPath) {
		this.mpqCachePath = mpqCachePath;
		this.basePath = basePath;
		this.documentsPath = documentsPath;
		this.mpqEditorPath = mpqEditorPath;
		this.iniSettings = iniSettings;
		this.raceId = raceId;
		this.consoleSkinId = consoleSkinId;
		this.baseUiPath = baseUiPath;
		this.cascExtractorExeFile = cascExtractorExeFile;
		this.cachePath = cachePath;
		this.miningTempPath = miningTempPath;
	}
	
	public Path getBaseUiPath(final GameDef gameDef) {
		return baseUiPath.resolve(gameDef.nameHandle());
	}
	
}
