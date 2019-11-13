// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress;

import com.ahli.galaxy.game.def.abstracts.GameDef;
import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.compress.GameService;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.FileService;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.Updateable;
import interfacebuilder.ui.progress.appender.Appender;
import interfacebuilder.ui.progress.appender.TextFlowAppender;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseUiExtractionController implements Updateable {
	
	private static int threadCount;
	private final String[] threadNames;
	@FXML
	public VBox loggingArea;
	@FXML
	private Label titleLabel;
	@FXML
	private TextFlow txtArea1;
	@FXML
	private Label areaLabel1;
	@FXML
	private TextFlow txtArea2;
	@FXML
	private Label areaLabel2;
	@FXML
	private TextFlow txtArea3;
	@FXML
	private Label areaLabel3;
	@Autowired
	private ConfigService configService;
	@Autowired
	private FileService fileService;
	@Autowired
	private BaseUiService baseUiService;
	@Autowired
	private GameService gameService;
	private ErrorTabController errorTabController;
	
	public BaseUiExtractionController() {
		threadNames = new String[3];
		for (int i = 0; i < threadNames.length; ++i) {
			threadNames[i] = "extractThread_" + ++threadCount;
		}
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
	
	public void start(final Game game, final boolean usePtr) {
		errorTabController.setRunning(true);
		final GameDef exportedGameDef = gameService.getNewGameDef(game);
		final String ptrString = usePtr ? " PTR" : "";
		titleLabel.setText(String.format("Extract %s's Base UI", exportedGameDef.getName() + ptrString));
		txtArea1.getChildren().clear();
		txtArea2.getChildren().clear();
		txtArea3.getChildren().clear();
		final Appender[] output = new Appender[3];
		output[0] = new TextFlowAppender(txtArea1);
		output[1] = new TextFlowAppender(txtArea2);
		output[2] = new TextFlowAppender(txtArea3);
		baseUiService.extract(game, usePtr, output);
		
		// TODO setRunning(false) after extraction was done -> integrate completableFuture into TaskExecutor
		// errorTabController.setRunning(false);
	}
	
	public String[] getThreadNames() {
		return threadNames;
	}
	
	public void setErrorTabControl(final ErrorTabController errorTabCtrl) {
		errorTabController = errorTabCtrl;
	}
	
	public ErrorTabController getErrorTabController() {
		return errorTabController;
	}
}
