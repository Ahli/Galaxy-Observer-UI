// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.home;

import interfacebuilder.integration.FileService;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class AddProjectController {
	private static final Logger logger = LogManager.getLogger(AddProjectController.class);
	
	@FXML
	private Dialog<Project> dialog;
	@FXML
	private ChoiceBox<Game> gameDropdown;
	@FXML
	private TextField projectPathLabel;
	@FXML
	private TextField projectNameLabel;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private FileService fileService;
	private Project project;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		dialog.setTitle("Add Observer Interface Project...");
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
		final Button okBttn = (Button) dialogPane.lookupButton(ButtonType.APPLY);
		okBttn.addEventFilter(ActionEvent.ACTION, this::addProjectAction);
		dialog.setResultConverter(param -> project);
		
		gameDropdown.setItems(FXCollections.observableArrayList(Game.SC2, Game.HEROES));
		gameDropdown.getSelectionModel().select(0);
	}
	
	public void addProjectAction(final ActionEvent actionEvent) {
		logger.trace("add project dialog creates project instance");
		final String name = projectNameLabel.getText();
		final String path = projectPathLabel.getText();
		final Game game = gameDropdown.getValue();
		if (game == null) {
			// eat event before it reaches the resultConverter
			actionEvent.consume();
			return;
		}
		if (project == null) {
			project = new Project(name, path, game);
		} else {
			project.setGame(game);
			project.setName(name);
			project.setProjectPath(path);
		}
		project = projectService.saveProject(project);
	}
	
	public void browsePathAction() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select an existing Observer UI's directory.");
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
	
	public void setProjectToEdit(final Project project) {
		this.project = project;
		dialog.setTitle("Edit Observer Interface Project...");
		projectNameLabel.setText(project.getName());
		projectPathLabel.setText(project.getProjectPath());
		gameDropdown.getSelectionModel().select(project.getGame());
	}
	
}
