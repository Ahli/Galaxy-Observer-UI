package com.ahli.hotkeyUi.application.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Class that defines a Hotkey or Settings value to be used in UI.
 * 
 * @author Ahli
 *
 */
public class ValueDef {
	private final SimpleStringProperty id;
	private final SimpleStringProperty value;
	private final SimpleStringProperty description;
	private final SimpleStringProperty defaultValue;

	public ValueDef(String id, String value, String description, String defaultValue) {
		this.id = new SimpleStringProperty(id);
		this.value = new SimpleStringProperty(value);
		this.description = new SimpleStringProperty(description);
		this.defaultValue = new SimpleStringProperty(defaultValue);
	}

	public String getId() {
		return id.get();
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public String getValue() {
		return value.get();
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public String getDefaultValue() {
		return defaultValue.get();
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue.set(defaultValue);
	}

	// required to make UI track changes
	public SimpleStringProperty valueProperty() {
		return value;
	}

	// required to make UI track changes
	public SimpleStringProperty defaultValueProperty() {
		return defaultValue;
	}

	// required to make UI track changes
	public SimpleStringProperty descriptionProperty() {
		return description;
	}

	// required to make UI track changes
	public SimpleStringProperty idProperty() {
		return id;
	}

}
