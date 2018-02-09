package com.ahli.hotkeyUi.application.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Class that defines a Hotkey or Settings value to be used in UI.
 *
 * @author Ahli
 */
public class ValueDef {
	private final SimpleStringProperty id;
	private final SimpleStringProperty value;
	private final SimpleStringProperty description;
	private final SimpleStringProperty defaultValue;
	
	/**
	 * Constructor.
	 *
	 * @param id
	 * @param value
	 * @param description
	 * @param defaultValue
	 */
	public ValueDef(final String id, final String value, final String description, final String defaultValue) {
		this.id = new SimpleStringProperty(id);
		this.value = new SimpleStringProperty(value);
		this.description = new SimpleStringProperty(description);
		this.defaultValue = new SimpleStringProperty(defaultValue);
	}
	
	/**
	 * @return
	 */
	public String getId() {
		return id.get();
	}
	
	/**
	 * @param id
	 */
	public void setId(final String id) {
		this.id.set(id);
	}
	
	/**
	 * @return
	 */
	public String getValue() {
		return value.get();
	}
	
	/**
	 * @param value
	 */
	public void setValue(final String value) {
		this.value.set(value);
	}
	
	/**
	 * @return
	 */
	public String getDescription() {
		return description.get();
	}
	
	/**
	 * @param description
	 */
	public void setDescription(final String description) {
		this.description.set(description);
	}
	
	/**
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue.get();
	}
	
	/**
	 * @param defaultValue
	 */
	public void setDefaultValue(final String defaultValue) {
		this.defaultValue.set(defaultValue);
	}
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleStringProperty valueProperty() {
		return value;
	}
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleStringProperty defaultValueProperty() {
		return defaultValue;
	}
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleStringProperty descriptionProperty() {
		return description;
	}
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleStringProperty idProperty() {
		return id;
	}
	
}
