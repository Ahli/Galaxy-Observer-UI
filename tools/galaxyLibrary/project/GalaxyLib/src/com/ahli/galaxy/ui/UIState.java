package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ahli
 */
public class UIState extends UIElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2094212167992593460L;
	
	private List<UIAttribute> whens = null;
	private List<UIAttribute> actions = null;
	private boolean nextAdditionShouldOverrideWhens = false;
	private boolean nextAdditionShouldOverrideActions = false;
	
	/**
	 * @param name
	 */
	public UIState(final String name) {
		super(name);
		whens = new ArrayList<>();
		actions = new ArrayList<>();
	}
	
	/**
	 * @param name
	 * @param initialWhensCapacity
	 * @param initialActionsCapacity
	 */
	public UIState(final String name, final int initialWhensCapacity, final int initialActionsCapacity) {
		super(name);
		whens = new ArrayList<>(initialWhensCapacity);
		actions = new ArrayList<>(initialActionsCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIState clone = new UIState(getName(), whens.size(), actions.size());
		final List<UIAttribute> whensClone = clone.whens;
		for (int i = 0; i < whens.size(); i++) {
			whensClone.add((UIAttribute) whens.get(i).deepCopy());
		}
		final List<UIAttribute> actionsClone = clone.actions;
		for (int i = 0; i < actions.size(); i++) {
			actionsClone.add((UIAttribute) actions.get(i).deepCopy());
		}
		clone.nextAdditionShouldOverrideActions = nextAdditionShouldOverrideActions;
		clone.nextAdditionShouldOverrideWhens = nextAdditionShouldOverrideWhens;
		return clone;
	}
	
	/**
	 * @return the whens
	 */
	public List<UIAttribute> getWhens() {
		return whens;
	}
	
	/**
	 * @param whens
	 *            the whens to set
	 */
	public void setWhens(final List<UIAttribute> whens) {
		this.whens = whens;
	}
	
	/**
	 * @return the actions
	 */
	public List<UIAttribute> getActions() {
		return actions;
	}
	
	/**
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(final List<UIAttribute> actions) {
		this.actions = actions;
	}
	
	/**
	 * @return the nextAdditionShouldOverrideWhens
	 */
	public boolean isNextAdditionShouldOverrideWhens() {
		return nextAdditionShouldOverrideWhens;
	}
	
	/**
	 * @param nextAdditionShouldOverrideWhens
	 *            the nextAdditionShouldOverrideWhens to set
	 */
	public void setNextAdditionShouldOverrideWhens(final boolean nextAdditionShouldOverrideWhens) {
		this.nextAdditionShouldOverrideWhens = nextAdditionShouldOverrideWhens;
	}
	
	/**
	 * @return the nextAdditionShouldOverrideActions
	 */
	public boolean isNextAdditionShouldOverrideActions() {
		return nextAdditionShouldOverrideActions;
	}
	
	/**
	 * @param nextAdditionShouldOverrideActions
	 *            the nextAdditionShouldOverrideActions to set
	 */
	public void setNextAdditionShouldOverrideActions(final boolean nextAdditionShouldOverrideActions) {
		this.nextAdditionShouldOverrideActions = nextAdditionShouldOverrideActions;
	}
	
	/**
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<State>";
	}
}
