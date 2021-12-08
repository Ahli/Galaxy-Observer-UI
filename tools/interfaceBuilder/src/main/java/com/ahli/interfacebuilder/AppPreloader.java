// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

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
			final URL cssFile = getClass().getResource("/view/preload.css");
			if (cssFile != null) {
				scene.getStylesheets().add(cssFile.toURI().toString());
			} else {
				logger.warn("preloader stylesheet not found");
			}
		} catch (final URISyntaxException e) {
			logger.error("preloader stylesheet loading error", e);
		}
		primaryStage.setScene(scene);
		primaryStage.setTitle("Interface Builder");
		final InputStream iconStream = getClass().getResourceAsStream("/res/ahli.png");
		if (iconStream != null) {
			primaryStage.getIcons().add(new Image(iconStream));
		} else {
			logger.warn("preloader icon not found");
		}
		primaryStage.show();
	}
	
	@Override
	public void handleStateChangeNotification(final StateChangeNotification info) {
		if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
			stage.hide();
		}
	}
}
