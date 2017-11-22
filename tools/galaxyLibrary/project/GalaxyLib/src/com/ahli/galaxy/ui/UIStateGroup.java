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
	public UIStateGroup(String name) {
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
	public void setDefaultState(String defaultState) {
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
	public void setStates(ArrayList<UIState> states) {
		this.states = states;
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
			for (UIElement curElem : states) {
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
	 */
	@Override
	public Object deepClone() {
		UIStateGroup clone = new UIStateGroup(name);
		clone.setDefaultState(defaultState);
		
		// clone states
		for (UIState state : states) {
			clone.getStates().add((UIState) state.deepClone());
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<StateGroup name='" + name + "'>";
	}
}
