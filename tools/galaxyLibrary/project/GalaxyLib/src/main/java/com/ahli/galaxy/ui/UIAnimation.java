package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.util.Pair;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIAnimation extends UIElement {
	private List<UIController> controllers;
	private List<Pair<String, UIAttribute>> events;
	@JsonInclude (JsonInclude.Include.NON_DEFAULT)
	private boolean nextEventsAdditionShouldOverride;
	private UIAttribute driver;
	
	public UIAnimation() {
		super(null);
		events = new ArrayList<>(0);
		controllers = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIAnimation(final String name) {
		super(name);
		events = new ArrayList<>(0);
		controllers = new ArrayList<>(0);
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAnimation(final String name, final int minEventsCapacity, final int minControllerCapacity) {
		super(name);
		events = new ArrayList<>(minEventsCapacity);
		controllers = new ArrayList<>(minControllerCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAnimation clone = new UIAnimation(getName(), events.size(), controllers.size());
		for (int i = 0, len = controllers.size(); i < len; i++) {
			clone.controllers.add((UIController) controllers.get(i).deepCopy());
		}
		for (int i = 0, len = events.size(); i < len; i++) {
			final Pair<String, UIAttribute> p = events.get(i);
			clone.events.add(new Pair<>(p.getKey(), (UIAttribute) p.getValue().deepCopy()));
		}
		clone.nextEventsAdditionShouldOverride = nextEventsAdditionShouldOverride;
		if (driver != null) {
			clone.driver = (UIAttribute) driver.deepCopy();
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
	 * 		the controllers to set
	 */
	public void setControllers(final List<UIController> controllers) {
		this.controllers = controllers;
	}
	
	/**
	 * @return the events
	 */
	public List<Pair<String, UIAttribute>> getEvents() {
		return events;
	}
	
	/**
	 * @param events
	 * 		the events to set
	 */
	public void setEvents(final List<Pair<String, UIAttribute>> events) {
		this.events = events;
	}
	
	/**
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
	 * @param key
	 * @return
	 */
	public UIAttribute getValue(final String key) {
		int i;
		Pair<String, UIAttribute> p;
		for (i = 0; i < events.size(); i++) {
			p = events.get(i);
			if (p.getKey().equals(key)) {
				return p.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public boolean isNextEventsAdditionShouldOverride() {
		return nextEventsAdditionShouldOverride;
	}
	
	/**
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
	 * 		the driver to set
	 */
	public void setDriver(final UIAttribute driver) {
		this.driver = driver;
	}
	
	/**
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
	
	@Override
	public List<UIElement> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UIAnimation)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final UIAnimation that = (UIAnimation) obj;
		for (int i = 0; i < getSignatureFields().length; i++) {
			if (!Objects.equals(getSignatureFields()[i], that.getSignatureFields()[i])) {
				return false;
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { getName(), controllers, events, nextEventsAdditionShouldOverride, driver };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
