// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.home;

import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.ui.Alerts;
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
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;

@Log4j2
public class NewProjectController extends AbstractProjectController {
	private final ProjectService projectService;
	@FXML
	private ChoiceBox<GameType> gameDropdown;
	@FXML
	private TextField projectPathLabel;
	@FXML
	private TextField projectNameLabel;
	private Dialog<Project> dialog;
	private Project project;
	
	public NewProjectController(final ProjectService projectService, final FileService fileService) {
		super(fileService);
		this.projectService = projectService;
	}
	
	public void browsePathAction() {
		showDirectoryChooser(Messages.getString("Customize Toolbar..."), projectPathLabel);
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
			dialog.setTitle(Messages.getString("newProject.dialogTitle"));
			final DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			final Button okBttn = (Button) dialogPane.lookupButton(ButtonType.APPLY);
			okBttn.addEventFilter(ActionEvent.ACTION, this::newProjectAction);
			okBttn.setText(Messages.getString("general.createButton"));
			dialog.setResultConverter(param -> project);
			
			gameDropdown.setItems(FXCollections.observableArrayList(GameType.SC2, GameType.HEROES));
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
		log.trace("new project action event fired");
		final GameType gameType = gameDropdown.getValue();
		if (gameType == null) {
			// eat event before it reaches the resultConverter
			event.consume();
			return;
		}
		
		// ensure that the project's path is not used in another project
		final Path path = Path.of(projectPathLabel.getText());
		if (!projectService.getProjectsOfPath(path).isEmpty()) {
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					Messages.getString("Main.anErrorOccurred"),
					Messages.getString("newProject.pathAlreadyUsedInOtherProject.header"),
					String.format(Messages.getString("newProject.pathAlreadyUsedInOtherProject.content"), path));
			alert.showAndWait();
			return;
		}
		
		// try creating the project
		final String name = projectNameLabel.getText();
		project = new Project(name, path, gameType);
		try {
			project = projectService.saveProject(project);
		} catch (final Exception e) {
			log.error("ERROR: Could not create project.", e);
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					Messages.getString("Main.anErrorOccurred"),
					Messages.getString("newProject.couldNotCreateProject.header"),
					String.format(Messages.getString("newProject.couldNotCreateProject.content"), path) +
							e.getLocalizedMessage());
			alert.showAndWait();
			return;
		}
		
		// try creating the template & clean up the project if it fails
		try {
			projectService.createTemplateProjectFiles(project);
			log.trace("new project template created");
		} catch (final IOException e) {
			log.error("ERROR: Could not create template for project.", e);
			projectService.deleteProject(project);
			final Alert alert = Alerts.buildErrorAlert(
					dialog.getOwner(),
					Messages.getString("Main.anErrorOccurred"),
					Messages.getString("newProject.couldNotCreateTemplate.header"),
					String.format(Messages.getString("newProject.couldNotCreateTemplate.content"), path) +
							e.getLocalizedMessage());
			alert.showAndWait();
		}
	}
}
