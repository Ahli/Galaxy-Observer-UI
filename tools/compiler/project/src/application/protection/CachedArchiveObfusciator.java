package application.protection;

import java.io.File;
import java.util.ArrayList;

public class CachedArchiveObfusciator {

	String[] protectedNames = { "(listfile)", "(attributes)", "ComponentList.StormComponents", "DocumentHeader",
			"DocumentInfo", "ObserverVariables.txt", "PreloadAssetDB.xml",
			"enUS.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"frFR.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"deDE.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"esES.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"esMX.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"ruRU.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"koKR.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"zhCN.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"zhTW.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"plPL.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"itIT.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt",
			"ptBR.StormData" + File.separator + "LocalizedData" + File.separator + "GameStrings.txt" };
	String[] protectedNamePrefixes = { "Base.StormData" + File.separator + "Assets" + File.separator + "Textures" };

	ArrayList<String> fileNames = new ArrayList<>();
	
}
