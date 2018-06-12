package application.config;

import application.integration.SettingsIniInterface;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ConfigService {
	
	@Autowired
	@Qualifier ("mpqCachePath")
	private String mpqCachePath;
	
	@Autowired
	@Qualifier ("basePath")
	private File basePath;
	
	@Autowired
	@Qualifier ("documentsPath")
	private String documentsPath;
	
	@Autowired
	@Qualifier ("mpqEditorPath")
	private String mpqEditorPath;
	
	@Autowired
	private SettingsIniInterface iniSettings;
	
	@Autowired
	@Qualifier ("raceId")
	private String raceId;
	
	@Autowired
	@Qualifier ("baseUiPath")
	private String baseUiPath;
	
	@Autowired
	@Qualifier ("cascExtractorConfigFile")
	private File cascExtractorConfigFile;
	
	@Autowired
	@Qualifier ("cascExtractorConsoleExeFile")
	private File cascExtractorConsoleExeFile;
	
	@Autowired
	@Qualifier ("cachePath")
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
	
	public File getCascExtractorConfigFile() {
		return cascExtractorConfigFile;
	}
	
	public File getCascExtractorConsoleExeFile() {
		return cascExtractorConsoleExeFile;
	}
	
	public String getCachePath() {
		return cachePath;
	}
}
