package application.baseUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Ahli
 *
 */
public class UIAnimation extends UIElement {
	private ArrayList<UIController> controllers = new ArrayList<>();
	private Map<String, UIAttribute> events = new HashMap<>();

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
	 * @param events the events to set
	 */
	public void setEvents(Map<String, UIAttribute> events) {
		this.events = events;
	}

}
