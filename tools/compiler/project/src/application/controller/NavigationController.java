package application.controller;

import application.InterfaceBuilderApp;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class NavigationController {
	private static final Logger logger = LogManager.getLogger();
	
	@FXML
	private AnchorPane contentContainer;
	@FXML
	private Button home;
	@FXML
	private Button settings;
	@FXML
	private Button progress;
	
	/* ContentPages:
	 * 0: taskChoice
	 * 1: tabPane
	 * 2: settings */
	private Parent[] contentPages = new Parent[3];
	private Updateable[] controllers = new Updateable[3];
	
	private int activeContent = -1;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		// nav button icons
		FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.HOME);
		icon.setFill(Color.WHITE);
		home.setGraphic(icon);
		home.setText(null);
		icon = new FontAwesomeIconView(FontAwesomeIcon.COG);
		icon.setFill(Color.WHITE);
		settings.setGraphic(icon);
		settings.setText(null);
		icon = new FontAwesomeIconView(FontAwesomeIcon.BAR_CHART);
		icon.setFill(Color.WHITE);
		progress.setGraphic(icon);
		progress.setText(null);
		
		// content pages
		initFXML("view/Content_TaskChoice.fxml", 0);
		initFXML("view/Content_TabPane.fxml", 1);
		initFXML("view/Content_Settings.fxml", 2);
		
		// make tabPane visible
		showPanelContent(1);
	}
	
	/**
	 * Initializes the TabPane for the Compiling Progress.
	 */
	private void initFXML(String path, int index) {
		try {
			FXMLLoader loader = new FXMLLoader();
			contentPages[index] = loader.load(InterfaceBuilderApp.class.getResourceAsStream(path));
			Object controller = loader.getController();
			controllers[index] = (controller instanceof Updateable) ? (Updateable) controller : null;
		} catch (IOException e) {
			logger.error("failed to load FXML: " + path + ".", e); //$NON-NLS-1$
			e.printStackTrace();
			contentPages[index] = null;
			controllers[index] = null;
		}
	}
	
	/**
	 * Shows the content of a specified index in the central panel.
	 *
	 * @param contentIndex
	 */
	private void showPanelContent(int contentIndex) {
		if (contentIndex != activeContent) {
			activeContent = contentIndex;
			ObservableList<Node> activeNodes = contentContainer.getChildren();
			activeNodes.clear();
			if (controllers[contentIndex] != null) {
				controllers[contentIndex].update();
			}
			activeNodes.add(contentPages[contentIndex]);
		}
	}
	
	/**
	 * Called when the user clicks on the home button.
	 */
	@FXML
	public void clickHome() {
		showPanelContent(0);
	}
	
	/**
	 * Called when the user clicks on the settings button.
	 */
	@FXML
	public void clickSettings() {
		showPanelContent(2);
	}
	
	/**
	 * Called when the user clicks on the progress button.
	 */
	@FXML
	public void clickProgress() {
		showPanelContent(1);
	}
	
	
}
