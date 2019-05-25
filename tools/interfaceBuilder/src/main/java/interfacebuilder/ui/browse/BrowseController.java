// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.archive.ComponentsListReaderDom;
import com.ahli.galaxy.archive.DescIndexData;
import com.ahli.galaxy.game.GameData;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.mpq.MpqEditorInterface;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compile.CompileService;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.i18n.Messages;
import interfacebuilder.integration.FileService;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.FXMLSpringLoader;
import interfacebuilder.ui.settings.Updateable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BrowseController implements Updateable {
	private static final Logger logger = LogManager.getLogger(BrowseController.class);
	
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
	
	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private BaseUiService baseUiService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private MpqBuilderService mpqBuilderService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private GameService gameService;
	@Autowired
	private CompileService compileService;
	@Autowired
	private FileService fileService;
	
	private List<Updateable> controllers;
	
	public BrowseController() {
		// nothing to do
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		controllers = new ArrayList<>();
		heroesChoiceBox.setItems(
				FXCollections.observableArrayList(Messages.getString("browse.live"), Messages.getString("browse.ptr")));
		final boolean ptrActive = baseUiService.isHeroesPtrActive();
		heroesChoiceBox.getSelectionModel().select(ptrActive ? 1 : 0);
		updatePtrStatusLabel(ptrActive);
		
		final ObservableList<Project> projectsObservable =
				FXCollections.observableList(projectService.getAllProjects());
		projectListView.setItems(projectsObservable);
		projectListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		projectListView.setCellFactory(new Callback<>() {
			@Override
			public ListCell<Project> call(final ListView<Project> p) {
				return new ListCell<>() {
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
				};
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
		final StringBuilder sb = new StringBuilder();
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
		baseUiService.extract(Game.SC2, false);
	}
	
	public void extractBaseUiHeroes() {
		final boolean usePtr = heroesChoiceBox.getSelectionModel().getSelectedIndex() != 0;
		baseUiService.extract(Game.HEROES, usePtr);
		updatePtrStatusLabel(usePtr);
	}
	
	@FXML
	public void browseUiSc2() {
		browseBaseUi(Game.SC2);
	}
	
	private void browseBaseUi(final Game game) {
		final GameData gameData = mpqBuilderService.getGameData(game);
		final Updateable controller = createTab(gameData.getGameDef().getName());
		if (controller != null) {
			final Runnable followupTask = () -> ((BrowseTabController) controller).setData(gameData.getUiCatalog());
			baseUiService.parseBaseUI(gameData, followupTask);
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
			final FXMLLoader loader = new FXMLSpringLoader(appContext);
			final Node content = loader.load(
					appContext.getResource("classpath:view/Content_UiBrowser_BrowseTab.fxml").getInputStream());
			controller = loader.getController();
			controllers.add(controller);
			final Tab newTab = new Tab(name, content);
			tabPane.getTabs().add(newTab);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			final Updateable controllerRef = controller;
			closeItem.setOnAction(event -> {
				tabPane.getTabs().remove(newTab);
				controllers.remove(controllerRef);
			});
			contextMenu.getItems().addAll(closeItem);
			newTab.setContextMenu(contextMenu);
			
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
				mod.setSourceDirectory(new File(project.getProjectPath()));
				final String cachePath =
						configService.getMpqCachePath() + File.separator + "browseCache" + File.separator +
								project.getName();
				final File cacheDir = new File(cachePath);
				try {
					Files.createDirectories(cacheDir.toPath());
				} catch (final IOException e) {
					logger.error("ERROR: could not create directories.", e);
					continue;
				}
				mod.setMpqCacheDirectory(cacheDir);
				
				final MpqEditorInterface mpqi = new MpqEditorInterface(cachePath, configService.getMpqEditorPath());
				if (!mpqi.clearCacheExtractedMpq()) {
					logger.error("ERROR: could not clear cache directory.");
					continue;
				}
				try {
					fileService.copyFileOrDirectory(new File(project.getProjectPath()), cacheDir);
				} catch (final IOException e) {
					logger.error("ERROR: could not copy project files.", e);
					continue;
				}
				
				final DescIndexData descIndexData = new DescIndexData(mpqi);
				mod.setDescIndexData(descIndexData);
				
				final File componentListFile = mpqi.getComponentListFile();
				mod.setComponentListFile(componentListFile);
				
				try {
					descIndexData.setDescIndexPathAndClear(ComponentsListReaderDom
							.getDescIndexPath(componentListFile, mod.getGameData().getGameDef()));
				} catch (final ParserConfigurationException | SAXException | IOException e) {
					logger.error("ERROR: unable to read DescIndex path.", e);
					continue;
				}
				
				final Runnable followupTask = () -> {
					try {
						// TODO cache compiled uicatalogs
						final UICatalog uiCatalog = compileService
								.compile(mod, configService.getRaceId(), false, true, true,
										configService.getConsoleSkinId());
						mod.setUi(uiCatalog);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					((BrowseTabController) controller).setData(mod.getUi());
				};
				baseUiService.parseBaseUI(mod.getGameData(), followupTask);
			}
		}
	}
}
