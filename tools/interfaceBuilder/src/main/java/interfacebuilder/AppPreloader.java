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
	public void start(final Stage stage) {
		this.stage = stage;
		final ProgressIndicator progressIndicator = new ProgressIndicator(-1);
		final Scene scene = new Scene(progressIndicator, 90, 90);
		stage.initStyle(StageStyle.TRANSPARENT);
		scene.setFill(Color.TRANSPARENT);
		try {
			scene.getStylesheets().add(getClass().getResource("/view/preload.css").toURI().toString());
		} catch (final URISyntaxException e) {
			logger.error("preloader stylesheet loading error", e);
		}
		stage.setScene(scene);
		stage.setTitle("Interface Builder");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png")));
		stage.show();
	}
	
	@Override
	public void handleStateChangeNotification(final StateChangeNotification info) {
		if (info.getType().equals(StateChangeNotification.Type.BEFORE_START)) {
			stage.hide();
		}
	}
}