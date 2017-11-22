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
public class UIAnimation extends UIElement {
	private ArrayList<UIController> controllers = new ArrayList<>();
	private Map<String, UIAttribute> events = new HashMap<>();
	private boolean nextEventsAdditionShouldOverride = false;
	private UIAttribute driver = null;
	
	/**
	 * 
	 * @param name
	 */
	public UIAnimation(final String name) {
		super(name);
	}
	
	/**
	 * @return the controllers
	 */
	public ArrayList<UIController> getControllers() {
		return controllers;
	}
	
	/**
	 * @param controllers
	 *            the controllers to set
	 */
	public void setControllers(final ArrayList<UIController> controllers) {
		this.controllers = controllers;
	}
	
	/**
	 * @return the events
	 */
	public Map<String, UIAttribute> getEvents() {
		return events;
	}
	
	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(final Map<String, UIAttribute> events) {
		this.events = events;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isNextEventsAdditionShouldOverride() {
		return nextEventsAdditionShouldOverride;
	}
	
	/**
	 * 
	 * @param nextEventsAdditionShouldOverride
	 */
	public void setNextEventsAdditionShouldOverride(final boolean nextEventsAdditionShouldOverride) {
		this.nextEventsAdditionShouldOverride = nextEventsAdditionShouldOverride;
	}
	
	/**
	 * @return the driver
	 */
	public UIAttribute getDriver() {
		return driver;
	}
	
	/**
	 * @param driver
	 *            the driver to set
	 */
	public void setDriver(final UIAttribute driver) {
		this.driver = driver;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		if (path == null || path.isEmpty()) {
			// end here
			return this;
		} else {
			// go deeper
			final String curName = UIElement.getLeftPathLevel(path);
			for (final UIElement curElem : controllers) {
				if (curName.equalsIgnoreCase(curElem.getName())) {
					// found right frame -> cut path
					final String newPath = UIElement.removeLeftPathLevel(path);
					return curElem.receiveFrameFromPath(newPath);
				}
			}
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public Object deepClone() {
		final UIAnimation clone = new UIAnimation(name);
		clone.setNextEventsAdditionShouldOverride(nextEventsAdditionShouldOverride);
		
		// clone events
		final Map<String, UIAttribute> clonedEvents = new HashMap<>();
		for (final Entry<String, UIAttribute> entry : events.entrySet()) {
			final UIAttribute clonedValue = (UIAttribute) entry.getValue().deepClone();
			clonedEvents.put(entry.getKey(), clonedValue);
		}
		clone.setEvents(clonedEvents);
		
		// clone controllers
		for (final UIController controller : controllers) {
			clone.getControllers().add((UIController) controller.deepClone());
		}
		
		// clone driver
		if (driver != null) {
			clone.setDriver((UIAttribute) driver.deepClone());
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<Animation name='" + name + "'>";
	}
}
