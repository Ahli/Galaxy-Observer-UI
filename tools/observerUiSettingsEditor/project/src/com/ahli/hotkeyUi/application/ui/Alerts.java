package com.ahli.hotkeyUi.application.ui;

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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ahli
 */
public class Alerts {
	public static final String GENERAL_OK_BUTTON = "General.OkButton";
	private static Logger logger = LogManager.getLogger(Alerts.class);
	
	private Alerts() {
		// disallow class instances
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildYesNoCancelAlert(final Window owner, final String title, final String header,
			final String content) {
		final long time = System.nanoTime();
		final ButtonType yesButton =
				new ButtonType(Messages.getString("General.YesButton"), ButtonData.YES); //$NON-NLS-1$
		final ButtonType noButton = new ButtonType(Messages.getString("General.NoButton"), ButtonData.NO); //$NON-NLS-1$
		final ButtonType cancelButton =
				new ButtonType(Messages.getString("General.CancelButton"), ButtonData.CANCEL_CLOSE); // $NON-NLS-1$
		final Alert alert = new Alert(AlertType.INFORMATION, content, yesButton, noButton, cancelButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		logger.warn("created yesNoCancelAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildErrorAlert(final Window owner, final String title, final String header,
			final String content) {
		final long time = System.nanoTime();
		final ButtonType okButton = new ButtonType(Messages.getString(GENERAL_OK_BUTTON), ButtonData.YES); //$NON-NLS-1$
		final Alert alert = new Alert(AlertType.ERROR, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		logger.warn("created errorAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildInfoAlert(final Window owner, final String title, final String header,
			final String content) {
		final long time = System.nanoTime();
		final ButtonType okButton =
				new ButtonType(Messages.getString("General.OkButton"), ButtonData.YES); //$NON-NLS-1$
		final Alert alert = new Alert(AlertType.INFORMATION, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		logger.warn("created infoAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildWarningAlert(final Window owner, final String title, final String header,
			final String content) {
		final long time = System.nanoTime();
		final ButtonType okButton =
				new ButtonType(Messages.getString("General.OkButton"), ButtonData.YES); //$NON-NLS-1$
		final Alert alert = new Alert(AlertType.WARNING, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		logger.warn("created warningAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @param imageUrl
	 * @return
	 */
	public static Alert buildAboutAlert(final Window owner, final String title, final String header,
			final String content, final String imageUrl) {
		final long time = System.nanoTime();
		final ButtonType okButton =
				new ButtonType(Messages.getString("General.OkButton"), ButtonData.OK_DONE); //$NON-NLS-1$
		final Alert alert = new Alert(AlertType.INFORMATION, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		if (imageUrl != null) {
			alert.setGraphic(new ImageView(imageUrl));
		}
		alert.getDialogPane().setPrefSize(480, 360);
		logger.warn("initialized about-alert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		return alert;
	}
	
	/**
	 * @param owner
	 * @param e
	 * @return
	 */
	public static Alert buildExceptionAlert(final Window owner, final Throwable e) {
		final long time = System.nanoTime();
		final ButtonType okButton =
				new ButtonType(Messages.getString("General.OkButton"), ButtonData.OK_DONE); //$NON-NLS-1$
		final String localizedMsg = e.getLocalizedMessage();
		final Alert alert = new Alert(AlertType.ERROR, localizedMsg, okButton);
		alert.initOwner(owner);
		
		final Label label = new Label(Messages.getString("General.ExceptionStackTrace")); //$NON-NLS-1$
		final String stackTrace = ExceptionUtils.getStackTrace(e);
		
		final TextArea textArea = new TextArea(stackTrace);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		
		final GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		
		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);
		logger.warn("created exceptionAlert within " + (System.nanoTime() - time) / 1000000 + "ms.");
		
		return alert;
	}
}
