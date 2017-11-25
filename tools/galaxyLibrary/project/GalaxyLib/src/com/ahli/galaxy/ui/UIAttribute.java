package com.ahli.galaxy.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 * 
 * @author Ahli
 *
 */
public class UIAttribute extends UIElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5420685675382001338L;
	
	private Map<String, String> values = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Element's name
	 */
	public UIAttribute(final String name) {
		super(name);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		final UIAttribute clone = new UIAttribute(getName());
		final Object[] entries = values.entrySet().toArray();
		for (int fix = 0, i = fix; i < entries.length; i++) {
			@SuppressWarnings("unchecked")
			final Entry<String, String> entry = (Entry<String, String>) entries[i];
			clone.values.put(entry.getKey(), entry.getValue());
		}
		return clone;
	}
	
	/**
	 * @return the values
	 */
	public Map<String, String> getValues() {
		return values;
	}
	
	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(final Map<String, String> values) {
		this.values = values;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<Attribute name='" + getName() + "'>";
	}
}
