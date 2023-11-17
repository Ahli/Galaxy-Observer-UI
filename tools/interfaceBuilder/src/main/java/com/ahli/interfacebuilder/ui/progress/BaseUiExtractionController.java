// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.progress;

import com.ahli.galaxy.game.GameDef;
import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.base_ui.ExtractBaseUiTask;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.appenders.Appender;
import com.ahli.interfacebuilder.ui.progress.appenders.TextFlowAppender;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

public class BaseUiExtractionController implements Updateable, FxmlController {
	
	private static int threadCount;
	@Getter
	private final String[] threadNames;
	private final BaseUiService baseUiService;
	private final GameService gameService;
	private final NavigationController navigationController;
	private final CleaningForkJoinPool executor;
	
	@FXML
	private FontAwesomeIconView stateImage1;
	@FXML
	private FontAwesomeIconView stateImage2;
	@FXML
	private FontAwesomeIconView stateImage3;
	@FXML
	private ScrollPane scrollPane1;
	@FXML
	private ScrollPane scrollPane2;
	@FXML
	private ScrollPane scrollPane3;
	@Getter
	@FXML
	private Pane loggingArea;
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
	
	@Getter
	@Setter
	private ErrorTabController errorTabController;
	
	public BaseUiExtractionController(
			final BaseUiService baseUiService,
			final GameService gameService,
			final NavigationController navigationController,
			final CleaningForkJoinPool executor) {
		this.baseUiService = baseUiService;
		this.gameService = gameService;
		this.navigationController = navigationController;
		this.executor = executor;
		final String threadName = "extractThread_";
		threadNames = new String[3];
		threadNames[0] = threadName + ++threadCount;
		threadNames[1] = threadName + ++threadCount;
		threadNames[2] = threadName + ++threadCount;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		// auto-downscrolling
		scrollPane1.vvalueProperty().bind(txtArea1.heightProperty());
		scrollPane2.vvalueProperty().bind(txtArea2.heightProperty());
		scrollPane3.vvalueProperty().bind(txtArea3.heightProperty());
		stateImage1.setVisible(false);
		stateImage2.setVisible(false);
		stateImage3.setVisible(false);
	}
	
	@Override
	public void update() {
		// nothing to do
	}
	
	public void start(@NonNull final GameType gameType, final boolean usePtr) {
		errorTabController.setRunning(true);
		final GameDef exportedGameDef = gameService.getGameDef(gameType);
		final String ptrString = usePtr ? " PTR" : "";
		titleLabel.setText(String.format("Extract %s's Base UI", exportedGameDef.name() + ptrString));
		txtArea1.getChildren().clear();
		txtArea2.getChildren().clear();
		txtArea3.getChildren().clear();
		final Appender[] appenders = new Appender[3];
		appenders[0] = new TextFlowAppender(txtArea1);
		appenders[1] = new TextFlowAppender(txtArea2);
		appenders[2] = new TextFlowAppender(txtArea3);
		appenders[0].endedProperty().addListener(new EndedListener(stateImage1, txtArea1));
		appenders[1].endedProperty().addListener(new EndedListener(stateImage2, txtArea2));
		appenders[2].endedProperty().addListener(new EndedListener(stateImage3, txtArea3));
		
		final String[] queryMasks = BaseUiService.getQueryMasks(gameType);
		final String msg = "Extracting %s files";
		areaLabel1.setText(String.format(msg, queryMasks[0]));
		areaLabel2.setText(String.format(msg, queryMasks[1]));
		areaLabel3.setText(String.format(msg, queryMasks[2]));
		
		stateImage1.setIcon(FontAwesomeIcon.SPINNER);
		stateImage2.setIcon(FontAwesomeIcon.SPINNER);
		stateImage3.setIcon(FontAwesomeIcon.SPINNER);
		stateImage1.setFill(Color.GAINSBORO);
		stateImage2.setFill(Color.GAINSBORO);
		stateImage3.setFill(Color.GAINSBORO);
		stateImage1.setVisible(true);
		stateImage2.setVisible(true);
		stateImage3.setVisible(true);
		
		final ExtractBaseUiTask task = new ExtractBaseUiTask(executor,
				baseUiService,
				gameType,
				usePtr,
				appenders,
				errorTabController,
				navigationController);
		executor.execute(task);
	}
	
	private record EndedListener(FontAwesomeIconView stateImage, TextFlow txtArea) implements ChangeListener<Boolean> {
		private static final String ERROR = "ERROR:";
		private static final String WARNING = "WARNING:";
		
		@Override
		public void changed(
				final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
			// the text children are added after the UI updates
			Platform.runLater(() -> {
				boolean isError = false;
				boolean isWarning = false;
				final ObservableList<Node> children = txtArea.getChildren();
				if (children != null && !children.isEmpty()) {
					for (final Node child : children) {
						if (child instanceof final Text text) {
							if (text.getText().contains(ERROR)) {
								isError = true;
								break;
							}
							if (text.getText().contains(WARNING)) {
								isWarning = true;
								break;
							}
						}
					}
				} else {
					isError = true;
				}
				if (isError) {
					stateImage.setIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
					stateImage.setFill(Color.RED);
				} else if (isWarning) {
					stateImage.setIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
					stateImage.setFill(Color.YELLOW);
				} else {
					stateImage.setIcon(FontAwesomeIcon.CHECK);
					stateImage.setFill(Color.LAWNGREEN);
				}
			});
		}
	}
}
