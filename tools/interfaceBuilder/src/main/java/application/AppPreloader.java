package application;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppPreloader extends Preloader {
	
	private Stage stage;
	
	@Override
	public void start(final Stage stage) {
		this.stage = stage;
		final Scene scene = new Scene(new ProgressIndicator(-1), 100, 100);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setScene(scene);
		stage.setTitle("Interface Builder");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/res/ahli.png"))); //$NON-NLS-1$
		stage.show();
	}
	
	@Override
	public void handleStateChangeNotification(final StateChangeNotification info) {
		if (info.getType().equals(StateChangeNotification.Type.BEFORE_START)) {
			stage.hide();
		}
	}
}