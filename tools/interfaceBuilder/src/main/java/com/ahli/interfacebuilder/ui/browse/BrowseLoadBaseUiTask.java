// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.browse;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.threads.CleaningForkJoinTask;
import lombok.extern.log4j.Log4j2;

import java.io.Serial;

@Log4j2
public class BrowseLoadBaseUiTask extends CleaningForkJoinTask {
	
	@Serial
	private static final long serialVersionUID = -2905917491106111107L;
	
	private final transient Game game;
	private final transient BrowseTabController controller;
	private final transient BaseUiService baseUiService;
	
	public BrowseLoadBaseUiTask(
			final CleaningForkJoinPool executor,
			final Game game,
			final BrowseTabController controller,
			final BaseUiService baseUiService) {
		super(executor);
		this.game = game;
		this.controller = controller;
		this.baseUiService = baseUiService;
	}
	
	@Override
	protected boolean work() {
		try {
			baseUiService.parseBaseUI(game);
		} catch (final Exception e) {
			log.error("Error while parsing Base UI.", e);
			return false;
		}
		controller.setData(game.getUiCatalog());
		return true;
	}
	
}
