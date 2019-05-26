// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.navigation;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfacebuilder.ui.FXMLSpringLoader;
import interfacebuilder.ui.settings.Updateable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NavigationController {
	public static final int HOME_TAB = 0;
	public static final int PROGRESS_TAB = 1;
	public static final int BROWSE_TAB = 2;
	public static final int SETTINGS_TAB = 3;
	private static final Logger logger = LogManager.getLogger(NavigationController.class);
	private static NavigationController instance;
	/* ContentPages:
	 * 0: taskChoice
	 * 1: tabPane
	 * 2: settings */
	private final Parent[] contentPages = new Parent[4];
	private final Updateable[] controllers = new Updateable[4];
	private final List<Notification> notifications = new ArrayList<>();
	@FXML
	private AnchorPane selectedMarker;
	@FXML
	private Button browse;
	//@FXML
	//private Button notificationCloseButton;
	@FXML
	private Label notificationLabel;
	@FXML
	private AnchorPane notificationBar;
	@Autowired
	private ApplicationContext appContext;
	@FXML
	private AnchorPane contentContainer;
	@FXML
	private Button home;
	@FXML
	private Button settings;
	@FXML
	private Button progress;
	private int activeContent = -1;
	
	public NavigationController() {
		if (instance == null) {
			instance = this;
		} else {
			throw new ExceptionInInitializerError("Cannot create multiple NavigationControllers.");
		}
	}
	
	/**
	 * Returns the singleton instance.
	 *
	 * @return instance or null
	 */
	public static NavigationController getInstance() {
		return instance;
	}
	
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
		icon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
		icon.setFill(Color.WHITE);
		browse.setGraphic(icon);
		browse.setText(null);
		
		notificationBar.setVisible(false);
		notificationBar.managedProperty().bind(notificationBar.visibleProperty());
		notificationBar.setBackground(new Background(
				new BackgroundFill(Color.color(211D / 256D, 168D / 255D, 3D / 255D), CornerRadii.EMPTY, Insets.EMPTY)));
		selectedMarker.setBackground(new Background(
				new BackgroundFill(Color.color(211D / 256D, 168D / 255D, 3D / 255D), CornerRadii.EMPTY, Insets.EMPTY)));
		
		// content pages
		initFXML("classpath:view/Content_Home.fxml", 0);
		initFXML("classpath:view/Content_TabPane.fxml", 1);
		initFXML("classpath:view/Content_UiBrowser.fxml", 2);
		initFXML("classpath:view/Content_Settings.fxml", 3);
		
		// make tabPane visible
		showPanelContent(HOME_TAB);
	}
	
	/**
	 * Initializes the TabPane for the Compiling Progress.
	 */
	private void initFXML(final String path, final int index) {
		try {
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			contentPages[index] = loader.load(path);
			final Object controller = loader.getController();
			controllers[index] = (controller instanceof Updateable) ? (Updateable) controller : null;
		} catch (final IOException e) {
			logger.error("failed to load FXML: " + path + ".", e);
			contentPages[index] = null;
			controllers[index] = null;
		}
	}
	
	/**
	 * Shows the content of a specified index in the central panel.
	 *
	 * @param contentIndex
	 */
	private void showPanelContent(final int contentIndex) {
		if (contentIndex != activeContent) {
			activeContent = contentIndex;
			final ObservableList<Node> activeNodes = contentContainer.getChildren();
			activeNodes.clear();
			// update event for controller
			if (controllers[contentIndex] != null) {
				controllers[contentIndex].update();
			}
			activeNodes.add(contentPages[contentIndex]);
			// move marker image
			markTab(contentIndex);
		}
	}
	
	/**
	 * @param contentIndex
	 */
	private void markTab(final int contentIndex) {
		selectedMarker.setLayoutY(8.0 + contentIndex * 28.0);
	}
	
	/**
	 * Called when the user clicks on the home button.
	 */
	@FXML
	public void clickHome() {
		showPanelContent(HOME_TAB);
	}
	
	/**
	 * Called when the user clicks on the progress button.
	 */
	@FXML
	public void clickProgress() {
		showPanelContent(PROGRESS_TAB);
	}
	
	/**
	 *
	 */
	@FXML
	public void clickBrowse() {
		showPanelContent(BROWSE_TAB);
	}
	
	/**
	 * Called when the user clicks on the settings button.
	 */
	@FXML
	public void clickSettings() {
		showPanelContent(SETTINGS_TAB);
	}
	
	/**
	 * Locks the navigation to the Progress screen.
	 */
	public void lockNavToProgress() {
		home.setDisable(true);
		settings.setDisable(true);
		showPanelContent(PROGRESS_TAB);
	}
	
	/**
	 * Releases all navigation button locks.
	 */
	public void unlockNav() {
		home.setDisable(false);
		settings.setDisable(false);
		progress.setDisable(false);
	}
	
	/**
	 * Appends a notification that will be shown.
	 *
	 * @param notification
	 */
	public void appendNotification(final Notification notification) {
		notifications.add(notification);
		showFirstNotification();
	}
	
	private void showFirstNotification() {
		notificationBar.setVisible(true);
		final Notification notification = notifications.get(0);
		notificationLabel.setText(notification.getText());
	}
	
	@FXML
	public void closeActiveNotification() {
		if (!notifications.isEmpty()) {
			notifications.remove(0);
			if (notifications.isEmpty()) {
				notificationBar.setVisible(false);
			} else {
				showFirstNotification();
			}
		}
	}
	
	@FXML
	public void openNotificationLink() {
		final Notification notification = notifications.get(0);
		showNavPage(notification.getNavPageIndex());
	}
	
	/**
	 * Shows the nav page of the specified index.
	 *
	 * @param navPageIndex
	 */
	public void showNavPage(final int navPageIndex) {
		showPanelContent(navPageIndex);
	}
	
}
