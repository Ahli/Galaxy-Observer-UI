package com.ahli.interfacebuilder.ui.home;

import com.ahli.interfacebuilder.integration.FileService;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractProjectController {
	private final FileService fileService;
	
	protected AbstractProjectController(FileService fileService) {
		this.fileService = fileService;
	}
	
	void showDirectoryChooser(String title, TextField pathLabel) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		pathLabel.getScene();
		final Path path = fileService.cutTillValidDirectory(Path.of(pathLabel.getText()));
		if (path != null) {
			directoryChooser.setInitialDirectory(path.toFile());
		}
		final File f = directoryChooser.showDialog(getWindow(pathLabel));
		if (f != null) {
			pathLabel.setText(f.getAbsolutePath());
		}
	}
	
	private Window getWindow(Node node) {
		return node.getScene().getWindow();
	}
	
}
