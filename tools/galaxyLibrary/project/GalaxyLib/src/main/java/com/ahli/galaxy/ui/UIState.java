// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIState extends UIElement {
	
	private List<UIAttribute> whens;
	private List<UIAttribute> actions;
	private boolean nextAdditionShouldOverrideWhens;
	private boolean nextAdditionShouldOverrideActions;
	
	public UIState() {
		super(null);
		whens = new ArrayList<>(0);
		actions = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIState(final String name) {
		super(name);
		whens = new ArrayList<>(0);
		actions = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 * @param whensCapacity
	 * @param actionsCapacity
	 */
	public UIState(final String name, final int whensCapacity, final int actionsCapacity) {
		super(name);
		whens = new ArrayList<>(whensCapacity);
		actions = new ArrayList<>(actionsCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIState clone = new UIState(getName(), whens.size(), actions.size());
		final List<UIAttribute> whensClone = clone.whens;
		for (final UIAttribute when : whens) {
			whensClone.add((UIAttribute) when.deepCopy());
		}
		final List<UIAttribute> actionsClone = clone.actions;
		for (final UIAttribute action : actions) {
			actionsClone.add((UIAttribute) action.deepCopy());
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
	 * 		the whens to set
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
	 * 		the actions to set
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
	 * 		the nextAdditionShouldOverrideWhens to set
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
	 * 		the nextAdditionShouldOverrideActions to set
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
		return "<State name='" + getName() + "'>";
	}
	
	@Override
	public List<UIElement> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public List<UIElement> getChildrenRaw() {
		return null; // returning null is desired here
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UIState)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final UIState uiState = (UIState) o;
		return uiState.canEqual(this) && nextAdditionShouldOverrideWhens == uiState.nextAdditionShouldOverrideWhens &&
				nextAdditionShouldOverrideActions == uiState.nextAdditionShouldOverrideActions &&
				Objects.equals(whens, uiState.whens) && Objects.equals(actions, uiState.actions);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIState);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(super.hashCode(),
				whens,
				actions,
				nextAdditionShouldOverrideWhens,
				nextAdditionShouldOverrideActions);
	}
}
