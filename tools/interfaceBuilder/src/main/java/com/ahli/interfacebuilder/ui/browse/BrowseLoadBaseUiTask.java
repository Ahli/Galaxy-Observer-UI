// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.browse;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BrowseLoadBaseUiTask implements Runnable {
	private final Game game;
	private final BrowseTabController controller;
	private final BaseUiService baseUiService;
	
	public BrowseLoadBaseUiTask(
			final Game game, final BrowseTabController controller, final BaseUiService baseUiService) {
		this.game = game;
		this.controller = controller;
		this.baseUiService = baseUiService;
	}
	
	@Override
	public void run() {
		try {
			baseUiService.parseBaseUI(game);
		} catch (final Exception e) {
			log.error("Error while parsing Base UI.", e);
		}
		controller.setData(game.getUiCatalog());
	}
	
}
