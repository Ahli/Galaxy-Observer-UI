// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.compile;

import com.ahli.galaxy.ModD;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTask;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTaskCleaner;
import com.ahli.interfacebuilder.ui.browse.BrowseTabController;
import lombok.extern.log4j.Log4j2;

import java.io.Serial;

@Log4j2
public class BrowseCompileTask extends CleaningForkJoinTask {
	
	@Serial
	private static final long serialVersionUID = 5971938125363486608L;
	
	private final transient ModD mod;
	private final transient BrowseTabController controller;
	private final transient CompileService compileService;
	private final transient BaseUiService baseUiService;
	private final transient ConfigService configService;
	
	public BrowseCompileTask(
			final CleaningForkJoinTaskCleaner cleaner,
			final ModD mod,
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
			baseUiService.parseBaseUI(mod.getGame());
			final String raceId = configService.getRaceId();
			final String consoleSkinId = configService.getConsoleSkinId();
			final UICatalog uiCatalog = compileService.compile(mod, raceId, false, true, true, consoleSkinId);
			mod.setUiCatalog(uiCatalog);
			controller.setData(mod.getUiCatalog());
		} catch (final InterruptedException e) {
			log.error("Interrupted while compiling.", e);
			Thread.currentThread().interrupt();
			return false;
		} catch (final Exception e) {
			log.error("Error while compiling.", e);
			return false;
		}
		return true;
	}
	
}
