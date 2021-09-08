// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serial;

public class BrowseLoadBaseUiTask extends CleaningForkJoinTask {
	private static final Logger logger = LogManager.getLogger(BrowseLoadBaseUiTask.class);
	
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
		try {
			baseUiService.parseBaseUI(gameData);
		} catch (final Exception e) {
			logger.error("Error while parsing Base UI.", e);
			return false;
		}
		controller.setData(gameData.getUiCatalog());
		return true;
	}
	
}
