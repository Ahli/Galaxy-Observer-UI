package com.ahli.hotkey_ui.application.model;

import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.beans.property.SimpleStringProperty;

import java.util.Locale;

public class TextValueDef extends ValueDef {
	
	private final SimpleStringProperty value;
	private final SimpleStringProperty defaultValue;
	private final SimpleStringProperty oldValue;
	private final TextValueDefType type;
	
	/**
	 * Constructor.
	 *
	 * @param id
	 * @param description
	 * @param defaultValue
	 */
	protected TextValueDef(final String id, final String description, final String defaultValue, final String typeStr) {
		super(id, description, TextValueDefType.TEXT);
		value = new SimpleStringProperty("");
		oldValue = new SimpleStringProperty("");
		this.defaultValue = new SimpleStringProperty(defaultValue);
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
		hasChanged.bind(value.isNotEqualTo(oldValue));
	}
	
	@Override
	public boolean isDefaultValue() {
		return value.get().equals(defaultValue.get());
	}
}
