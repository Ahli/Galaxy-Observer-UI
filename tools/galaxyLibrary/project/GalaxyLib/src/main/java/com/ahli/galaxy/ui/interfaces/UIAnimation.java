// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import java.util.List;

public interface UIAnimation extends UIElement {
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * @return the controllers
	 */
	List<UIElement> getControllers();
	
	/**
	 * @return the events
	 */
	List<UIAttribute> getEvents();
	
	/**
	 * @param newEvent
	 */
	void addEvent(UIAttribute newEvent);
	
	/**
	 * @return the driver
	 */
	UIAttribute getDriver();
	
	/**
	 * @param driver
	 * 		the driver to set
	 */
	void setDriver(UIAttribute driver);
	
	/**
	 * @param path
	 * @return
	 */
	@Override
	UIElement receiveFrameFromPath(String path);
	
	@Override
	String toString();
	
	@Override
	@SuppressWarnings("java:S4144")
	List<UIElement> getChildren();
	
	@Override
	@SuppressWarnings("java:S4144")
	List<UIElement> getChildrenRaw();
	
	@Override
	boolean equals(Object o);
	
	@Override
	int hashCode();
}
