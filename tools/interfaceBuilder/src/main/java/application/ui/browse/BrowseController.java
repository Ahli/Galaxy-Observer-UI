package application.ui.browse;

import application.InterfaceBuilderApp;
import application.baseUi.BaseUiService;
import application.build.MpqBuilderService;
import application.config.ConfigService;
import application.i18n.Messages;
import application.projects.enums.Game;
import application.ui.FXMLSpringLoader;
import application.ui.settings.Updateable;
import com.ahli.galaxy.game.GameData;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BrowseController implements Updateable {
	private final Logger logger = LogManager.getLogger();

	@FXML
	private TabPane tabPane;
	@FXML
	private Label ptrStatusLabel;
	@FXML
	private ChoiceBox<String> heroesChoiceBox;

	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private BaseUiService baseUiService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private MpqBuilderService mpqBuilderService;
	
	private List<Updateable> controllers;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		controllers = new ArrayList<>();
		heroesChoiceBox.setItems(
				FXCollections.observableArrayList(Messages.getString("browse.live"),
						Messages.getString("browse.ptr")));
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
	
	@FXML
	public void browseUiSc2() {
		final Updateable controller = createTab("SC2");
		final GameData gameData = mpqBuilderService.getGameData(Game.SC2);
		((BrowseTabController) controller).setData(gameData);
	}
	
	private Updateable createTab(final String name) {
		Updateable controller = null;
		try {
			final FXMLLoader loader = new FXMLSpringLoader(appContext);
			final Node content =
					loader.load(appContext.getResource("view/Content_UiBrowser_BrowseTab.fxml").getInputStream());
			controller = loader.getController();
			controllers.add(controller);
			final Tab tab = new Tab(name, content);
			tabPane.getTabs().add(tab);
		} catch (final IOException e) {
			logger.error("failed to load BrowseTab FXML", e);
		}
		return controller;
	}
	
	@FXML
	public void browseUiHeroes() {
		final Updateable controller = createTab("Heroes");
		// TODO
	}
}
