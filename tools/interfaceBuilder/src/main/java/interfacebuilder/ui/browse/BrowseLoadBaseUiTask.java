package interfacebuilder.ui.browse;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.ui.browse.BrowseTabController;

public class BrowseLoadBaseUiTask extends CleaningForkJoinTask<Object> {
	
	private final GameData gameData;
	private final BrowseTabController controller;
	private final BaseUiService baseUiService;
	
	public BrowseLoadBaseUiTask(final GameData gameData, final BrowseTabController controller,
			final BaseUiService baseUiService) {
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
