// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIState;
import com.ahli.galaxy.ui.interfaces.UIStateGroup;
import com.ahli.util.StringInterner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIStateGroupMutable extends UIElementAbstract implements UIStateGroup {
	private final List<UIElement> states;
	private String defaultState;
	
	/**
	 * Constructor for Kryo
	 */
	private UIStateGroupMutable() {
		super(null);
		states = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIStateGroupMutable(final String name) {
		super(name);
		states = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 * @param statesCapacity
	 */
	public UIStateGroupMutable(final String name, final int statesCapacity) {
		super(name);
		states = new ArrayList<>(statesCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIStateGroupMutable clone = new UIStateGroupMutable(getName(), states.size());
		for (int i = 0, len = states.size(); i < len; ++i) {
			clone.states.add((UIState) states.get(i).deepCopy());
		}
		clone.defaultState = defaultState;
		return clone;
	}
	
	/**
	 * @return the defaultState
	 */
	@Override
	public String getDefaultState() {
		return defaultState;
	}
	
	/**
	 * @param defaultState
	 * 		the defaultState to set
	 */
	@Override
	public void setDefaultState(final String defaultState) {
		this.defaultState = defaultState != null ? StringInterner.intern(defaultState) : null;
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
			for (final UIElement curElem : states) {
				// curName cannot be null here
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
		return "<StateGroup name='" + getName() + "'>";
	}
	
	@Override
	public List<UIElement> getChildren() {
		return states;
	}
	
	@Override
	@SuppressWarnings("java:S4144")
	public List<UIElement> getChildrenRaw() {
		return states;
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final UIStateGroupMutable that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return that.canEqual(this) && Objects.equals(defaultState, that.defaultState) &&
				Objects.equals(states, that.states);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIStateGroupMutable);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(super.hashCode(), defaultState, states);
	}
}
