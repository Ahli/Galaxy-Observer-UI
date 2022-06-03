// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.model.abstracts;

import com.ahli.hotkey_ui.application.model.TextValueDefType;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Locale;

/**
 * Class that defines a Hotkey or Settings value to be used in UI.
 *
 * @author Ahli
 */
public abstract class ValueDef {
	protected final SimpleBooleanProperty hasChanged;
	private final String id;
	private final String description;
	
	protected ValueDef(final String id, final String description) {
		this.id = id;
		this.description = description;
		hasChanged = new SimpleBooleanProperty(false);
		initHasChangedBinding();
	}
	
	private TextValueDefType determineType(final String typeStr) {
		try {
			return TextValueDefType.valueOf(typeStr.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException ignored) {
			return TextValueDefType.TEXT;
		}
	}
	
	protected abstract void initHasChangedBinding();
	
	protected ValueDef(final String id, final String description, final TextValueDefType type) {
		this.id = id;
		this.description = description;
		this.type = type;
		hasChanged = new SimpleBooleanProperty(false);
		initHasChangedBinding();
	}
	
	public abstract boolean isDefaultValue();
	
	public String getId() {
		return id.get();
	}
	
	public void setValue(final String value) {
		this.value.set(value);
	}
	
	public String getDefaultValue() {
		return defaultValue.get();
	}
	
	// required to make UI track changes
	public SimpleStringProperty valueProperty() {
		return value;
	}
	
	public TextValueDefType getType() {
		return type;
	}
	
	/**
	 * Returns whether the value has changed.
	 *
	 * @return true if the value changed; false if the old value is still current
	 */
	public boolean hasChanged() {
		return hasChanged.get();
	}
	
	
	public void setOldValue(final String val) {
		defaultValue.set(val);
	}
}
