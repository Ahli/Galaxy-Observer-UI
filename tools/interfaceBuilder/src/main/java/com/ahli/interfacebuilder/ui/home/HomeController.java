// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.home;

import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.ui.FXMLSpringLoader;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.CompressionMiningController;
import com.ahli.interfacebuilder.ui.progress.TabPaneController;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Log4j2
public class HomeController implements Updateable, FxmlController {
	private final ApplicationContext appContext;
	private final ProjectService projectService;
	private final FileService fileService;
	private final GameService gameService;
	private final TabPaneController tabPaneController;
	private final NavigationController navigationController;
	private final MpqBuilderService mpqBuilderService;
	@FXML
	private Pane selectedPanel;
	@FXML
	private Label selectedName;
	@FXML
	private ImageView selectedImage;
	@FXML
	private Label selectedPath;
	@FXML
	private Label selectedDirSize;
	@FXML
	private Label selectedDirFiles;
	@FXML
	private Label selectedBuildDate;
	@FXML
	private Label selectedBuildSize;
	@FXML
	private Button newProject;
	@FXML
	private Button addProject;
	@FXML
	private ListView<Project> selectionList;
	private ObservableList<Project> projectsObservable;
	
	public HomeController(
			final ApplicationContext appContext,
			final ProjectService projectService,
			final FileService fileService,
			final GameService gameService,
			final TabPaneController tabPaneController,
			final NavigationController navigationController,
			final MpqBuilderService mpqBuilderService) {
		this.appContext = appContext;
		this.projectService = projectService;
		this.fileService = fileService;
		this.gameService = gameService;
		this.tabPaneController = tabPaneController;
		this.navigationController = navigationController;
		this.mpqBuilderService = mpqBuilderService;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	@SuppressWarnings("java:S1905") // unnecessary cast warning (cast is necessary here)
	public void initialize() {
		// grab list of projects per game
		projectsObservable = FXCollections.observableList(projectService.getAllProjects());
		selectionList.setItems(projectsObservable);
		selectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		selectionList.setCellFactory(listViewProject -> new ListCell<>() {
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
		selectionList.getSelectionModel().selectAll();
		selectionList.getSelectionModel()
				.getSelectedItems()
				.addListener((InvalidationListener) observable -> updateSelectedDetailsPanel());
	}
	
	/**
	 * Returns an image reflecting the game with proper size for the project list.
	 *
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private ImageView getListItemGameImage(final Project project) throws IOException {
		final ImageView iv =
				new ImageView(getResourceAsUrl(gameService.getGameItemPath(project.getGameType())).toString());
		iv.setFitHeight(32);
		iv.setFitWidth(32);
		return iv;
	}
	
	/**
	 * Updates the panel showing info about the selected projects.
	 */
	private void updateSelectedDetailsPanel() {
		final ObservableList<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (selectedItems.size() == 1) {
			selectedPanel.setVisible(true);
			final Project p = selectedItems.get(0);
			selectedName.setText(p.getName());
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			selectedBuildDate.setText(
					p.getLastBuildDateTime() == null ? "-" : p.getLastBuildDateTime().format(formatter));
			selectedBuildSize.setText(
					p.getLastBuildSize() == 0 ? "-" : (String.format("%,d", p.getLastBuildSize() / 1024) + " kb"));
			try {
				final Path path = p.getProjectPath();
				selectedDirFiles.setText(String.format("%,d", fileService.getFileCountOfDirectory(path)) + " files");
				selectedDirSize.setText(String.format("%,d", fileService.getDirectorySize(path) / 1024) + " kb");
			} catch (final IOException e) {
				selectedDirSize.setText("-");
				selectedDirFiles.setText("-");
				log.trace("Error updating selected details panel.", e);
			}
			selectedPath.setText(p.getProjectPath().toString());
			try {
				selectedImage.setImage(new Image(getResourceAsUrl(gameService.getGameItemPath(p.getGameType())).toString()));
			} catch (final IOException e) {
				log.error("Failed to load image from project's game setting.", e);
			}
		} else {
			selectedPanel.setVisible(false);
		}
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
		updateSelectedDetailsPanel();
	}
	
	/**
	 * Add a Project to the list.
	 *
	 * @throws IOException
	 */
	public void addProjectAction() throws IOException {
		final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
		final Dialog<Project> dialog = loader.load("classpath:view/Home_AddProjectDialog.fxml");
		dialog.initOwner(addProject.getScene().getWindow());
		final Optional<Project> result = dialog.showAndWait();
		if (result.isPresent()) {
			log.trace("dialog 'add project' result: {}", result::get);
			projectsObservable.add(result.get());
		}
	}
	
	/**
	 * Create a new Project from a template and add it to the list.
	 *
	 * @throws IOException
	 */
	public void newProjectAction() throws IOException {
		final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
		final Dialog<Project> dialog = loader.load("classpath:view/Home_NewProjectDialog.fxml");
		dialog.initOwner(newProject.getScene().getWindow());
		final Optional<Project> result = dialog.showAndWait();
		if (result.isPresent()) {
			log.trace("dialog 'new project' result: {}", result::get);
			projectsObservable.add(result.get());
		}
	}
	
	/**
	 * Edit a single selected project.
	 *
	 * @throws IOException
	 */
	public void editProjectAction() throws IOException {
		final List<Project> projects = selectionList.getSelectionModel().getSelectedItems();
		if (projects.size() == 1) {
			final Project project = projects.get(0);
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final Dialog<Project> dialog = loader.load("classpath:view/Home_AddProjectDialog.fxml");
			dialog.initOwner(addProject.getScene().getWindow());
			((AddProjectDialogController) loader.getController()).getContentController().setProjectToEdit(project);
			final Optional<Project> result = dialog.showAndWait();
			if (result.isPresent()) {
				log.trace("dialog 'edit project' result: {}", result::get);
				updateProjectList();
			}
		}
	}
	
	/**
	 * Update the project list to reflect name/game changes.
	 */
	private void updateProjectList() {
		final MultipleSelectionModel<Project> selectionModel = selectionList.getSelectionModel();
		final Object[] selectedIndices = selectionModel.getSelectedIndices().toArray();
		selectionModel.clearSelection();
		for (final Object i : selectedIndices) {
			selectionModel.select((int) i);
		}
	}
	
	/**
	 * Builds the selected projects.
	 */
	public void buildSelectedAction() {
		final List<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {
			navigationController.clickProgress();
			mpqBuilderService.build(selectedItems, false);
		}
	}
	
	/**
	 * Removes the selected projects.
	 */
	public void removeSelectedAction() {
		final Project[] selectedItems = selectionList.getSelectionModel().getSelectedItems().toArray(new Project[0]);
		if (selectedItems.length > 0) {
			final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.initOwner(getWindow());
			if (selectedItems.length > 1) {
				alert.setTitle(String.format("Remove selected Projects from list? - %s items selected",
						selectedItems.length));
			} else {
				alert.setTitle("Remove selected Project from list? - 1 item selected");
			}
			alert.setHeaderText("""
			                       Are you sure you want to remove the selected projects?
			                       This will not remove any files from the project.
			                    """);
			final ButtonType result = alert.showAndWait().orElse(null);
			if (result == ButtonType.OK) {
				for (final Project p : selectedItems) {
					projectService.deleteProject(p);
					projectsObservable.remove(p);
				}
			}
		}
	}
	
	/**
	 * Returns this UI's window.
	 *
	 * @return
	 */
	public Window getWindow() {
		return selectionList.getScene().getWindow();
	}
	
	/**
	 * Action to view the best compression ruleset for the selected project.
	 *
	 * @throws IOException
	 */
	public void viewBestCompressionForSelected() throws IOException {
		final List<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (selectedItems.size() == 1) {
			final Project project = selectedItems.get(0);
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final Dialog<Project> dialog = loader.load("classpath:view/Home_ViewRuleSet.fxml");
			((ViewRuleSetController) loader.getController()).setProject(project);
			dialog.initOwner(addProject.getScene().getWindow());
			dialog.showAndWait();
		}
	}
	
	/**
	 * Action to mine a better compression for the selected project.
	 */
	public void mineBetterCompressionForSelected() throws IOException {
		final List<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (selectedItems.size() == 1) {
			final Project project = selectedItems.get(0);
			// init UI as Tab in Progress
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final Parent content = loader.load("classpath:view/Progress_CompressionMining.fxml");
			final String tabName = String.format("%s Compression Mining", project.getName());
			
			final TabPane tabPane = tabPaneController.getTabPane();
			final ObservableList<Tab> tabs = tabPane.getTabs();
			Tab newTab = null;
			
			// re-use existing tab with that name
			for (final Tab tab : tabs) {
				if (tab.getText().equals(tabName)) {
					newTab = tab;
					break;
				}
			}
			if (newTab == null) {
				// CASE: new tab
				newTab = new Tab(tabName, content);
				final CompressionMiningController controller = loader.getController();
				
				// context menu with close option
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
				closeItem.setOnAction(new CloseMiningTabAction(newTab, controller));
				contextMenu.getItems().add(closeItem);
				newTab.setContextMenu(contextMenu);
				
				tabs.add(newTab);
				controller.setProject(project);
				// switch to progress and the new tab
				navigationController.clickProgress();
				tabPane.getSelectionModel().select(newTab);
				// start mining
				controller.startMining();
			} else {
				// CASE: recycle existing Tab
				
				// switch to progress and the new tab
				navigationController.clickProgress();
				tabPane.getSelectionModel().select(newTab);
			}
		}
	}
	
	private static final class CloseMiningTabAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final CompressionMiningController controller;
		
		private CloseMiningTabAction(final Tab tab, final CompressionMiningController controller) {
			this.tab = tab;
			this.controller = controller;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			tab.setContextMenu(null);
			controller.stopMining();
		}
	}
}
