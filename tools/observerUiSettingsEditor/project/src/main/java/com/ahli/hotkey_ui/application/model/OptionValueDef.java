package com.ahli.hotkey_ui.application.model;

import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.beans.property.SimpleStringProperty;

import java.util.Arrays;
import java.util.Locale;

public class OptionValueDef extends ValueDef {
	
	protected final OptionValueDefType type;
	private final String[] allowedValues;
	private final String[] allowedValuesDisplayNames;
	private final String gamestringsAdd;
	private final SimpleStringProperty value;
	private final SimpleStringProperty oldValue;
	private int selectedIndex;
	private int defaultSelectedIndex;
	private int previouslySelectedIndex;
	
	/**
	 * @param id
	 * @param description
	 * @param defaultValue
	 * @param type
	 * @param allowedValues
	 * @param gamestringsAdd
	 */
	public OptionValueDef(
			final String id,
			final String description,
			final String defaultValue,
			final String type,
			final String[] allowedValues,
			final String[] allowedValuesDisplayNames,
			final String gamestringsAdd) {
		
		super(id, description);
		this.allowedValues = allowedValues;
		if (allowedValues != null) {
			if (allowedValuesDisplayNames != null) {
				if (allowedValuesDisplayNames.length == allowedValues.length) {
					this.allowedValuesDisplayNames = allowedValuesDisplayNames;
				} else {
					this.allowedValuesDisplayNames = Arrays.copyOf(allowedValuesDisplayNames, allowedValues.length);
					// if there are not enough displayNames for each allowedValue => copy them from allowedValues
					if (allowedValues.length >= allowedValuesDisplayNames.length) {
						System.arraycopy(
								allowedValues,
								allowedValuesDisplayNames.length,
								this.allowedValuesDisplayNames,
								allowedValuesDisplayNames.length,
								allowedValues.length - allowedValuesDisplayNames.length);
					}
				}
			} else {
				this.allowedValuesDisplayNames = allowedValues;
			}
		} else {
			this.allowedValuesDisplayNames = null;
		}
		this.gamestringsAdd = gamestringsAdd;
		this.type = determineType(type);
		value = new SimpleStringProperty("");
		oldValue = new SimpleStringProperty("");
		initHasChangedBinding();
		initDefaultValueIndex(defaultValue);
	}
	
	private OptionValueDefType determineType(final String typeStr) {
		try {
			return OptionValueDefType.valueOf(typeStr.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException ignored) {
			return OptionValueDefType.TEXT;
		}
	}
	
	@Override
	protected void initHasChangedBinding() {
		hasChanged.bind(value.isNotEqualTo(oldValue));
	}
	
	private void initDefaultValueIndex(final String defaultValue) {
		for (int i = 0; i < allowedValuesDisplayNames.length; ++i) {
			if (defaultValue.equals(allowedValuesDisplayNames[i])) {
				defaultSelectedIndex = i;
				return;
			}
		}
		try {
			defaultSelectedIndex = Integer.parseInt(defaultValue);
		} catch (final NumberFormatException e) {
			log.error();
			throw new NumberFormatException()
		}
	}
	
	/**
	 * @return
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void setSelectedIndex(final int selectedIndex) {
		this.selectedIndex = selectedIndex;
		value.set(allowedValues[selectedIndex]);
	}
	
	public int getIndexOfDefaultValue() {
		return defaultSelectedIndex;
	}
	
	/**
	 * @return
	 */
	public String getGamestringsAdd() {
		return gamestringsAdd;
	}
	
	public String[] getAllowedValues() {
		return allowedValues;
	}
	
	public String[] getAllowedValuesDisplayNames() {
		return allowedValuesDisplayNames;
	}
	
	/**
	 * Returns whether the value is the default value.
	 *
	 * @return true if the value is the default value
	 */
	@Override
	public boolean isDefaultValue() {
		return selectedIndex == defaultSelectedIndex;
	}
	
}
