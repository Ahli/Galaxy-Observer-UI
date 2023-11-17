// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import java.util.List;

public interface UIStateGroup extends UIElement {
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * @return the defaultState
	 */
	String getDefaultState();
	
	/**
	 * @param defaultState
	 * 		the defaultState to set
	 */
	void setDefaultState(String defaultState);
	
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
	@SuppressWarnings("java:S4144")
	List<UIElement> getChildrenRaw();
	
	@Override
	boolean equals(Object o);
	
//	@Override
//	boolean canEqual(Object other);
	
	@Override
	int hashCode();
}
