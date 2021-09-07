// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;

import java.io.Serial;

public class BrowseLoadBaseUiTask extends CleaningForkJoinTask {
	
	@Serial
	private static final long serialVersionUID = -2905917491106111107L;
	
	private final transient GameData gameData;
	private final transient BrowseTabController controller;
	private final transient BaseUiService baseUiService;
	
	public BrowseLoadBaseUiTask(
			final CleaningForkJoinTaskCleaner cleaner,
			final GameData gameData,
			final BrowseTabController controller,
			final BaseUiService baseUiService) {
		super(cleaner);
		this.gameData = gameData;
		this.controller = controller;
		this.baseUiService = baseUiService;
	}
	
	@Override
	protected boolean work() {
		baseUiService.parseBaseUI(gameData);
		controller.setData(gameData.getUiCatalog());
		return true;
	}
	
}
