package com.ahli.galaxy.ui;

/**
 * 
 * @author Ahli
 * 
 */
public class UIConstant extends UIElement {
	String value = "";
	
	/**
	 * 
	 * @param name
	 */
	public UIConstant(final String name) {
		super(name);
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
	
	/**
	 * 
	 * @return
	 */
	@Override
	public Object deepClone() {
		final UIConstant clone = new UIConstant(name);
		clone.setValue(value);
		
		return clone;
	}
	
	@Override
	public String toString() {
		return "<Constant name='" + name + "', value='" + value + "'>";
	}
}
