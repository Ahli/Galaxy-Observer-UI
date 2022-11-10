// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui;

import com.ahli.interfacebuilder.i18n.Messages;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
