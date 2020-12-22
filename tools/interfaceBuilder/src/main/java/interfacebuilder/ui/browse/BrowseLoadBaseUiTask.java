// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;

public class BrowseLoadBaseUiTask extends CleaningForkJoinTask {
	
	private final GameData gameData;
	private final BrowseTabController controller;
	private final BaseUiService baseUiService;
	
	public BrowseLoadBaseUiTask(final CleaningForkJoinTaskCleaner cleaner, final GameData gameData,
			final BrowseTabController controller, final BaseUiService baseUiService) {
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
