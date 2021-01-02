// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.Locale;

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
	private final String[] allowedValues;
	private final ValueType type;
	
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
		allowedValues = null;
		type = ValueType.TEXT;
	}
	
	/**
	 * Constructor.
	 *
	 * @param id
	 * @param value
	 * @param description
	 * @param defaultValue
	 */
	public ValueDef(
			final String id,
			final String value,
			final String description,
			final String defaultValue,
			final String type,
			final String[] allowedValues) {
		this.id = new SimpleStringProperty(id);
		this.value = new SimpleStringProperty(value);
		this.description = new SimpleStringProperty(description);
		this.defaultValue = new SimpleStringProperty(defaultValue);
		this.allowedValues = allowedValues;
		
		ValueType resultType;
		try {
			resultType = ValueType.valueOf(type.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException e) {
			resultType = ValueType.TEXT;
		}
		this.type = resultType;
		
		if (resultType.equals(ValueType.BOOLEAN) && defaultValue.isEmpty()) {
			setDefaultValue("false");
		}
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
	
	public ValueType getType() {
		return type;
	}
	
	public String[] getAllowedValues() {
		return allowedValues;
	}
	
}
