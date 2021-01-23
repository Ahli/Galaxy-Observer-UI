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
	 * @return
	 */
	boolean isNextEventsAdditionShouldOverride();
	
	/**
	 * @param nextEventsAdditionShouldOverride
	 */
	void setNextEventsAdditionShouldOverride(boolean nextEventsAdditionShouldOverride);
	
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
	boolean canEqual(Object other);
	
	@Override
	int hashCode();
}
