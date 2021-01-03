// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.mpq.MpqEditorInterface;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compile.BrowseCompileTask;
import interfacebuilder.compile.CompileService;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.i18n.Messages;
import interfacebuilder.integration.FileService;
import interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.Alerts;
import interfacebuilder.ui.AppController;
import interfacebuilder.ui.FXMLSpringLoader;
import interfacebuilder.ui.Updateable;
import interfacebuilder.ui.navigation.NavigationController;
import interfacebuilder.ui.progress.BaseUiExtractionController;
import interfacebuilder.ui.progress.ErrorTabController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static interfacebuilder.InterfaceBuilderApp.FATAL_ERROR;

public class BrowseController implements Updateable {
	private static final Logger logger = LogManager.getLogger(BrowseController.class);
	private final ApplicationContext appContext;
	private final BaseUiService baseUiService;
	private final ConfigService configService;
	private final MpqBuilderService mpqBuilderService;
	private final ProjectService projectService;
	private final GameService gameService;
	private final CompileService compileService;
	private final FileService fileService;
	private final NavigationController navigationController;
	private final AppController appController;
	private final ForkJoinPool executor;
	@FXML
	public ListView<Project> projectListView;
	@FXML
	public Label sc2BaseUiDetailsLabel;
	@FXML
	public Label heroesBaseUiDetailsLabel;
	@FXML
	private TabPane tabPane;
	@FXML
	private Label ptrStatusLabel;
	@FXML
	private ChoiceBox<String> heroesChoiceBox;
	private List<Updateable> controllers;
	
	public BrowseController(
			final ApplicationContext appContext,
			final BaseUiService baseUiService,
			final ConfigService configService,
			final MpqBuilderService mpqBuilderService,
			final ProjectService projectService,
			final GameService gameService,
			final CompileService compileService,
			final FileService fileService,
			final NavigationController navigationController,
			final AppController appController,
			final ForkJoinPool executor) {
		this.appContext = appContext;
		this.baseUiService = baseUiService;
		this.configService = configService;
		this.mpqBuilderService = mpqBuilderService;
		this.projectService = projectService;
		this.gameService = gameService;
		this.compileService = compileService;
		this.fileService = fileService;
		this.navigationController = navigationController;
		this.appController = appController;
		this.executor = executor;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		controllers = new ArrayList<>();
		heroesChoiceBox.setItems(FXCollections.observableArrayList(Messages.getString("browse.live"),
				Messages.getString("browse.ptr")));
		final boolean ptrActive = baseUiService.isHeroesPtrActive();
		heroesChoiceBox.getSelectionModel().select(ptrActive ? 1 : 0);
		updatePtrStatusLabel(ptrActive);
		
		final ObservableList<Project> projectsObservable =
				FXCollections.observableList(projectService.getAllProjects());
		projectListView.setItems(projectsObservable);
		projectListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		projectListView.setCellFactory(listViewProject -> new ListCell<>() {
			@Override
			protected void updateItem(final Project project, final boolean empty) {
				super.updateItem(project, empty);
				if (empty || project == null) {
					setText(null);
					setGraphic(null);
				} else {
					setText(project.getName());
					try {
						setGraphic(getListItemGameImage(project));
					} catch (final IOException e) {
						logger.error("Failed to find image resource.", e);
					}
				}
			}
		});
	}
	
	private void updatePtrStatusLabel(final boolean ptrActive) {
		ptrStatusLabel.setText(ptrActive ? Messages.getString("browse.ptrActive") : "");
	}
	
	/**
	 * Returns an image reflecting the game with proper size for the project list.
	 *
	 * @param project
	 * @return
	 * @throws IOException
	 */
	ImageView getListItemGameImage(final Project project) throws IOException {
		final ImageView iv = new ImageView(getResourceAsUrl(gameService.getGameItemPath(project.getGame())).toString());
		iv.setFitHeight(32);
		iv.setFitWidth(32);
		return iv;
	}
	
