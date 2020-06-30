// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import interfacebuilder.integration.SettingsIniInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;

public class ConfigService {
	
	@Autowired
	@Qualifier("mpqCachePath")
	private String mpqCachePath;
	
	@Autowired
	@Qualifier("basePath")
	private File basePath;
	
	@Autowired
	@Qualifier("documentsPath")
	private String documentsPath;
	
	@Autowired
	@Qualifier("mpqEditorPath")
	private String mpqEditorPath;
	
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
	private String baseUiPath;
	
	@Autowired
	@Qualifier("cascExtractorExeFile")
	private File cascExtractorExeFile;
	
	@Autowired
	@Qualifier("cachePath")
	private String cachePath;
	
	public String getMpqEditorPath() {
		return mpqEditorPath;
	}
	
	public String getMpqCachePath() {
		return mpqCachePath;
	}
	
	public File getBasePath() {
		return basePath;
	}
	
	public String getDocumentsPath() {
		return documentsPath;
	}
	
	public SettingsIniInterface getIniSettings() {
		return iniSettings;
	}
	
	public String getRaceId() {
		return raceId;
	}
	
	public String getBaseUiPath(final GameDef gameDef) {
		return baseUiPath + File.separator + gameDef.getNameHandle();
	}
	
	public File getCascExtractorExeFile() {
		return cascExtractorExeFile;
	}
	
	public String getCachePath() {
		return cachePath;
	}
	
	public String getConsoleSkinId() {
		return consoleSkinId;
	}
}
