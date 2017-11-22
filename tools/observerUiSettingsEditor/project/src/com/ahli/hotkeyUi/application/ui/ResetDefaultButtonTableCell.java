package com.ahli.hotkeyUi.application.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.model.ValueDef;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

/**
 * A table cell for ValueDef model data to reset its value to the default value.
 * 
 * @author Ahli
 *
 */
public class ResetDefaultButtonTableCell extends TableCell<ValueDef, Boolean> {
	static Logger LOGGER = LogManager.getLogger(ResetDefaultButtonTableCell.class);
	
	final Button cellButton = new Button();
	
	public ResetDefaultButtonTableCell() {
		cellButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent t) {
				LOGGER.trace("reset table item clicked");
				final ValueDef data = (ValueDef) getTableRow().getItem();
				data.setValue(data.getDefaultValue());
			}
		});
	}
	
	public ResetDefaultButtonTableCell(final String text) {
		this();
		cellButton.setText(text);
	}
	
	// Display button if the row is not empty
	@Override
	protected void updateItem(final Boolean t, final boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			setGraphic(cellButton);
		}
	}
}
