// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIAnimation;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UIController;
import com.ahli.galaxy.ui.interfaces.UIElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIAnimationMutable extends UIElementAbstract implements UIAnimation {
	private final List<UIElement> controllers;
	private final List<UIAttribute> events;
	private boolean nextEventsAdditionShouldOverride;
	private UIAttribute driver;
	
	/**
	 * Constructor for Kryo
	 */
	private UIAnimationMutable() {
		super(null);
		events = new ArrayList<>(0);
		controllers = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIAnimationMutable(final String name) {
		super(name);
		events = new ArrayList<>(0);
		controllers = new ArrayList<>(0);
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 * @param eventsCapacity
	 * @param controllerCapacity
	 */
	public UIAnimationMutable(final String name, final int eventsCapacity, final int controllerCapacity) {
		super(name);
		events = new ArrayList<>(eventsCapacity);
		controllers = new ArrayList<>(controllerCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAnimationMutable clone = new UIAnimationMutable(getName(), events.size(), controllers.size());
		for (int i = 0, len = controllers.size(); i < len; ++i) {
			clone.controllers.add((UIController) controllers.get(i).deepCopy());
		}
		for (int i = 0, len = events.size(); i < len; ++i) {
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
	@Override
	public List<UIElement> getControllers() {
		return controllers;
	}
	
	/**
	 * @return the events
	 */
	@Override
	public List<UIAttribute> getEvents() {
		return events;
	}
	
	/**
	 * @param newEvent
	 */
	@Override
	public void addEvent(final UIAttribute newEvent) {
		events.add(newEvent);
	}
	
	/**
	 * @return the driver
	 */
	@Override
	public UIAttribute getDriver() {
		return driver;
	}
	
	/**
	 * @param driver
	 * 		the driver to set
	 */
	@Override
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
	@SuppressWarnings("java:S4144")
	public List<UIElement> getChildren() {
		return controllers;
	}
	
	@Override
	@SuppressWarnings("java:S4144")
	public List<UIElement> getChildrenRaw() {
		return controllers;
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final UIAnimationMutable that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return that.canEqual(this) && nextEventsAdditionShouldOverride == that.nextEventsAdditionShouldOverride &&
				Objects.equals(controllers, that.controllers) && Objects.equals(events, that.events) &&
				Objects.equals(driver, that.driver);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIAnimationMutable);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(super.hashCode(), controllers, events, nextEventsAdditionShouldOverride, driver);
	}
}
