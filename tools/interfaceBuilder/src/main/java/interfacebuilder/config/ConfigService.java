// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import com.ahli.galaxy.game.GameDef;
import interfacebuilder.integration.SettingsIniInterface;

import java.io.File;
import java.nio.file.Path;

public class ConfigService {
	
	private final Path mpqCachePath;
	private final Path basePath;
	private final Path documentsPath;
	private final Path mpqEditorPath;
	private final SettingsIniInterface iniSettings;
	private final String raceId;
	private final String consoleSkinId;
	private final Path baseUiPath;
	private final File cascExtractorExeFile;
	private final Path cachePath;
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
	
	public Path getMpqEditorPath() {
		return mpqEditorPath;
	}
	
	public Path getMpqCachePath() {
		return mpqCachePath;
	}
	
	public Path getBasePath() {
		return basePath;
	}
	
	public Path getDocumentsPath() {
		return documentsPath;
	}
	
	public SettingsIniInterface getIniSettings() {
		return iniSettings;
	}
	
	public String getRaceId() {
		return raceId;
	}
	
	public Path getBaseUiPath(final GameDef gameDef) {
		return baseUiPath.resolve(gameDef.nameHandle());
	}
	
	public File getCascExtractorExeFile() {
		return cascExtractorExeFile;
	}
	
	public Path getCachePath() {
		return cachePath;
	}
	
	public String getConsoleSkinId() {
		return consoleSkinId;
	}
	
	public Path getMiningTempPath() {
		return miningTempPath;
	}
}
