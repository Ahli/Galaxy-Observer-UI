package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;

/**
 * @author Ahli
 */
public class UIConstant extends UIElement {
	private String value;
	
	public UIConstant() {
		super(null);
	}
	
	/**
	 * @param name
	 */
	public UIConstant(final String name) {
		super(name);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIConstant clone = new UIConstant(getName());
		clone.value = value;
		return clone;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value
	 * 		the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
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
		return "<Constant name='" + getName() + "', value='" + value + "'>";
	}
}
