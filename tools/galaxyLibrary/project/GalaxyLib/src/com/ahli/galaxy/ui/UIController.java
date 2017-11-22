package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Ahli
 *
 */
public class UIController extends UIElement {
	private Map<String, String> values = new HashMap<>();
	private ArrayList<UIAttribute> keys = new ArrayList<>();
	private boolean nextAdditionShouldOverride = false;
	private boolean nameIsImplicit = true;
	
	/**
	 * 
	 * @param name
	 */
	public UIController(final String name) {
		super(name);
	}
	
	/**
	 * @return the keys
	 */
	public ArrayList<UIAttribute> getKeys() {
		return keys;
	}
	
	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(final ArrayList<UIAttribute> keys) {
		this.keys = keys;
	}
	
	/**
	 * @return the nextAdditionShouldOverride
	 */
	public boolean isNextAdditionShouldOverride() {
		return nextAdditionShouldOverride;
	}
	
	/**
	 * @param nextAdditionShouldOverride
	 *            the nextAdditionShouldOverride to set
	 */
	public void setNextAdditionShouldOverride(final boolean nextAdditionShouldOverride) {
		this.nextAdditionShouldOverride = nextAdditionShouldOverride;
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
	 * @return the nameIsImplicit
	 */
	public boolean isNameIsImplicit() {
		return nameIsImplicit;
	}
	
	/**
	 * @param nameIsImplicit
	 *            the nameIsImplicit to set
	 */
	public void setNameIsImplicit(final boolean nameIsImplicit) {
		this.nameIsImplicit = nameIsImplicit;
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
	
	/**
	 * 
	 * @return
	 */
	@Override
	public Object deepClone() {
		final UIController clone = new UIController(name);
		clone.setNameIsImplicit(nameIsImplicit);
		clone.setNextAdditionShouldOverride(nextAdditionShouldOverride);
		
		// clone values
		final Map<String, String> clonedValues = new HashMap<>();
		for (final Entry<String, String> entry : values.entrySet()) {
			clonedValues.put(entry.getKey(), entry.getValue());
		}
		clone.setValues(clonedValues);
		
		// clone keys
		for (final UIElement key : keys) {
			clone.getKeys().add((UIAttribute) key.deepClone());
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<Controller name='" + name + "'>";
	}
}
