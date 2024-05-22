// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import java.util.List;

public interface UIController extends UIElement {
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * @return the keys
	 */
	List<UIAttribute> getKeys();
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 *
	 * @param key
	 * @param value
	 */
	void addValue(String key, String value);
	
	/**
	 * @param key
	 * @return
	 */
	String getValue(String key);
	
	/**
	 * @param nameIsImplicit
	 * 		the nameIsImplicit to set
	 */
	void setNameIsImplicit(boolean nameIsImplicit);
	
	/**
	 * @param path
	 * @return
	 */
	@Override
	UIElement receiveFrameFromPath(String path);
	
	@Override
	String toString();
	
	@Override
	List<UIElement> getChildren();
	
	@Override
	@SuppressWarnings("java:S1168")
	List<UIElement> getChildrenRaw();
	
	@Override
	boolean equals(Object o);
	
	@Override
	int hashCode();
}
