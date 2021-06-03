// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import java.util.List;

public interface UIConstant extends UIElement {
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	@Override
	void setName(String name);
	
	/**
	 * @return the value
	 */
	String getValue();
	
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
	boolean equals(Object obj);
	
	@Override
	boolean canEqual(Object other);
	
	@Override
	int hashCode();
}
