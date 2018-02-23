package application.ui.home;

import application.InterfaceBuilderApp;
import application.projects.Project;
import application.projects.ProjectService;
import application.ui.settings.Updateable;
import application.util.FXMLSpringLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HomeController implements Updateable {
	private static final Logger logger = LogManager.getLogger();
	@FXML
	public Button addProject;
	@FXML
	public Button removeProject;
	@Autowired
	private ApplicationContext appContext;
	@FXML
	private ListView<Project> selectionList;
	@FXML
	private Button buildSelection;
	@Autowired
	private ProjectService projectService;
	
	private ObservableList<Project> projectsObservable;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		// grab list of projects per game
		projectsObservable = FXCollections.observableList(projectService.getAllProjects());
		selectionList.setItems(projectsObservable);
		selectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		selectionList.setCellFactory(new Callback<>() {
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
		selectionList.getSelectionModel().selectAll();
	}
	
	private Node getListItemGameImage(final Project project) throws IOException {
		final String path;
		switch (project.getGame()) {
			case SC2:
				path = "res/sc2.png";
				break;
			case HEROES:
				path = "res/heroes.png";
				break;
			default:
				path = "res/ahli.png";
		}
		final ImageView iv = new ImageView(appContext.getResource(path).getURL().toString());
		iv.setFitHeight(32);
		iv.setFitWidth(32);
		return iv;
	}
	
	@Override
	public void update() {
		// nothing to refresh?
	}
	
	public void addProjectAction() throws IOException {
		final FXMLLoader loader = new FXMLSpringLoader(appContext);
		final Dialog<Project> dialog =
				loader.load(appContext.getResource("view/Home_AddProject.fxml").getInputStream());
		dialog.initOwner(addProject.getScene().getWindow());
		final Optional<Project> result = dialog.showAndWait();
		if (result.isPresent()) {
			logger.info("dialog 'add project' result: " + result.get());
			projectsObservable.add(result.get());
		}
	}
	
	public void buildSelectedAction() {
		final List<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {
			InterfaceBuilderApp.getInstance().getNavigationController().clickProgress();
			projectService.build(selectedItems, false);
		}
	}
	
	public void removeSelectedAction() {
		final List<Project> selectedItems = selectionList.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {
			for (final Project p : selectedItems.toArray(new Project[0])) {
				projectService.deleteProject(p);
				projectsObservable.remove(p);
			}
		}
	}
}
