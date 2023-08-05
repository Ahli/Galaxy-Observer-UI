// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.progress;

import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;
import lombok.Getter;

public class TabPaneController implements Updateable, FxmlController {
	
	private ProgressController progressController;
	
	@Getter
	@FXML
	private TabPane tabPane;
	
	public TabPaneController() {
		// explicit constructor
	}
	
	public void setProgressController(final ProgressController progressController) {
		this.progressController = progressController;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		
		// log4j2 prints into txtArea
		final TextFlow txtArea = new TextFlow();
		final ScrollPane scrollPane = new ScrollPane(txtArea);
		scrollPane.setPannable(true);
		
		// auto-downscrolling
		scrollPane.vvalueProperty().bind(txtArea.heightProperty());
		
		// special ScrollPane within first Tab of tabPane
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		tab.setContent(scrollPane);
		
		final ErrorTabController errorTabCtrl = new ErrorTabController(tab, txtArea, false, true, true);
		progressController.addErrorTabController(errorTabCtrl);
		StylizedTextAreaAppender.setGeneralController(errorTabCtrl);
	}
	
	@Override
	public void update() {
		// nothing to do
	}
}
