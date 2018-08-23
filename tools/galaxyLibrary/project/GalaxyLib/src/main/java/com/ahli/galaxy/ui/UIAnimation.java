package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
@JsonTypeInfo (use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIAnimation extends UIElement {
	private List<UIController> controllers;
	private List<UIAttribute> events;
	//	@JsonInclude (JsonInclude.Include.NON_DEFAULT)
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
			final UIAttribute p = events.get(i);
			clone.events.add((UIAttribute) p.deepCopy());
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
	public List<UIAttribute> getEvents() {
		return events;
	}
	
	/**
	 * @param events
	 * 		the events to set
	 */
	public void setEvents(final List<UIAttribute> events) {
		this.events = events;
	}
	
	/**
	 * @param newEvent
	 */
	public void addEvent(final UIAttribute newEvent) {
		events.add(newEvent);
	}
	
	//	/**
	//	 * @param key
	//	 * @return
	//	 */
	//	public UIAttribute getValue(final String key) {
	//		int i;
	//		Pair<String, UIAttribute> p;
	//		for (i = 0; i < events.size(); i++) {
	//			p = events.get(i);
	//			if (p.getKey().equals(key)) {
	//				return p.getValue();
	//			}
	//		}
	//		return null;
	//	}
	
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
		return new ArrayList<>(controllers);
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
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UIAnimation) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					return false;
				}
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
