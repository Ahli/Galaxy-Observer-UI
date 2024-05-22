// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.ui;

import com.ahli.hotkey_ui.application.model.OptionValueDef;
import com.ahli.hotkey_ui.application.model.TextValueDef;
import com.ahli.hotkey_ui.application.model.TextValueDefType;
import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class DynamicValueDefEditingTableCell extends TableCell<ValueDef, String> {
	private static final Pattern NUMBER_INPUT_REGEX_PATTERN = Pattern.compile("-?\\d{0,7}(?:[.]\\d{0,4})?");
	
	@Override
	protected void updateItem(final String item, final boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			updateItemNull();
		} else {
			updateItem(item);
		}
	}
	
	private void updateItemNull() {
		log.trace("update valuedef-edit table cell - null");
		setGraphic(null);
		setText(null);
	}
	
	private void updateItem(final String item) {
		final ValueDef data = getTableRow().getItem();
		if (data instanceof TextValueDef tvd) {
			log.trace("update valuedef-edit table cell - label {} - type: {}", item, tvd.getType());
			if (tvd.getType() == TextValueDefType.NUMBER) {
				createNumberEditor(tvd, item);
			} else {
				createTextEditor(tvd, item);
			}
		} else if (data instanceof OptionValueDef ovd) {
			log.trace("update valuedef-edit table cell - label {} - type: {}", item, ovd.getType());
			createChoiceEditor(ovd);
		} else {
			log.trace("update valuedef-edit table cell - label {} - null ValueDef", item);
			setGraphic(null);
		}
	}
	
	private void createNumberEditor(final TextValueDef data, final String item) {
		final TextField textField = new TextField(item);
		
		textField.focusedProperty().addListener((obs, wasFocussed, isFocussed) -> {
			log.debug("createNumberEditor: {}", isFocussed);
			if (isFocussed != null && !isFocussed) {
				final TextField control = (TextField) ((ReadOnlyBooleanProperty) obs).getBean();
				final String input = control.getText().trim();
				if (NUMBER_INPUT_REGEX_PATTERN.matcher(input).matches()) {
					data.setValue(input);
				} else {
					// reset input
					control.setText(data.getValue());
				}
			}
		});
		
		textField.setText(data.getValue());
		
		setGraphic(textField);
	}
	
	private void createTextEditor(final TextValueDef data, final String item) {
		final TextField textField = new TextField(item);
		
		textField.focusedProperty().addListener((obs, wasFocussed, isFocussed) -> {
			log.debug("createTextEditor: {}", isFocussed);
			if (isFocussed != null && !isFocussed) {
				final TextField control = (TextField) ((ReadOnlyBooleanProperty) obs).getBean();
				data.setValue(control.getText());
			}
		});
		
		textField.setText(data.getDisplayValue());
		
		setGraphic(textField);
	}
	
	private void createChoiceEditor(final OptionValueDef data) {
		final ObservableList<String> items = FXCollections.observableArrayList(data.getAllowedDisplayValues());
		final ComboBox<String> comboBox = new ComboBox<>(items);
		
		comboBox.getSelectionModel()
				.selectedIndexProperty()
				.addListener((observable, oldIndex, newIndex) -> data.setSelectedIndex(newIndex.intValue()));
		
		data.getSelectedIndexProperty()
				.addListener((obs, oldIndex, newIndex) -> comboBox.getSelectionModel().select(newIndex.intValue()));
		
		comboBox.getSelectionModel().select(data.getSelectedIndex());
		
		setGraphic(comboBox);
	}
	
}
