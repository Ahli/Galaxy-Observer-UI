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
	public UIAnimation(String name) {
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
	public void setControllers(ArrayList<UIController> controllers) {
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
	public void setEvents(Map<String, UIAttribute> events) {
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
	public void setNextEventsAdditionShouldOverride(boolean nextEventsAdditionShouldOverride) {
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
	public void setDriver(UIAttribute driver) {
		this.driver = driver;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(String path) {
		if (path == null || path.isEmpty()) {
			// end here
			return this;
		} else {
			// go deeper
			String curName = UIElement.getLeftPathLevel(path);
			for (UIElement curElem : controllers) {
				if (curName.equalsIgnoreCase(curElem.getName())) {
					// found right frame -> cut path
					String newPath = UIElement.removeLeftPathLevel(path);
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
		UIAnimation clone = new UIAnimation(name);
		clone.setNextEventsAdditionShouldOverride(nextEventsAdditionShouldOverride);
		
		// clone events
		Map<String, UIAttribute> clonedEvents = new HashMap<>();
		for (Entry<String, UIAttribute> entry : events.entrySet()) {
			UIAttribute clonedValue = (UIAttribute) entry.getValue().deepClone();
			clonedEvents.put(entry.getKey(), clonedValue);
		}
		clone.setEvents(clonedEvents);
		
		// clone controllers
		for (UIController controller : controllers) {
			clone.getControllers().add((UIController) controller.deepClone());
		}
		
		// clone driver
		if (driver != null) {
			clone.setDriver((UIAttribute) driver.deepClone());
		}
		
		return clone;
	}
}
