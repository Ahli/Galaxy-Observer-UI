package application.ui.browse;

import application.baseUi.BaseUiService;
import application.config.ConfigService;
import application.i18n.Messages;
import application.projects.enums.Game;
import application.ui.settings.Updateable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;

public class BrowseController implements Updateable {
	@FXML
	private Label ptrStatusLabel;
	
	@FXML
	private ChoiceBox<String> heroesChoiceBox;
	
	@Autowired
	private BaseUiService baseUiService;
	
	@Autowired
	private ConfigService configService;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		heroesChoiceBox.setItems(
				FXCollections.observableArrayList(Messages.getString("browse.live"), Messages.getString("browse.ptr")));
		final boolean ptrActive = configService.getIniSettings().isHeroesPtrActive();
		heroesChoiceBox.getSelectionModel().select(ptrActive ? 1 : 0);
		updatePtrStatusLabel(ptrActive);
	}
	
	private void updatePtrStatusLabel(final boolean ptrActive) {
		ptrStatusLabel.setText(ptrActive ? Messages.getString("browse.ptrActive") : "");
	}
	
	@Override
	public void update() {
		updatePtrStatusLabel(configService.getIniSettings().isHeroesPtrActive());
	}
	
	public void extractBaseUiSc2() {
		baseUiService.extract(Game.SC2, false);
	}
	
	public void extractBaseUiHeroes() {
		final boolean usePtr = heroesChoiceBox.getSelectionModel().getSelectedIndex() != 0;
		baseUiService.extract(Game.HEROES, usePtr);
		updatePtrStatusLabel(usePtr);
	}
	
}
