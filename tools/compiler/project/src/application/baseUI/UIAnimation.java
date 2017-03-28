package application.baseUI;

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
			for (UIElement curElem : controllers) {
				if (path.equalsIgnoreCase(curElem.getName())) {
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
	 */
	@Override
	public Object clone() {
		UIAnimation clone = (UIAnimation) super.clone();
		clone.setNextEventsAdditionShouldOverride(nextEventsAdditionShouldOverride);

		// clone events
		Map<String, UIAttribute> clonedEvents = new HashMap<>();
		for (Entry<String, UIAttribute> entry : events.entrySet()) {
			UIAttribute clonedValue = (UIAttribute) entry.getValue().clone();
			clonedEvents.put(entry.getKey(), clonedValue);
		}
		clone.setEvents(clonedEvents);

		// clone controllers
		for (UIController controller : controllers) {
			clone.getControllers().add((UIController) controller.clone());
		}

		return clone;
	}
}
