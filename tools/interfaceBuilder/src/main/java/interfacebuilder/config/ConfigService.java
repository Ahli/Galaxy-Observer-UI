// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import interfacebuilder.integration.SettingsIniInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.nio.file.Path;

public class ConfigService {
	
	@Autowired
	@Qualifier("mpqCachePath")
	private Path mpqCachePath;
	
	@Autowired
	@Qualifier("basePath")
	private Path basePath;
	
	@Autowired
	@Qualifier("documentsPath")
	private Path documentsPath;
	
	@Autowired
	@Qualifier("mpqEditorPath")
	private Path mpqEditorPath;
	
	@Autowired
	private SettingsIniInterface iniSettings;
	
	@Autowired
	@Qualifier("raceId")
	private String raceId;
	
	@Autowired
	@Qualifier("consoleSkinId")
	private String consoleSkinId;
	
	@Autowired
	@Qualifier("baseUiPath")
	private Path baseUiPath;
	
	@Autowired
	@Qualifier("cascExtractorExeFile")
	private File cascExtractorExeFile;
	
	@Autowired
	@Qualifier("cachePath")
	private Path cachePath;
	
	@Autowired
	@Qualifier("miningTempPath")
	private Path miningTempPath;
	
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
		return baseUiPath.resolve(gameDef.getNameHandle());
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
