// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.home;

import interfacebuilder.integration.FileService;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.Alerts;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class NewProjectController {
	private static final Logger logger = LogManager.getLogger(NewProjectController.class);
	private final ProjectService projectService;
	private final FileService fileService;
	@FXML
	private ChoiceBox<Game> gameDropdown;
	@FXML
	private TextField projectPathLabel;
	@FXML
	private TextField projectNameLabel;
	private Dialog<Project> dialog;
	private Project project;
	
	public NewProjectController(final ProjectService projectService, final FileService fileService) {
		this.projectService = projectService;
		this.fileService = fileService;
	}
	
	public void browsePathAction() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select a directory to create a template Project in");
		File f = fileService.cutTillValidDirectory(projectPathLabel.getText());
		if (f != null) {
			directoryChooser.setInitialDirectory(f);
		}
		f = directoryChooser.showDialog(getWindow());
		if (f != null) {
			projectPathLabel.setText(f.getAbsolutePath());
		}
	}
	
	private Window getWindow() {
		return projectPathLabel.getScene().getWindow();
	}
	
	/**
	 * Initialize this Controller and sets its dialog.
	 *
	 * @param dialog
	 */
	public void initialize(final Dialog<Project> dialog) {
		this.dialog = dialog;
		initialize();
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	private void initialize() {
		if (dialog != null) {
			dialog.setTitle("Create new Observer Interface Project...");
			final DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			final Button okBttn = (Button) dialogPane.lookupButton(ButtonType.APPLY);
			okBttn.addEventFilter(ActionEvent.ACTION, this::newProjectAction);
			dialog.setResultConverter(param -> project);
			
			gameDropdown.setItems(FXCollections.observableArrayList(Game.SC2, Game.HEROES));
			gameDropdown.getSelectionModel().select(0);
		}
	}
	
	/**
	 * Action to create a new Project based on the parameters selected. Invalid user input will be checked and consume
	 * the specified event.
	 *
	 * @param event
	 * 		button click event
	 */
	public void newProjectAction(final Event event) {
		logger.trace("new project action event fired");
		final Game game = gameDropdown.getValue();
		if (game == null) {
			// eat event before it reaches the resultConverter
			event.consume();
			return;
		}
		
		// ensure that the project's path is not used in another project
		final String path = projectPathLabel.getText();
		final var existingProjects = projectService.getProjectsOfPath(path);
		if (!existingProjects.isEmpty()) {
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					"An Error occurred",
					"Project's path already used in another project",
					String.format(
							"Could not create a Project for path '%s' as it is already registered in another project.",
							path));
			alert.showAndWait();
			return;
		}
		
		// try creating the project
		final String name = projectNameLabel.getText();
		project = new Project(name, Path.of(path), game);
		try {
			project = projectService.saveProject(project);
		} catch (final Exception e) {
			logger.error("ERROR: Could not create project.", e);
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					"An Error occurred",
					"Could not create the Project.",
					String.format("Could not create a Project in '%s'", path) + e.getLocalizedMessage());
			alert.showAndWait();
			return;
		}
		
		// try creating the template & clean up the project if it fails
		try {
			projectService.createTemplateProjectFiles(project);
			logger.trace("new project template created");
		} catch (final IOException e) {
			logger.error("ERROR: Could not create template for project.", e);
			projectService.deleteProject(project);
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					"An Error occurred",
					"Could not create the Template.",
					String.format("Could not create a Template in '%s'", path) + e.getLocalizedMessage());
			alert.showAndWait();
		}
	}
}
