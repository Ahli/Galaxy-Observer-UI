package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Ahli
 *
 */
public class UIController extends UIElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5133613746543071378L;
	
	private List<UIAttribute> keys = new ArrayList<>();
	private Map<String, String> values = new HashMap<>();
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
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		final UIController clone = new UIController(getName());
		for (int i = 0; i < keys.size(); i++) {
			clone.keys.add((UIAttribute) keys.get(i).clone());
		}
		final Object[] entries = values.entrySet().toArray();
		for (int fix = 0, i = fix; i < entries.length; i++) {
			@SuppressWarnings("unchecked")
			final Entry<String, String> entry = (Entry<String, String>) entries[i];
			clone.values.put(entry.getKey(), entry.getValue());
		}
		clone.nextAdditionShouldOverride = nextAdditionShouldOverride;
		clone.nameIsImplicit = nameIsImplicit;
		return clone;
	}
	
	/**
	 * @return the keys
	 */
	public List<UIAttribute> getKeys() {
		return keys;
	}
	
	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(final List<UIAttribute> keys) {
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
	
	@Override
	public String toString() {
		return "<Controller name='" + getName() + "'>";
	}
}
