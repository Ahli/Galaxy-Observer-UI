package com.ahli.hotkey_ui.application.model;

import com.ahli.hotkey_ui.application.model.abstracts.ValueDef;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

import java.util.Arrays;
import java.util.Locale;

public class OptionValueDef extends ValueDef {
	
	protected final OptionValueDefType type;
	private final String[] allowedValues;
	private final String[] allowedDisplayValues;
	private final String gamestringsAdd;
	private final SimpleIntegerProperty selectedIndex;
	private final SimpleIntegerProperty defaultSelectedIndex;
	private final SimpleIntegerProperty oldSelectedIndex;
	
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
		
		super(id, description, defaultValue);
		
		this.type = determineType(type);
		
		if (this.type == OptionValueDefType.BOOLEAN) {
			this.allowedValues = new String[] { Constants.FALSE, Constants.TRUE };
			allowedDisplayValues = this.allowedValues;
		} else {
			this.allowedValues = allowedValues;
			if (allowedValues != null) {
				if (allowedValuesDisplayNames != null) {
					if (allowedValuesDisplayNames.length == allowedValues.length) {
						allowedDisplayValues = allowedValuesDisplayNames;
					} else {
						allowedDisplayValues = Arrays.copyOf(allowedValuesDisplayNames, allowedValues.length);
						// if there are not enough displayNames for each allowedValue => copy them from allowedValues
						if (allowedValues.length >= allowedValuesDisplayNames.length) {
							System.arraycopy(
									allowedValues,
									allowedValuesDisplayNames.length,
									allowedDisplayValues,
									allowedValuesDisplayNames.length,
									allowedValues.length - allowedValuesDisplayNames.length);
						}
					}
				} else {
					allowedDisplayValues = allowedValues;
				}
			} else {
				allowedDisplayValues = null;
			}
		}
		
		this.gamestringsAdd = gamestringsAdd;
		selectedIndex = new SimpleIntegerProperty();
		defaultSelectedIndex = new SimpleIntegerProperty();
		oldSelectedIndex = new SimpleIntegerProperty();
		initHasChangedBinding();
		initDefaultValueIndex(defaultValue);
	}
	
	private OptionValueDefType determineType(final String typeStr) {
		try {
			return OptionValueDefType.valueOf(typeStr.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException _) {
			return OptionValueDefType.TEXT;
		}
	}
	
	@Override
	protected void initHasChangedBinding() {
		hasChanged.bind(selectedIndex.isNotEqualTo(oldSelectedIndex));
	}
	
	private void initDefaultValueIndex(final String defaultValue) {
		for (int i = 0; i < allowedValues.length; ++i) {
			if (defaultValue.equals(allowedValues[i])) {
				defaultSelectedIndex.set(i);
				defaultDisplayValue.set(allowedDisplayValues[defaultSelectedIndex.get()]);
				return;
			}
		}
		for (int i = 0; i < allowedDisplayValues.length; ++i) {
			if (defaultValue.equals(allowedDisplayValues[i])) {
				defaultSelectedIndex.set(i);
				defaultDisplayValue.set(allowedDisplayValues[defaultSelectedIndex.get()]);
				return;
			}
		}
		defaultSelectedIndex.set(Integer.parseInt(defaultValue));
		defaultDisplayValue.set(allowedDisplayValues[defaultSelectedIndex.get()]);
	}
	
	public int getSelectedIndex() {
		return selectedIndex.get();
	}
	
	public void setSelectedIndex(final int selectedIndex) {
		this.selectedIndex.set(selectedIndex);
	}
	
	public String getGamestringsAdd() {
		return gamestringsAdd;
	}
	
	public String[] getAllowedDisplayValues() {
		return allowedDisplayValues;
	}
	
	/**
	 * Returns whether the value is the default value.
	 *
	 * @return true if the value is the default value
	 */
	@Override
	public boolean isDefaultValue() {
		return selectedIndex.get() == defaultSelectedIndex.get();
	}
	
	public OptionValueDefType getType() {
		return type;
	}
	
	@Override
	public void addListener(final ChangeListener<Object> changeListener) {
		selectedIndex.addListener(changeListener);
	}
	
	@Override
	public String getValue() {
		return allowedValues[selectedIndex.get()];
	}
	
	public String getSelectedValue() {
		return allowedValues[selectedIndex.get()];
	}
	
	public void setSelectedValue(final String value) {
		for (int i = 0; i < allowedValues.length; ++i) {
			if (value.equals(allowedValues[i])) {
				selectedIndex.set(i);
				updateDisplayValue();
				return;
			}
		}
		for (int i = 0; i < allowedDisplayValues.length; ++i) {
			if (value.equals(allowedDisplayValues[i])) {
				selectedIndex.set(i);
				updateDisplayValue();
				return;
			}
		}
		selectedIndex.set(Integer.parseInt(value));
		updateDisplayValue();
	}
	
	private void updateDisplayValue() {
		displayValue.set(allowedDisplayValues[selectedIndex.get()]);
	}
	
	public void setOldSelectedValue(final String oldValue) {
		for (int i = 0; i < allowedValues.length; ++i) {
			if (oldValue.equals(allowedValues[i])) {
				oldSelectedIndex.set(i);
				return;
			}
		}
		for (int i = 0; i < allowedDisplayValues.length; ++i) {
			if (oldValue.equals(allowedDisplayValues[i])) {
				oldSelectedIndex.set(i);
				return;
			}
		}
		oldSelectedIndex.set(Integer.parseInt(oldValue));
	}
	
	@Override
	public void resetToDefault() {
		selectedIndex.set(defaultSelectedIndex.get());
		updateDisplayValue();
	}
	
	@Override
	public void resetToOldValue() {
		selectedIndex.set(oldSelectedIndex.get());
		updateDisplayValue();
	}
	
	@Override
	public String getDisplayValue() {
		return allowedDisplayValues[selectedIndex.get()];
	}
	
	public SimpleIntegerProperty getSelectedIndexProperty() {
		return selectedIndex;
	}
	
}
