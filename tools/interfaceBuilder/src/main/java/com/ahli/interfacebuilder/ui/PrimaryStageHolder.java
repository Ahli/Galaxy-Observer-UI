package com.ahli.interfacebuilder.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

@Log4j2
@Getter
@Setter
public class PrimaryStageHolder {
	private Stage primaryStage;
	
	public boolean hasPrimaryStage() {
		return primaryStage != null;
	}
	
	/**
	 * Prints a message to the message log.
	 *
	 * @param msg
	 * 		the message
	 */
	public void printInfoLogMessageToGeneral(final String msg) {
		if (primaryStage != null) {
			Platform.runLater(() -> {
				try {
					log.info(msg);
				} catch (final Exception e) {
					log.fatal(FATAL_ERROR, e);
				}
			});
		} else {
			log.info(msg);
		}
	}
}
