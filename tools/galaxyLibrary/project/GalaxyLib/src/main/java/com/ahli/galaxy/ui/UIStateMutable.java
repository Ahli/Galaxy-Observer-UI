// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIStateMutable extends UIElementAbstract implements UIState {
	
	private final List<UIAttribute> whens;
	private final List<UIAttribute> actions;
	private boolean nextAdditionShouldOverrideWhens;
	private boolean nextAdditionShouldOverrideActions;
	
	/**
	 * Constructor for Kryo
	 */
	private UIStateMutable() {
		super(null);
		whens = new ArrayList<>(0);
		actions = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIStateMutable(final String name) {
		super(name);
		whens = new ArrayList<>(0);
		actions = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 * @param whensCapacity
	 * @param actionsCapacity
	 */
	public UIStateMutable(final String name, final int whensCapacity, final int actionsCapacity) {
		super(name);
		whens = new ArrayList<>(whensCapacity);
		actions = new ArrayList<>(actionsCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIStateMutable clone = new UIStateMutable(getName(), whens.size(), actions.size());
		for (final UIAttribute when : whens) {
			clone.whens.add((UIAttribute) when.deepCopy());
		}
		for (final UIAttribute action : actions) {
			clone.actions.add((UIAttribute) action.deepCopy());
		}
		clone.nextAdditionShouldOverrideActions = nextAdditionShouldOverrideActions;
		clone.nextAdditionShouldOverrideWhens = nextAdditionShouldOverrideWhens;
		return clone;
	}
	
	/**
	 * @return the whens
	 */
	@Override
	public List<UIAttribute> getWhens() {
		return whens;
	}
	
	/**
	 * @return the actions
	 */
	@Override
	public List<UIAttribute> getActions() {
		return actions;
	}
	
	/**
	 * @param nextAdditionShouldOverrideWhens
	 * 		the nextAdditionShouldOverrideWhens to set
	 */
	@Override
	public void setNextAdditionShouldOverrideWhens(final boolean nextAdditionShouldOverrideWhens) {
		this.nextAdditionShouldOverrideWhens = nextAdditionShouldOverrideWhens;
	}
	
	/**
	 * @param nextAdditionShouldOverrideActions
	 * 		the nextAdditionShouldOverrideActions to set
	 */
	@Override
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
	@SuppressWarnings("java:S1168")
	public List<UIElement> getChildrenRaw() {
		return null; // returning null is desired here
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final UIStateMutable uiState)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return uiState.canEqual(this) && nextAdditionShouldOverrideWhens == uiState.nextAdditionShouldOverrideWhens &&
				nextAdditionShouldOverrideActions == uiState.nextAdditionShouldOverrideActions &&
				Objects.equals(whens, uiState.whens) && Objects.equals(actions, uiState.actions);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIStateMutable);
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
