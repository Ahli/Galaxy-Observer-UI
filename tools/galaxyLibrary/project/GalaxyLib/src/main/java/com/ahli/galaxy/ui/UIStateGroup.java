package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
@JsonTypeInfo (use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIStateGroup extends UIElement {
	private String defaultState;
	private List<UIState> states;
	
	public UIStateGroup() {
		super(null);
		states = new ArrayList<>(0);
	}
	
	@JsonCreator
	public UIStateGroup(@JsonProperty ("name") final String name,
			@JsonProperty ("defaultState") final String defaultState,
			@JsonProperty ("states") final List<UIState> states) {
		super(name);
		this.defaultState = defaultState != null ? defaultState.intern() : defaultState;
		if(states != null) {
			this.states = states;
		} else {
			this.states = new ArrayList<>(0);
		}
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
	
	@Override
	public List<UIElement> getChildren() {
		if (states == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(states);
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
