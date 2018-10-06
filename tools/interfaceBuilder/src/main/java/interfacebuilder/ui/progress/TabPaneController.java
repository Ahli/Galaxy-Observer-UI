package interfacebuilder.ui.progress;

import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.ui.settings.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;
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
		//		final StyleClassedTextArea txtArea = new StyleClassedTextArea();
		//		txtArea.setEditable(false);
		// txtArea within special ScrollPane
		//		final VirtualizedScrollPane<StyleClassedTextArea> virtualizedScrollPane = new VirtualizedScrollPane<>(txtArea);
		final TextFlow txtArea = new TextFlow();
		txtArea.getChildren().add(new Text("This is how text looks like in TextFlow."));
		
		// special ScrollPane within first Tab of tabPane
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final Tab tab = tabs.get(0);
		//		tab.setContent(virtualizedScrollPane);
		tab.setContent(txtArea);
		
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
