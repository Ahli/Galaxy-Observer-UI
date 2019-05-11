// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIStateGroup extends UIElement {
	private String defaultState;
	private List<UIElement> states;
	
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
		this.defaultState = defaultState.intern();
	}
	
	/**
	 * @return the states
	 */
	public List<UIElement> getStates() {
		return states;
	}
	
	/**
	 * @param states
	 * 		the states to set
	 */
	public void setStates(final List<UIElement> states) {
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
		if (states == null) {
			states = new ArrayList<>(0);
		}
		return states;
	}
	
	@Override
	public List<UIElement> getChildrenRaw() {
		return states;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UIStateGroup)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UIStateGroup) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { getName(), defaultState, states };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
