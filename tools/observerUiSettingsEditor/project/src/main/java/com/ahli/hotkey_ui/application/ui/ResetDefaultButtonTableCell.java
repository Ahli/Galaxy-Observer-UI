// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.ui;

import com.ahli.hotkey_ui.application.model.ValueDef;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A table cell for ValueDef model data to reset its value to the default or the old value.
 *
 * @author Ahli
 */
public class ResetDefaultButtonTableCell extends TableCell<ValueDef, Boolean> {
	private static final Logger logger = LogManager.getLogger(ResetDefaultButtonTableCell.class);
	
	private final Button resetToDefaultButton = new Button();
	private final Button resetToOldValueButton = new Button();
	
	/**
	 * @param resetToDefaultText
	 * @param resetToOldValueText
	 */
	public ResetDefaultButtonTableCell(final String resetToDefaultText, final String resetToOldValueText) {
		resetToDefaultButton.setText(resetToDefaultText);
		resetToOldValueButton.setText(resetToOldValueText);
		
		resetToDefaultButton.setOnAction(this::resetToDefault);
		resetToOldValueButton.setOnAction(this::resetToOldValue);
	}
	
	private void resetToDefault(final ActionEvent event) {
		logger.trace("reset value to default value button clicked");
		final ValueDef data = getTableRow().getItem();
		data.setValue(data.getDefaultValue());
	}
	
	private void resetToOldValue(final ActionEvent event) {
		logger.trace("reset value to old value button clicked");
		final ValueDef data = getTableRow().getItem();
		data.setValue(data.getOldValue());
	}
	
	// Display button if the row is not empty
	@Override
	protected void updateItem(final Boolean t, final boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			setGraphic(new HBox(resetToDefaultButton, resetToOldValueButton));
		} else {
			setGraphic(null);
		}
	}
}
