package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ahli
 */
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIStateGroup extends UIElement {
	
	private String defaultState;
	private List<UIState> states;
	
	public UIStateGroup() {
		super(null);
		states = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIStateGroup(final String name) {
		super(name);
		states = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIStateGroup(final String name, final int initialStatesMaxCapacity) {
		super(name);
		states = new ArrayList<>(initialStatesMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIStateGroup clone = new UIStateGroup(getName(), states.size());
		for (int i = 0, len = states.size(); i < len; i++) {
			clone.states.add((UIState) states.get(i).deepCopy());
		}
		clone.defaultState = defaultState;
		return clone;
	}
	
	/**
	 * @return the defaultState
	 */
	public String getDefaultState() {
		return defaultState;
	}
	
	/**
	 * @param defaultState
	 * 		the defaultState to set
	 */
	public void setDefaultState(final String defaultState) {
		this.defaultState = defaultState;
	}
	
	/**
	 * @return the states
	 */
	public List<UIState> getStates() {
		return states;
	}
	
	/**
	 * @param states
	 * 		the states to set
	 */
	public void setStates(final List<UIState> states) {
		this.states = states;
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
			if (curName != null) {
				for (final UIElement curElem : states) {
					if (curName.equalsIgnoreCase(curElem.getName())) {
						// found right frame -> cut path
						final String newPath = UIElement.removeLeftPathLevel(path);
						return curElem.receiveFrameFromPath(newPath);
					}
				}
			}
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "<StateGroup name='" + getName() + "'>";
	}
}
