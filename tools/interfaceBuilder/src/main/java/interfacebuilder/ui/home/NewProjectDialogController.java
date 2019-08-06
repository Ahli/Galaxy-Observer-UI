package interfacebuilder.ui.home;

import interfacebuilder.projects.Project;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;

public class NewProjectDialogController {
	
	@FXML
	private Pane contentPane;
	@FXML
	private NewProjectController contentPaneController;
	
	@FXML
	private Dialog<Project> dialog;
	
	
	public NewProjectDialogController() {
		// nothing to do
	}
	
	public NewProjectController getContentController() {
		return contentPaneController;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		contentPaneController.initialize(dialog);
	}
}
