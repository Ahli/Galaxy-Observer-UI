// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui;

import interfacebuilder.i18n.Messages;
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

public final class Alerts {
	
	private Alerts() {
		// no instances allowed
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildYesNoCancelAlert(
			final Window owner, final String title, final String header, final String content) {
		final ButtonType yesButton = new ButtonType(Messages.getString("general.yesButton"), ButtonData.YES);
		final ButtonType noButton = new ButtonType(Messages.getString("general.noButton"), ButtonData.NO);
		final ButtonType cancelButton =
				new ButtonType(Messages.getString("general.cancelButton"), ButtonData.CANCEL_CLOSE);
		final Alert alert = new Alert(AlertType.INFORMATION, content, yesButton, noButton, cancelButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		return alert;
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildErrorAlert(
			final Window owner, final String title, final String header, final String content) {
		final ButtonType okButton = createOkButton();
		final Alert alert = new Alert(AlertType.ERROR, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		return alert;
	}
	
	private static ButtonType createOkButton() {
		return new ButtonType(Messages.getString("general.okButton"), ButtonData.OK_DONE);
	}
	
	/**
	 * @param owner
	 * @param title
	 * @param header
	 * @param content
	 * @return
	 */
	public static Alert buildWarningAlert(
			final Window owner, final String title, final String header, final String content) {
		final ButtonType okButton = createOkButton();
		final Alert alert = new Alert(AlertType.WARNING, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
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
	public static Alert buildAboutAlert(
			final Window owner, final String title, final String header, final String content, final String imageUrl) {
		final ButtonType okButton = createOkButton();
		final Alert alert = new Alert(AlertType.INFORMATION, content, okButton);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		if (imageUrl != null) {
			alert.setGraphic(new ImageView(imageUrl));
		}
		alert.getDialogPane().setPrefSize(480, 360);
		return alert;
	}
	
	/**
	 * @param owner
	 * @param e
	 * @return
	 */
	public static Alert buildExceptionAlert(final Window owner, final Throwable e) {
		final ButtonType okButton = createOkButton();
		final String localizedMsg = e.getLocalizedMessage();
		final Alert alert = new Alert(AlertType.ERROR, localizedMsg, okButton);
		alert.initOwner(owner);
		
		final Label label = new Label(Messages.getString("general.exceptionStackTrace"));
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
		
		return alert;
	}
}
