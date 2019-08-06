// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress;

import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.FileService;
import interfacebuilder.ui.settings.Updateable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseUiExctractionController implements Updateable {
	
	@FXML
	public Label titleLabel;
	
	@Autowired
	private ConfigService configService;
	@Autowired
	private FileService fileService;
	@Autowired
	private BaseUiService baseUiService;
	
	public BaseUiExctractionController() {
		// nothing to do
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		// nothing to do
	}
	
	@Override
	public void update() {
		// nothing to do
	}
	
}
