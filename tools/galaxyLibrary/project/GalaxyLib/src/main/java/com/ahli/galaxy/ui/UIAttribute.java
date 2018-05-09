package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;

import java.util.ArrayList;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 *
 * @author Ahli
 */
public class UIAttribute extends UIElement {
	
	/**
	 *
	 */
	private static final long serialVersionUID = 5420685675382001338L;
	
	private final ArrayList<String> keyValueList;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name) {
		super(name);
		keyValueList = new ArrayList<>(0);
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name, final int initialValuesMaxCapacity) {
		super(name);
		keyValueList = new ArrayList<>(initialValuesMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAttribute clone = new UIAttribute(getName(), keyValueList.size());
		for (int i = 0, len = keyValueList.size(); i < len; i++) {
			clone.keyValueList.add(keyValueList.get(i));
		}
		return clone;
	}
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 *
	 * @param key
	 * @param value
	 */
	public String addValue(final String key, final String value) {
		int i = 0;
		final int len = keyValueList.size();
		for (; i < len; i += 2) {
			if (keyValueList.get(i).equals(key)) {
				break;
			}
		}
		if (i >= len) {
			// not found
			keyValueList.add(key);
			keyValueList.add(value);
			return null;
		} else {
			return keyValueList.set(i, value);
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getValue(final String key) {
		int i = 0;
		for (final int len = keyValueList.size(); i < len; i += 2) {
			if (keyValueList.get(i).equals(key)) {
				return keyValueList.get(i + 1);
			}
		}
		return null;
	}
	
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<" + getName() + ">";
	}
}
