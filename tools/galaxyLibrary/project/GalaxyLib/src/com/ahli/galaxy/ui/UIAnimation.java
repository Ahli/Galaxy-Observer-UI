package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.List;

import com.ahli.util.Pair;

/**
 * 
 * @author Ahli
 * 
 */
public class UIAnimation extends UIElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7493401910318905210L;
	
	private List<UIController> controllers = null;
	// private Map<String, UIAttribute> events = new HashMap<>();
	private ArrayList<Pair<String, UIAttribute>> events = null;
	private boolean nextEventsAdditionShouldOverride = false;
	private UIAttribute driver = null;
	
	/**
	 * 
	 * @param name
	 */
	public UIAnimation(final String name) {
		super(name);
		events = new ArrayList<>(2);
		controllers = new ArrayList<>(1);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Element's name
	 */
	public UIAnimation(final String name, final int minEventsCapacity, final int minControllerCapacity) {
		super(name);
		// values = new HashMap<>(minAttributeCapacity, 1);
		events = new ArrayList<>(minEventsCapacity);
		controllers = new ArrayList<>(minControllerCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		final UIAnimation clone = new UIAnimation(getName(), events.size(), controllers.size());
		for (int i = 0; i < controllers.size(); i++) {
			clone.controllers.add((UIController) controllers.get(i).clone());
		}
		// final Object[] entries = events.entrySet().toArray();
		// for (int fix = 0, i = fix; i < entries.length; i++) {
		// @SuppressWarnings("unchecked")
		// final Entry<String, UIAttribute> entry = (Entry<String, UIAttribute>)
		// entries[i];
		// clone.events.put(entry.getKey(), (UIAttribute) entry.getValue().clone());
		// }
		for (int i = 0; i < events.size(); i++) {
			final Pair<String, UIAttribute> p = events.get(i);
			clone.events.add(new Pair<>(p.getKey(), (UIAttribute) p.getValue().clone()));
		}
		clone.nextEventsAdditionShouldOverride = nextEventsAdditionShouldOverride;
		if (driver != null) {
			clone.driver = (UIAttribute) driver.clone();
		}
		return clone;
	}
	
	/**
	 * @return the controllers
	 */
	public List<UIController> getControllers() {
		return controllers;
	}
	
	/**
	 * @param controllers
	 *            the controllers to set
	 */
	public void setControllers(final List<UIController> controllers) {
		this.controllers = controllers;
	}
	
	// /**
	// * @return the events
	// */
	// public Map<String, UIAttribute> getEvents() {
	// return events;
	// }
	//
	// /**
	// * @param events
	// * the events to set
	// */
	// public void setEvents(final Map<String, UIAttribute> events) {
	// this.events = events;
	// }
	
	/**
	 * @return the events
	 */
	public ArrayList<Pair<String, UIAttribute>> getEvents() {
		return events;
	}
	
	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(final ArrayList<Pair<String, UIAttribute>> events) {
		this.events = events;
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public UIAttribute addEvent(final String key, final UIAttribute value) {
		final Pair<String, UIAttribute> newPair = new Pair<>(key, value);
		final int i = events.indexOf(newPair);
		if (i == -1) {
			events.add(newPair);
			return null;
		} else {
			return events.set(i, newPair).getValue();
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public UIAttribute getValue(final String key) {
		int i;
		Pair<String, UIAttribute> p = null;
		for (i = 0; i < events.size(); i++) {
			p = events.get(i);
			if (p.getKey().equals(key)) {
				return p.getValue();
			}
		}
		return null;
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
	
	@Override
	public String toString() {
		return "<Animation name='" + getName() + "'>";
	}
}
