// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress;

import interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import interfacebuilder.ui.AppController;
import interfacebuilder.ui.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;

public class TabPaneController implements Updateable {
	
	private final AppController appController;
	@FXML
	private TabPane tabPane;
	
	public TabPaneController(final AppController appController) {
		this.appController = appController;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
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
		appController.addErrorTabController(errorTabCtrl);
		StylizedTextAreaAppender.setGeneralController(errorTabCtrl);
	}
	
	/**
	 * Returns the TabPane.
	 *
	 * @return
	 */
	public TabPane getTabPane() {
		return tabPane;
	}
	
	@Override
	public void update() {
		// nothing to do
	}
}
