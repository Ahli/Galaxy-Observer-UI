// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compile;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.ui.browse.BrowseTabController;

public class BrowseCompileTask extends CleaningForkJoinTask {
	
	private final ModData mod;
	private final BrowseTabController controller;
	private final CompileService compileService;
	private final BaseUiService baseUiService;
	private final ConfigService configService;
	
	public BrowseCompileTask(final ModData mod, final BrowseTabController controller,
			final CompileService compileService, final BaseUiService baseUiService, final ConfigService configService) {
		this.mod = mod;
		this.controller = controller;
		this.compileService = compileService;
		this.baseUiService = baseUiService;
		this.configService = configService;
	}
	
	@Override
	protected boolean work() {
		try {
			// TODO cache compiled uicatalogs
			baseUiService.parseBaseUI(mod.getGameData());
			final String raceId = configService.getRaceId();
			final String consoleSkinId = configService.getConsoleSkinId();
			final UICatalog uiCatalog = compileService.compile(mod, raceId, false, true, true, consoleSkinId);
			mod.setUi(uiCatalog);
			controller.setData(mod.getUi());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		
		return true;
	}
	
}
