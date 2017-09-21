package com.ahli.hotkeyUi.application.ui;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.i18n.Messages;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

/**
 * 
 * @author Ahli
 *
 */
public class Alerts {
	static Logger LOGGER = LogManager.getLogger("Alerts");

	/**
	 * 
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildYesNoCancelAlert(Window owner, String title, String header, String content) {
		long time = System.nanoTime();
		ButtonType yesButton = new ButtonType(Messages.getString("General.YesButton"), ButtonData.YES); //$NON-NLS-1$
		ButtonType noButton = new ButtonType(Messages.getString("General.NoButton"), ButtonData.NO); //$NON-NLS-1$
		ButtonType cancelButton = new ButtonType(Messages.getString("General.CancelButton"), ButtonData.CANCEL_CLOSE); // $NON-NLS-1$
		Alert alert = new Alert(AlertType.INFORMATION, content, yesButton, noButton, cancelButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		LOGGER.warn("created yesNoCancelAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}

	/**
	 * 
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildErrorAlert(Window owner, String title, String header, String content) {
		long time = System.nanoTime();
		ButtonType okButton = new ButtonType(Messages.getString("General.OkButton"), ButtonData.YES); //$NON-NLS-1$
		Alert alert = new Alert(AlertType.ERROR, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		LOGGER.warn("created errorAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}

	/**
	 * 
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @param imageUrl
	 * @return
	 */
	public static Alert buildAboutAlert(Window owner, String title, String header, String content, String imageUrl) {
		long time = System.nanoTime();
		ButtonType okButton = new ButtonType(Messages.getString("General.OkButton"), ButtonData.OK_DONE); //$NON-NLS-1$
		Alert alert = new Alert(AlertType.INFORMATION, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		if (imageUrl != null) {
			alert.setGraphic(new ImageView(imageUrl));
		}
		alert.getDialogPane().setPrefSize(480, 360);
		LOGGER.warn("initialized about-alert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}

	/**
	 * 
	 * @param owner
	 * @param e
	 * @return
	 */
	public static Alert buildExceptionAlert(Window owner, Exception e) {
		long time = System.nanoTime();
		ButtonType okButton = new ButtonType(Messages.getString("General.OkButton"), ButtonData.OK_DONE); //$NON-NLS-1$
		String localizedMsg = e.getLocalizedMessage();
		Alert alert = new Alert(AlertType.ERROR, localizedMsg, okButton);
		alert.initOwner(owner);

		Label label = new Label(Messages.getString("General.ExceptionStackTrace")); //$NON-NLS-1$
		String stackTrace = ExceptionUtils.getStackTrace(e);

		TextArea textArea = new TextArea(stackTrace);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);
		LOGGER.warn("created exceptionAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");

		return alert;
	}
}
