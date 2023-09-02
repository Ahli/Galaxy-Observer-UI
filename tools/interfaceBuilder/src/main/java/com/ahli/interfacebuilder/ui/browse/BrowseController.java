// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.browse;

import com.ahli.galaxy.ModD;
import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndex;
import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.compile.BrowseCompileTask;
import com.ahli.interfacebuilder.compile.CompileService;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.ui.Alerts;
import com.ahli.interfacebuilder.ui.FXMLSpringLoader;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.PrimaryStageHolder;
import com.ahli.interfacebuilder.ui.Updateable;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.BaseUiExtractionController;
import com.ahli.interfacebuilder.ui.progress.ErrorTabController;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import com.ahli.mpq.MpqEditorInterface;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

@Log4j2
public class BrowseController implements Updateable, FxmlController {
	private final ApplicationContext appContext;
	private final BaseUiService baseUiService;
	private final ConfigService configService;
	private final MpqBuilderService mpqBuilderService;
	private final ProjectService projectService;
	private final GameService gameService;
	private final CompileService compileService;
	private final FileService fileService;
	private final NavigationController navigationController;
	private final CleaningForkJoinPool executor;
	private final ProgressController progressController;
	private final PrimaryStageHolder primaryStage;
	@FXML
	public ListView<Project> projectListView;
	@FXML
	public Label sc2BaseUiDetailsLabel;
	@FXML
	public Label heroesBaseUiDetailsLabel;
	@FXML
	private TabPane tabPane;
	@FXML
	private Label heroesPtrStatusLabel;
	@FXML
	private Label sc2PtrStatusLabel;
	@FXML
	private ChoiceBox<String> heroesChoiceBox;
	@FXML
	private ChoiceBox<String> sc2ChoiceBox;
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
			final CleaningForkJoinPool executor,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
		this.appContext = appContext;
		this.baseUiService = baseUiService;
		this.configService = configService;
		this.mpqBuilderService = mpqBuilderService;
		this.projectService = projectService;
		this.gameService = gameService;
		this.compileService = compileService;
		this.fileService = fileService;
		this.navigationController = navigationController;
		this.executor = executor;
		this.progressController = progressController;
		this.primaryStage = primaryStage;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		controllers = new ArrayList<>(0);
		heroesChoiceBox.setItems(FXCollections.observableArrayList(Messages.getString("browse.live"),
				Messages.getString("browse.ptr")));
		final boolean heroesPtrActive = baseUiService.isPtrActive(gameService.getGameDefHeroes());
		heroesChoiceBox.getSelectionModel().select(heroesPtrActive ? 1 : 0);
		updateHeroesPtrStatusLabel(heroesPtrActive);
		sc2ChoiceBox.setItems(FXCollections.observableArrayList(Messages.getString("browse.live"),
				Messages.getString("browse.ptr")));
		final boolean sc2PtrActive = baseUiService.isPtrActive(gameService.getGameDefSc2());
		sc2ChoiceBox.getSelectionModel().select(sc2PtrActive ? 1 : 0);
		updateSc2PtrStatusLabel(sc2PtrActive);
		
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
						log.error("Failed to find image resource.", e);
					}
				}
			}
		});
	}
	
	private void updateHeroesPtrStatusLabel(final boolean ptrActive) {
		heroesPtrStatusLabel.setText(ptrActive ? Messages.getString("browse.ptrActive") : "");
	}
	
	private void updateSc2PtrStatusLabel(final boolean ptrActive) {
		sc2PtrStatusLabel.setText(ptrActive ? Messages.getString("browse.ptrActive") : "");
	}
	
	/**
	 * Returns an image reflecting the game with proper size for the project list.
	 *
	 * @param project
	 * @return
	 * @throws IOException
	 */
	ImageView getListItemGameImage(final Project project) throws IOException {
		final ImageView iv =
				new ImageView(getResourceAsUrl(gameService.getGameItemPath(project.getGameType())).toString());
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
		updateHeroesPtrStatusLabel(baseUiService.isPtrActive(gameService.getGameDef(GameType.HEROES)));
		updateSc2PtrStatusLabel(baseUiService.isPtrActive(gameService.getGameDef(GameType.SC2)));
		updateBaseUiDetails();
	}
	
	private void updateBaseUiDetails() {
		heroesBaseUiDetailsLabel.setText(buildBaseUiDetailsString(GameType.HEROES,
				baseUiService.isPtrActive(gameService.getGameDef(GameType.HEROES))));
		sc2BaseUiDetailsLabel.setText(buildBaseUiDetailsString(GameType.SC2,
				baseUiService.isPtrActive(gameService.getGameDef(GameType.SC2))));
	}
	
	private String buildBaseUiDetailsString(final GameType gameType, final boolean isPtr) {
		final int[] version = baseUiService.getVersion(gameService.getGameDef(gameType), isPtr);
		if (version == null) {
			return Messages.getString("browse.couldNotCheckGameVersion");
		}
		final StringBuilder sb = new StringBuilder(26); // big enough for default case
		for (final int v : version) {
			sb.append(v).append('.');
		}
		sb.deleteCharAt(sb.length() - 1);
		if (isPtr) {
			sb.append(" - PTR");
		}
		try {
			if (baseUiService.isOutdated(gameType, isPtr)) {
				sb.append(' ').append(Messages.getString("browse.requiresUpdate"));
			} else {
				sb.append(' ').append(Messages.getString("browse.upToDate"));
			}
		} catch (final IOException e) {
			sb.append(' ').append(Messages.getString("browse.couldNotCheckGameVersion"));
			log.trace("Error: could not check game version", e);
		}
		return sb.toString();
	}
	
	public void extractBaseUiHeroes() {
		try {
			final boolean heroesPtr = heroesChoiceBox.getSelectionModel().getSelectedIndex() != 0;
			extractBaseUi(GameType.HEROES, heroesPtr);
			updateHeroesPtrStatusLabel(heroesPtr);
		} catch (final IOException e) {
			log.error("Error extracting Heroes Base UI.", e);
			Alerts.buildExceptionAlert(primaryStage.getPrimaryStage(), e).showAndWait();
		}
	}
	
	private void extractBaseUi(final GameType gameType, final boolean usePtr) throws IOException {
		final ObservableList<Tab> tabs = progressController.getTabPane().getTabs();
		final String tabName = String.format(Messages.getString("browse.extract"), gameType.name());
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
			
			final ScrollPane scrollPane = new ScrollPane(newTxtArea);
			scrollPane.setPannable(true);
			
			// auto-downscrolling
			scrollPane.vvalueProperty().bind(newTxtArea.heightProperty());
			
			// hide unless a text appears because it should usually be empty
			scrollPane.visibleProperty().bind(Bindings.isNotEmpty(newTxtArea.getChildren()));
			
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final AnchorPane rootNode = loader.load("classpath:view/Progress_ExtractBaseUi.fxml");
			
			newTab.setContent(rootNode);
			
			extractionController = loader.getController();
			final ErrorTabController errorTabCtrl = new ErrorTabController(newTab, newTxtArea, true, false, false);
			extractionController.setErrorTabController(errorTabCtrl);
			controllers.add(extractionController);
			
			for (final String threadName : extractionController.getThreadNames()) {
				StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			}
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new ExtractBaseUiCloseAction(newTab, extractionController, controllers));
			contextMenu.getItems().add(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			final Tab newTabFinal = newTab;
			final ObservableList<Node> controllerLoggingAreaChildren =
					extractionController.getLoggingArea().getChildren();
			Platform.runLater(() -> {
				try {
					controllerLoggingAreaChildren.add(scrollPane);
					progressController.getTabPane().getTabs().add(newTabFinal);
				} catch (final Exception e) {
					log.fatal(FATAL_ERROR, e);
				}
			});
		} else {
			// CASE: recycled Tab -> only do the workload
			for (final var controller : controllers) {
				if (controller instanceof final BaseUiExtractionController extractCtrl) {
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
		progressController.getTabPane().getSelectionModel().select(newTab);
		navigationController.clickProgress();
		
		extractionController.start(gameType, usePtr);
	}
	
	public void extractBaseUiSc2() {
		try {
			final boolean sc2Ptr = sc2ChoiceBox.getSelectionModel().getSelectedIndex() != 0;
			extractBaseUi(GameType.SC2, sc2Ptr);
			updateSc2PtrStatusLabel(sc2Ptr);
		} catch (final IOException e) {
			log.error("Error extracting SC2 Base UI.", e);
			Alerts.buildExceptionAlert(primaryStage.getPrimaryStage(), e).showAndWait();
		}
	}
	
	@FXML
	public void browseUiSc2() {
		browseBaseUi(GameType.SC2);
	}
	
	private void browseBaseUi(final GameType gameType) {
		final Game gameData = mpqBuilderService.getGameData(gameType);
		final Updateable controller = createTab(gameData.getGameDef().name());
		if (controller != null) {
			final BrowseLoadBaseUiTask task =
					new BrowseLoadBaseUiTask(executor, gameData, (BrowseTabController) controller, baseUiService);
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
			final Node content = loader.load("classpath:view/Browse_BrowseTab.fxml");
			controller = loader.getController();
			controllers.add(controller);
			final Tab newTab = new Tab(name, content);
			tabPane.getTabs().add(newTab);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new CloseTabAction(newTab, (BrowseTabController) controller, controllers, executor));
			contextMenu.getItems().add(closeItem);
			newTab.setContextMenu(contextMenu);
			
			tabPane.getSelectionModel().select(newTab);
			
		} catch (final IOException e) {
			log.error("failed to load BrowseTab FXML", e);
		}
		return controller;
	}
	
	@FXML
	public void browseUiHeroes() {
		browseBaseUi(GameType.HEROES);
	}
	
	@FXML
	public void browseUiSelected() {
		final ObservableList<Project> selectedItems = projectListView.getSelectionModel().getSelectedItems();
		for (final Project project : selectedItems) {
			final Updateable controller = createTab(project.getName());
			if (controller != null) {
				final ModD mod = gameService.getModData(project.getGameType());
				mod.setSourceDirectory(project.getProjectPath());
				final Path cachePath =
						Path.of(configService.getMpqCachePath().toString(), "browseCache", project.getName());
				try {
					Files.createDirectories(cachePath);
				} catch (final IOException e) {
					log.error("ERROR: could not create directories.", e);
					continue;
				}
				mod.setMpqCacheDirectory(cachePath);
				
				@SuppressWarnings("ObjectAllocationInLoop")
				final MpqEditorInterface mpqi = new MpqEditorInterface(cachePath, configService.getMpqEditorPath());
				if (!mpqi.clearCacheExtractedMpq()) {
					log.error("ERROR: could not clear cache directory.");
					continue;
				}
				try {
					fileService.copyFileOrDirectory(project.getProjectPath(), cachePath);
				} catch (final IOException e) {
					log.error("ERROR: could not copy project files.", e);
					continue;
				}
				
				@SuppressWarnings("ObjectAllocationInLoop")
				final DescIndex descIndex = new DescIndex(mpqi);
				mod.setDescIndex(descIndex);
				
				final Path componentListFile = mpqi.getComponentListFile();
				mod.setComponentListFile(componentListFile);
				
				try {
					descIndex.setDescIndexPathAndClear(ComponentsListReaderDom.getDescIndexPath(componentListFile,
							mod.getGame().getGameDef()));
				} catch (final ParserConfigurationException | SAXException | IOException e) {
					log.error("ERROR: unable to read DescIndex path.", e);
					continue;
				}
				
				@SuppressWarnings("ObjectAllocationInLoop")
				final BrowseCompileTask task = new BrowseCompileTask(executor,
						mod,
						(BrowseTabController) controller,
						compileService,
						baseUiService,
						configService);
				executor.execute(task);
			}
		}
	}
	
	private static final class CloseTabAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final BrowseTabController controller;
		private final List<Updateable> controllers;
		private final CleaningForkJoinPool executor;
		
		private CloseTabAction(
				final Tab tab,
				final BrowseTabController controller,
				final List<Updateable> controllers,
				final CleaningForkJoinPool executor) {
			this.tab = tab;
			this.controller = controller;
			this.controllers = controllers;
			this.executor = executor;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			final ContextMenu contextMenu = tab.getContextMenu();
			tab.setContextMenu(null);
			controllers.remove(controller);
			// TODO BrowseTabController is not garbage collected => remove heavy data
			controller.setData(null);
			
			// TODO Tab is not garbage collected due to Scene's mouseHandler
			executor.tryCleanUp();
			
			// context menu is not properly cleaned up
			if (contextMenu != null) {
				contextMenu.setOnAction(null);
			}
		}
	}
	
	private static final class ExtractBaseUiCloseAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final BaseUiExtractionController controller;
		private final List<Updateable> controllers;
		
		private ExtractBaseUiCloseAction(
				final Tab tab, final BaseUiExtractionController controller, final List<Updateable> controllers) {
			this.tab = tab;
			this.controller = controller;
			this.controllers = controllers;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			controllers.remove(controller);
			controller.setErrorTabController(null);
			StylizedTextAreaAppender.unregister(controller.getErrorTabController());
			// for some reason this class was not garbage collected
			tab.getContextMenu().getItems().get(0).setOnAction(null);
			tab.getContextMenu().getItems().clear();
			tab.setContextMenu(null);
		}
	}
}
