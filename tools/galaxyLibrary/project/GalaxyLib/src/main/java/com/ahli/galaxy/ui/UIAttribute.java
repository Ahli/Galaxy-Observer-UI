package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.util.Pair;

import java.util.ArrayList;
import java.util.List;

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
	
	private final List<Pair<String, String>> values;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name) {
		super(name);
		values = new ArrayList<>();
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name, final int initialValuesMaxCapacity) {
		super(name);
		values = new ArrayList<>(initialValuesMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAttribute clone = new UIAttribute(getName(), values.size());
		for (int i = 0, len = values.size(); i < len; i++) {
			final Pair<String, String> p = values.get(i);
			clone.values.add(new Pair<>(p.getKey(), p.getValue()));
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
		final int len;
		Pair<String, String> p = null;
		for (len = values.size(); i < len; i++) {
			p = values.get(i);
			if (p.getKey().equals(key)) {
				break;
			}
		}
		if (i == len) {
			// not found
			values.add(new Pair<>(key, value));
			return null;
		} else {
			// p cannot be null here, ignore linter warning
			final String oldVal = p.getValue();
			p.setValue(value);
			return oldVal;
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getValue(final String key) {
		int i;
		Pair<String, String> p;
		for (i = 0; i < values.size(); i++) {
			p = values.get(i);
			if (p.getKey().equals(key)) {
				return p.getValue();
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
