package com.ahli.galaxy.ui;

/**
 * 
 * @author Ahli
 * 
 */
public class UIConstant extends UIElement {
	private String value = "";
	
	/**
	 * 
	 * @param name
	 */
	public UIConstant(final String name) {
		super(name);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		final UIConstant clone = new UIConstant(getName());
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
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}
	
	/**
	 * 
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
