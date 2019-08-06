// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.home;

import interfacebuilder.integration.FileService;
import interfacebuilder.projects.Project;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.projects.enums.Game;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import java.io.IOException;

public class NewProjectController {
	private static final Logger logger = LogManager.getLogger(NewProjectController.class);
	
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
	private Dialog<Project> dialog;
	private Project project;
	
	public NewProjectController() {
		// nothing to do
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
		if (logger.isTraceEnabled()) {
			logger.trace("new project action event fired");
		}
		final Game game = gameDropdown.getValue();
		if (game == null) {
			// eat event before it reaches the resultConverter
			event.consume();
			return;
		}
		final String name = projectNameLabel.getText();
		final String path = projectPathLabel.getText();
		project = new Project(name, path, game);
		project = projectService.saveProject(project);
		try {
			projectService.createTemplateProjectFiles(project);
		} catch (final IOException e) {
			logger.error("ERROR: Could not create template project.", e);
			// TODO popup dialog
		}
	}
}
