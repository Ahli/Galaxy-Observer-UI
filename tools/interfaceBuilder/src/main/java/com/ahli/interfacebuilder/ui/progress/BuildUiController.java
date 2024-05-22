package com.ahli.interfacebuilder.ui.progress;

import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.Setter;

public class BuildUiController implements Updateable, FxmlController {
	
	private final MpqBuilderService mpqBuilderService;
	@FXML
	public ScrollPane scrollPane;
	@FXML
	public TextFlow txtArea;
	@Setter
	private Project project;
	@Setter
	@Getter
	private ErrorTabController errorTabController;
	
	public BuildUiController(MpqBuilderService mpqBuilderService) {
		this.mpqBuilderService = mpqBuilderService;
	}
	
	public void build() {
		if (errorTabController != null && !errorTabController.isRunning() && project != null) {
			errorTabController.clearError(false);
			errorTabController.clearWarning(false);
			errorTabController.setRunning(true);
			mpqBuilderService.build(project);
		}
	}
	
	@Override
	public void initialize() {
		// auto-downscrolling
		scrollPane.vvalueProperty().bind(txtArea.heightProperty());
	}
	
	@Override
	public void update() {
		// do nothing
	}
}
