// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.model;

import javafx.beans.property.SimpleBooleanProperty;
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
	private final SimpleStringProperty oldValue;
	private final SimpleBooleanProperty hasChanged;
	private final String[] allowedValues;
	private final ValueType type;
	private final String gamestringsAdd;
	
	/**
	 * Constructor.
	 *
	 * @param id
	 * @param description
	 * @param defaultValue
	 */
	public ValueDef(final String id, final String description, final String defaultValue) {
		this.id = new SimpleStringProperty(id);
		value = new SimpleStringProperty("");
		this.description = new SimpleStringProperty(description);
		this.defaultValue = new SimpleStringProperty(defaultValue);
		allowedValues = null;
		gamestringsAdd = "";
		type = ValueType.TEXT;
		oldValue = new SimpleStringProperty("");
		hasChanged = new SimpleBooleanProperty(false);
		initHasChangedBinding();
	}
	
	private void initHasChangedBinding() {
		hasChanged.bind(value.isNotEqualTo(oldValue));
	}
	
	/**
	 * @param id
	 * @param description
	 * @param defaultValue
	 * @param type
	 * @param allowedValues
	 * @param gamestringsAdd
	 */
	public ValueDef(
			final String id,
			final String description,
			final String defaultValue,
			final String type,
			final String[] allowedValues,
			final String gamestringsAdd) {
		this.id = new SimpleStringProperty(id);
		value = new SimpleStringProperty("");
		this.description = new SimpleStringProperty(description);
		this.defaultValue = new SimpleStringProperty(defaultValue);
		this.allowedValues = allowedValues;
		this.gamestringsAdd = gamestringsAdd;
		this.type = determineType(type);
		oldValue = new SimpleStringProperty("");
		hasChanged = new SimpleBooleanProperty(false);
		initHasChangedBinding();
		initDefaultValue(defaultValue);
	}
	
	private ValueType determineType(final String typeStr) {
		try {
			return ValueType.valueOf(typeStr.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException ignored) {
			return ValueType.TEXT;
		}
	}
	
	private void initDefaultValue(final String defaultValue) {
		if (type == ValueType.BOOLEAN && defaultValue.isEmpty()) {
			setDefaultValue(Constants.FALSE);
		}
	}
	
	public String getOldValue() {
		return oldValue.get();
	}
	
	public void setOldValue(final String oldValue) {
		this.oldValue.set(oldValue);
	}
	
	/**
	 * @return
	 */
	public String getGamestringsAdd() {
		return gamestringsAdd;
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
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleBooleanProperty hasChangedProperty() {
		return hasChanged;
	}
	
	/**
	 * @return
	 */
	// required to make UI track changes
	public SimpleStringProperty oldValueProperty() {
		return oldValue;
	}
	
	/**
	 * @return
	 */
	public ValueType getType() {
		return type;
	}
	
	/**
	 * @return
	 */
	public String[] getAllowedValues() {
		return allowedValues;
	}
	
	/**
	 * Returns whether the value has changed.
	 *
	 * @return true if the value changed; false if the old value is still current
	 */
	public boolean hasChanged() {
		return hasChanged.get();
	}
}
