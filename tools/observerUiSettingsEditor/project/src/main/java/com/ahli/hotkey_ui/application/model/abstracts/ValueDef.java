// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.model.abstracts;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

/**
 * Class that defines a Hotkey or Settings value to be used in UI.
 *
 * @author Ahli
 */
public abstract class ValueDef {
	protected final SimpleBooleanProperty hasChanged;
	protected final SimpleStringProperty id;
	protected final SimpleStringProperty description;
	protected final SimpleStringProperty defaultDisplayValue;
	protected final SimpleStringProperty displayValue;
	
	protected ValueDef(final String id, final String description, final String defaultDisplayValue) {
		this.id = new SimpleStringProperty(id);
		this.description = new SimpleStringProperty(description);
		this.defaultDisplayValue = new SimpleStringProperty(defaultDisplayValue);
		displayValue = new SimpleStringProperty("");
		hasChanged = new SimpleBooleanProperty(false);
	}
	
	protected abstract void initHasChangedBinding();
	
	public abstract boolean isDefaultValue();
	
	public String getId() {
		return id.get();
	}
	
	/**
	 * Returns whether the value has changed.
	 *
	 * @return true if the value changed; false if the old value is still current
	 */
	public boolean getHasChanged() {
		return hasChanged.get();
	}
	
	public String getDescription() {
		return description.get();
	}
	
	public abstract void addListener(final ChangeListener<Object> changeListener);
	
	public abstract void resetToDefault();
	
	public abstract void resetToOldValue();
	
	public abstract String getValue();
	
	public abstract String getDisplayValue();
	
	// required to make UI track changes
	public SimpleStringProperty valueProperty() {
		return displayValue;
	}
	
	// required to make UI track changes
	public SimpleStringProperty defaultValueProperty() {
		return defaultDisplayValue;
	}
	
	// required to make UI track changes
	public SimpleStringProperty descriptionProperty() {
		return description;
	}
	
	// required to make UI track changes
	public SimpleStringProperty idProperty() {
		return id;
	}
	
	// required to make UI track changes
	public SimpleBooleanProperty hasChangedProperty() {
		return hasChanged;
	}
	
}
