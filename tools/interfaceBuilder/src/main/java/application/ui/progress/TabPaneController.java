package application.ui.progress;

import application.InterfaceBuilderApp;
import application.ui.settings.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

public class TabPaneController implements Updateable {
	private static TabPaneController instance = null;
	
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
		final StyleClassedTextArea txtArea = new StyleClassedTextArea();
		txtArea.setEditable(false);
		// txtArea within special ScrollPane
		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(txtArea);
		// special ScrollPane within first Tab of tabPane
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		tab.setContent(virtualizedScrollPane);
		
		final ErrorTabController errorTabCtrl = new ErrorTabController(tab, txtArea, false, true, false);
		//errorTabCtrl.setRunning(true);
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
