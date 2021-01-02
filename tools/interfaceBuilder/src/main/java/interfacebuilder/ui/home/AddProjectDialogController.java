// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.home;

import interfacebuilder.projects.Project;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;

public class AddProjectDialogController {
	
	@FXML
	private Pane contentPane;
	@FXML
	private AddProjectController contentPaneController;
	@FXML
	private Dialog<Project> dialog;
	
	public AddProjectDialogController() {
		// nothing to do
	}
	
	public AddProjectController getContentController() {
		return contentPaneController;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		contentPaneController.initialize(dialog);
	}
}
