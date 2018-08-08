package interfacebuilder.ui.browse;

import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.UITemplate;
import interfacebuilder.ui.settings.Updateable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeView;

public class BrowseTabController implements Updateable {
	@FXML
	private TreeView frameTree;
	@FXML
	private ChoiceBox<UITemplate> templateDropdown;
	private GameData gameData;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
	}
	
	public void setData(final GameData gameData) {
		this.gameData = gameData;
		update();
	}
	
	@Override
	public void update() {
		final ObservableList<UITemplate> templates =
				FXCollections.observableList(gameData.getUiCatalog().getTemplates());
		templateDropdown.setItems(templates);
	}
}
