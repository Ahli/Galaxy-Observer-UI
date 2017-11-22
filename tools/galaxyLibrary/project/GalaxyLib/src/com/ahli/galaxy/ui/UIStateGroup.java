package com.ahli.galaxy.ui;

import java.util.ArrayList;

/**
 * 
 * @author Ahli
 * 
 */
public class UIStateGroup extends UIElement {
	
	private String defaultState = "";
	private ArrayList<UIState> states = new ArrayList<>();
	
	/**
	 * 
	 * @param name
	 */
	public UIStateGroup(final String name) {
		super(name);
	}
	
	/**
	 * @return the defaultState
	 */
	public String getDefaultState() {
		return defaultState;
	}
	
	/**
	 * @param defaultState
	 *            the defaultState to set
	 */
	public void setDefaultState(final String defaultState) {
		this.defaultState = defaultState;
	}
	
	/**
	 * @return the states
	 */
	public ArrayList<UIState> getStates() {
		return states;
	}
	
	/**
	 * @param states
	 *            the states to set
	 */
	public void setStates(final ArrayList<UIState> states) {
		this.states = states;
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
			for (final UIElement curElem : states) {
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
	 */
	@Override
	public Object deepClone() {
		final UIStateGroup clone = new UIStateGroup(name);
		clone.setDefaultState(defaultState);
		
		// clone states
		for (final UIState state : states) {
			clone.getStates().add((UIState) state.deepClone());
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<StateGroup name='" + name + "'>";
	}
}
