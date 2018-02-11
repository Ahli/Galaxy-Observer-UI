package application.controller;

import application.InterfaceBuilderApp;
import application.util.logger.log4j2plugin.StylizedTextAreaAppender;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

public class TabPaneController {
	
	@FXML
	private TabPane tabPane;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		
		// log4j2 prints into txtArea
		final StyleClassedTextArea txtArea = new StyleClassedTextArea();
		txtArea.setEditable(false);
		// txtArea within special ScrollPane
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(txtArea);
		// special ScrollPane within first Tab of tabPane
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		tab.setContent(virtualizedScrollPane);
		
		
		final ErrorTabPaneController errorTabCtrl = new ErrorTabPaneController(tab, txtArea, false, true);
		errorTabCtrl.setRunning(true);
		InterfaceBuilderApp.getInstance().addErrorTabController(errorTabCtrl);
		StylizedTextAreaAppender.setGeneralController(errorTabCtrl);
	}
	
}
