// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.ui;

import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

/**
 * A table cell for ValueDef model data to reset its value to the default or the old value.
 *
 * @author Ahli
 */
@Slf4j
public class ResetDefaultButtonTableCell extends TableCell<ValueDef, Boolean> {
	
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
		log.trace("reset value to default value button clicked");
		final ValueDef data = getTableRow().getItem();
		data.resetToDefault();
	}
	
	private void resetToOldValue(final ActionEvent event) {
		log.trace("reset value to old value button clicked");
		final ValueDef data = getTableRow().getItem();
		data.resetToOldValue();
	}
	
	// Display button if the row is not empty
	@Override
	protected void updateItem(final Boolean value, final boolean empty) {
		super.updateItem(value, empty);
		if (!empty && getTableRow().getItem() != null) {
			if (value != null && value) {
				if (getTableRow().getItem().isDefaultValue()) {
					setGraphic(resetToOldValueButton);
				} else {
					setGraphic(new HBox(resetToDefaultButton, resetToOldValueButton));
				}
			} else {
				if (getTableRow().getItem().isDefaultValue()) {
					setGraphic(null);
				} else {
					setGraphic(resetToDefaultButton);
				}
			}
		} else {
			setGraphic(null);
		}
	}
}
