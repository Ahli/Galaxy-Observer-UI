// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress;

import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import interfacebuilder.ui.settings.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;

public class TabPaneController implements Updateable {
	private static TabPaneController instance;
	
	@FXML
	private TabPane tabPane;
	
	public TabPaneController() {
		if (instance == null) {
			instance = this;
		} else {
			throw new ExceptionInInitializerError("Cannot create multiple TabPaneControllers.");
		}
	}
	
	/**
	 * Returns the singleton instance.
	 *
	 * @return instance or null
	 */
	public static TabPaneController getInstance() {
		return instance;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		
		// log4j2 prints into txtArea
		final TextFlow txtArea = new TextFlow();
		txtArea.getStyleClass().add("styled-text-area");
		final ScrollPane scrollPane = new ScrollPane(txtArea);
		scrollPane.getStyleClass().add("virtualized-scroll-pane");
		
		// special ScrollPane within first Tab of tabPane
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		tab.setContent(scrollPane);
		
		final ErrorTabController errorTabCtrl = new ErrorTabController(tab, txtArea, false, true, false);
		InterfaceBuilderApp.getInstance().addErrorTabController(errorTabCtrl);
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
