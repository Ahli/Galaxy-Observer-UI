package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.List;

import com.ahli.util.Pair;

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
	
	// private final Map<String, String> values;
	private final List<Pair<String, String>> values;
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Element's name
	 */
	public UIAttribute(final String name) {
		super(name);
		// values = new THashMap<>(1, 1f);
		values = new ArrayList<>();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Element's name
	 */
	public UIAttribute(final String name, final int initialValuesMaxCapacity) {
		super(name);
		// values = new THashMap<>(initialValuesMaxCapacity, 1f);
		values = new ArrayList<>(initialValuesMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAttribute clone = new UIAttribute(getName(), values.size());
		// final Map<String, String> clonedMap = clone.values;
		// final Object[] entries = values.entrySet().toArray();
		// for (int fix = 0, i = fix; i < entries.length; i++) {
		// @SuppressWarnings("unchecked")
		// final Entry<String, String> entry = (Entry<String, String>) entries[i];
		// clonedMap.put(entry.getKey(), entry.getValue());
		// }
		for (int i = 0, len = values.size(); i < len; i++) {
			final Pair<String, String> p = values.get(i);
			clone.values.add(new Pair<>(p.getKey(), p.getValue()));
		}
		return clone;
	}
	
	// /**
	// * @return the values
	// */
	// public Map<String, String> getValues() {
	// return values;
	// }
	//
	// /**
	// * @param values
	// * the values to set
	// */
	// public void setValues(final Map<String, String> values) {
	// this.values = values;
	// }
	
	// /**
	// * @return the values
	// */
	// public List<Pair<String, String>> getValues() {
	// return values;
	// }
	
	// /**
	// * @param values
	// * the values to set
	// */
	// public void setValues(final ArrayList<Pair<String, String>> values) {
	// this.values = values;
	// }
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 * 
	 * @param key
	 * @param value
	 */
	public String addValue(final String key, final String value) {
		final Pair<String, String> newPair = new Pair<>(key, value);
		final int i = values.indexOf(newPair);
		if (i == -1) {
			values.add(newPair);
			return null;
		} else {
			return values.set(i, newPair).getValue();
		}
		
		// return values.put(key, value);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getValue(final String key) {
		int i;
		Pair<String, String> p = null;
		for (i = 0; i < values.size(); i++) {
			p = values.get(i);
			if (p.getKey().equals(key)) {
				return p.getValue();
			}
		}
		return null;
		
		// return values.get(key);
	}
	
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<Attribute name='" + getName() + "'>";
	}
}
