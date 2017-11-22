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
	
	public UIAttribute(String name) {
		super(name);
	}
	
	private Map<String, String> values = new HashMap<>();
	
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
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public Object deepClone() {
		UIAttribute clone = new UIAttribute(name);
		
		// clone values
		Map<String, String> clonedValues = new HashMap<>();
		for (Entry<String, String> entry : values.entrySet()) {
			clonedValues.put(entry.getKey(), entry.getValue());
		}
		clone.setValues(clonedValues);
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<Attribute name='" + name + "'>";
	}
}