	/**
	 * Returns a resource as a URL.
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private URL getResourceAsUrl(final String path) throws IOException {
		return appContext.getResource(path).getURL();
	}
	
	@Override
	public void update() {
		updatePtrStatusLabel(baseUiService.isHeroesPtrActive());
		updateBaseUiDetails();
	}
	
	private void updateBaseUiDetails() {
		sc2BaseUiDetailsLabel.setText(buildBaseUiDetailsString(Game.SC2, false));
		heroesBaseUiDetailsLabel.setText(buildBaseUiDetailsString(Game.HEROES, baseUiService.isHeroesPtrActive()));
	}
	
	private String buildBaseUiDetailsString(final Game game, final boolean isPtr) {
		final StringBuilder sb = new StringBuilder(25); // big enough for default case
		final int[] version = baseUiService.getVersion(gameService.getNewGameDef(game), isPtr);
		for (final int v : version) {
			sb.append(v).append('.');
		}
		sb.deleteCharAt(sb.length() - 1);
		if (isPtr) {
			sb.append(" - PTR");
		}
		try {
			if (baseUiService.isOutdated(game, isPtr)) {
				sb.append(" - requires update");
			} else {
				sb.append(" - up to date");
			}
		} catch (final IOException e) {
			sb.append(" - could not check game version");
			if (logger.isTraceEnabled()) {
				logger.trace("Error: could not check game version", e);
			}
		}
		return sb.toString();
	}
	
	public void extractBaseUiSc2() {
		try {
			extractBaseUi(Game.SC2, false);
		} catch (final IOException e) {
			logger.error("Error extracting Heroes Base UI.", e);
			Alerts.buildExceptionAlert(appController.getPrimaryStage(), e).showAndWait();
		}
	}
	
	private void extractBaseUi(final Game game, final boolean usePtr) throws IOException {
		final ObservableList<Tab> tabs = appController.getTabPane().getTabs();
		final String tabName = "Extract " + game.name();
		Tab newTab = null;
		BaseUiExtractionController extractionController = null;
		
		// re-use existing tab with that name
		for (final Tab tab : tabs) {
			if (tab.getText().equals(tabName)) {
				newTab = tab;
				break;
			}
		}
		if (newTab == null) {
			// CASE: new tab
			newTab = new Tab(tabName);
			
			final TextFlow newTxtArea = new TextFlow();
			newTxtArea.getStyleClass().add("styled-text-area");
			
			final ScrollPane scrollPane = new ScrollPane(newTxtArea);
			scrollPane.getStyleClass().add("virtualized-scroll-pane");
			scrollPane.setPannable(true);
			
			// auto-downscrolling
			scrollPane.vvalueProperty().bind(newTxtArea.heightProperty());
			
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final var rootNode = loader.<AnchorPane>load("classpath:view/ProgressTab_ExtractBaseUi.fxml");
			
			newTab.setContent(rootNode);
			
			extractionController = loader.getController();
			final ErrorTabController errorTabCtrl = new ErrorTabController(newTab, newTxtArea, true, false, true);
			extractionController.setErrorTabControl(errorTabCtrl);
			controllers.add(extractionController);
			
			for (final String threadName : extractionController.getThreadNames()) {
				StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			}
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new ExtractBaseUiCloseAction(newTab, extractionController, controllers));
			contextMenu.getItems().addAll(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			final Tab newTabFinal = newTab;
			final ObservableList<Node> controllerLoggingAreaChildren = extractionController.loggingArea.getChildren();
			Platform.runLater(() -> {
				try {
					controllerLoggingAreaChildren.add(scrollPane);
					appController.getTabPane().getTabs().add(newTabFinal);
				} catch (final Exception e) {
					logger.fatal(FATAL_ERROR, e);
				}
			});
		} else {
			// CASE: recycled Tab -> only do the workload
			for (final var controller : controllers) {
				if (controller instanceof BaseUiExtractionController) {
					final BaseUiExtractionController extractCtrl = (BaseUiExtractionController) controller;
					final ErrorTabController errorTabCtrl = extractCtrl.getErrorTabController();
					if (errorTabCtrl != null) {
						newTab = errorTabCtrl.getTab();
						if (newTab != null && tabName.equals(newTab.getText())) {
							// found the correct one
							extractionController = extractCtrl;
							break;
						}
					}
				}
			}
			if (extractionController == null) {
				throw new IOException("The desired BaseUiExtractionController was not found while recycling a Tab.");
			}
		}
		// select progress & tab
		appController.getTabPane().getSelectionModel().select(newTab);
		navigationController.clickProgress();
		
		extractionController.start(game, usePtr);
	}
	
	public void extractBaseUiHeroes() {
		try {
			final boolean usePtr = heroesChoiceBox.getSelectionModel().getSelectedIndex() != 0;
			extractBaseUi(Game.HEROES, usePtr);
			updatePtrStatusLabel(usePtr);
		} catch (final IOException e) {
			logger.error("Error extracting Heroes Base UI.", e);
			Alerts.buildExceptionAlert(appController.getPrimaryStage(), e).showAndWait();
		}
	}
	
	@FXML
	public void browseUiSc2() {
		browseBaseUi(Game.SC2);
	}
	
	private void browseBaseUi(final Game game) {
		final GameData gameData = mpqBuilderService.getGameData(game);
		final Updateable controller = createTab(gameData.getGameDef().getName());
		if (controller != null) {
			final BrowseLoadBaseUiTask task =
					new BrowseLoadBaseUiTask(appController, gameData, (BrowseTabController) controller, baseUiService);
			executor.execute(task);
		}
	}
	
	/**
	 * Creates a Tab with the specified name, registers its controller and returns it.
	 *
	 * @param name
	 * 		name of the Tnew ab
	 * @return the created Tab
	 */
	private Updateable createTab(final String name) {
		Updateable controller = null;
		try {
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final Node content = loader.load("classpath:view/Content_UiBrowser_BrowseTab.fxml");
			controller = loader.getController();
			controllers.add(controller);
			final Tab newTab = new Tab(name, content);
			tabPane.getTabs().add(newTab);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new CloseTabAction(newTab,
					(BrowseTabController) controller,
					controllers,
					appController));
			contextMenu.getItems().addAll(closeItem);
			newTab.setContextMenu(contextMenu);
			
			tabPane.getSelectionModel().select(newTab);
			
		} catch (final IOException e) {
			logger.error("failed to load BrowseTab FXML", e);
		}
		return controller;
	}
	
	@FXML
	public void browseUiHeroes() {
		browseBaseUi(Game.HEROES);
	}
	
	@FXML
	public void browseUiSelected() {
		final ObservableList<Project> selectedItems = projectListView.getSelectionModel().getSelectedItems();
		for (final Project project : selectedItems) {
			final Updateable controller = createTab(project.getName());
			if (controller != null) {
				final ModData mod = gameService.getModData(project.getGame());
				mod.setSourceDirectory(Path.of(project.getProjectPath()));
				final Path cachePath =
						Path.of(configService.getMpqCachePath().toString(), "browseCache", project.getName());
				try {
					Files.createDirectories(cachePath);
				} catch (final IOException e) {
					logger.error("ERROR: could not create directories.", e);
					continue;
				}
				mod.setMpqCacheDirectory(cachePath);
				
				final MpqEditorInterface mpqi = new MpqEditorInterface(cachePath, configService.getMpqEditorPath());
				if (!mpqi.clearCacheExtractedMpq()) {
					logger.error("ERROR: could not clear cache directory.");
					continue;
				}
				try {
					fileService.copyFileOrDirectory(Path.of(project.getProjectPath()), cachePath);
				} catch (final IOException e) {
					logger.error("ERROR: could not copy project files.", e);
					continue;
				}
				
				final DescIndexData descIndexData = new DescIndexData(mpqi);
				mod.setDescIndexData(descIndexData);
				
				final Path componentListFile = mpqi.getComponentListFile();
				mod.setComponentListFile(componentListFile);
				
				try {
					descIndexData.setDescIndexPathAndClear(ComponentsListReaderDom.getDescIndexPath(componentListFile,
							mod.getGameData().getGameDef()));
				} catch (final ParserConfigurationException | SAXException | IOException e) {
					logger.error("ERROR: unable to read DescIndex path.", e);
					continue;
				}
				
				final BrowseCompileTask task = new BrowseCompileTask(appController,
						mod,
						(BrowseTabController) controller,
						compileService,
						baseUiService,
						configService);
				executor.execute(task);
			}
		}
	}
	
	private static class CloseTabAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final BrowseTabController controller;
		private final List<Updateable> controllers;
		private final AppController appController;
		
		public CloseTabAction(
				final Tab tab,
				final BrowseTabController controller,
				final List<Updateable> controllers,
				final AppController appController) {
			this.tab = tab;
			this.controller = controller;
			this.controllers = controllers;
			this.appController = appController;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			tab.setContextMenu(null);
			controllers.remove(controller);
			// TODO BrowseTabController is not garbage collected => remove heavy data
			controller.setData(null);
			
			// TODO Tab is not garbage collected due to Scene's mouseHandler
			appController.tryCleanUp();
		}
	}
	
	private static class ExtractBaseUiCloseAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final BaseUiExtractionController controller;
		private final List<Updateable> controllers;
		
		public ExtractBaseUiCloseAction(
				final Tab tab, final BaseUiExtractionController controller, final List<Updateable> controllers) {
			this.tab = tab;
			this.controller = controller;
			this.controllers = controllers;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			controllers.remove(controller);
			controller.setErrorTabControl(null);
			StylizedTextAreaAppender.unregister(controller.getErrorTabController());
			// for some reason this class was not garbage collected
			tab.getContextMenu().getItems().get(0).setOnAction(null);
			tab.getContextMenu().getItems().clear();
			tab.setContextMenu(null);
		}
	}
}
