// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

public class AppPreloader extends Preloader {
	private static final Logger logger = LogManager.getLogger(AppPreloader.class);
	private Stage stage;
	
	@Override
	public void start(final Stage primaryStage) {
		stage = primaryStage;
		final ProgressIndicator progressIndicator = new ProgressIndicator(-1);
		final Scene scene = new Scene(progressIndicator, 90, 90);
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		scene.setFill(Color.TRANSPARENT);
		try {
			scene.getStylesheets().add(getClass().getResource("/view/preload.css").toURI().toString());
		} catch (final URISyntaxException e) {
			logger.error("preloader stylesheet loading error", e);
		}
		primaryStage.setScene(scene);
		primaryStage.setTitle("Interface Builder");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png")));
		primaryStage.show();
	}
	
	@Override
	public void handleStateChangeNotification(final StateChangeNotification info) {
		if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
			stage.hide();
		}
	}
}
