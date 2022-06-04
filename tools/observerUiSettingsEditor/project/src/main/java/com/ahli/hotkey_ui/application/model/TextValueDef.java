package com.ahli.hotkey_ui.application.model;

import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

import java.util.Locale;

public class TextValueDef extends ValueDef {
	
	private final SimpleStringProperty oldValue;
	private final TextValueDefType type;
	
	public TextValueDef(final String id, final String description, final String defaultValue, final String typeStr) {
		super(id, description, defaultValue);
		oldValue = new SimpleStringProperty("");
		type = determineType(typeStr);
		initHasChangedBinding();
	}
	
	private TextValueDefType determineType(final String typeStr) {
		try {
			return TextValueDefType.valueOf(typeStr.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException ignored) {
			return TextValueDefType.TEXT;
		}
	}
	
	@Override
	protected void initHasChangedBinding() {
		hasChanged.bind(displayValue.isNotEqualTo(oldValue));
	}
	
	public TextValueDef(
			final String id, final String description, final String defaultValue, final TextValueDefType type) {
		super(id, description, defaultValue);
		oldValue = new SimpleStringProperty("");
		this.type = type;
		initHasChangedBinding();
	}
	
	@Override
	public boolean isDefaultValue() {
		return displayValue.get().equals(defaultDisplayValue.get());
	}
	
	@Override
	public void addListener(final ChangeListener<Object> changeListener) {
		displayValue.addListener(changeListener);
	}
	
	public TextValueDefType getType() {
		return type;
	}
	
	@Override
	public void resetToDefault() {
		displayValue.set(defaultDisplayValue.get());
	}
	
	@Override
	public void resetToOldValue() {
		displayValue.set(oldValue.get());
	}
	
	@Override
	public String getValue() {
		return displayValue.get();
	}
	
	public void setValue(final String value) {
		displayValue.set(value);
	}
	
	@Override
	public String getDisplayValue() {
		return displayValue.get();
	}
	
	public void setOldValue(final String oldValue) {
		this.oldValue.set(oldValue);
	}
}
