// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compile;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.threads.CleaningForkJoinTask;
import interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import interfacebuilder.ui.browse.BrowseTabController;

import java.io.Serial;

public class BrowseCompileTask extends CleaningForkJoinTask {
	
	@Serial
	private static final long serialVersionUID = 5971938125363486608L;
	
	private final transient ModData mod;
	private final transient BrowseTabController controller;
	private final transient CompileService compileService;
	private final transient BaseUiService baseUiService;
	private final transient ConfigService configService;
	
	public BrowseCompileTask(
			final CleaningForkJoinTaskCleaner cleaner,
			final ModData mod,
			final BrowseTabController controller,
			final CompileService compileService,
			final BaseUiService baseUiService,
			final ConfigService configService) {
		super(cleaner);
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
			mod.setUiCatalog(uiCatalog);
			controller.setData(mod.getUiCatalog());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return true;
	}
	
}
