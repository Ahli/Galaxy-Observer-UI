package com.ahli.galaxy.ui.interfaces;

import java.util.List;

public interface UIState extends UIElement {
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	Object deepCopy();
	
	/**
	 * @return the whens
	 */
	List<UIAttribute> getWhens();
	
	/**
	 * @param whens
	 * 		the whens to set
	 */
	void setWhens(List<UIAttribute> whens);
	
	/**
	 * @return the actions
	 */
	List<UIAttribute> getActions();
	
	/**
	 * @param actions
	 * 		the actions to set
	 */
	void setActions(List<UIAttribute> actions);
	
	/**
	 * @return the nextAdditionShouldOverrideWhens
	 */
	boolean isNextAdditionShouldOverrideWhens();
	
	/**
	 * @param nextAdditionShouldOverrideWhens
	 * 		the nextAdditionShouldOverrideWhens to set
	 */
	void setNextAdditionShouldOverrideWhens(boolean nextAdditionShouldOverrideWhens);
	
	/**
	 * @return the nextAdditionShouldOverrideActions
	 */
	boolean isNextAdditionShouldOverrideActions();
	
	/**
	 * @param nextAdditionShouldOverrideActions
	 * 		the nextAdditionShouldOverrideActions to set
	 */
	void setNextAdditionShouldOverrideActions(boolean nextAdditionShouldOverrideActions);
	
	/**
	 * @param path
	 * @return
	 */
	@Override
	UIElement receiveFrameFromPath(String path);
	
	@Override
	String toString();
	
	@Override
	List<UIElement> getChildren();
	
	@Override
	@SuppressWarnings("java:S1168")
	List<UIElement> getChildrenRaw();
	
	@Override
	boolean equals(Object o);
	
	@Override
	boolean canEqual(Object other);
	
	@Override
	int hashCode();
}
