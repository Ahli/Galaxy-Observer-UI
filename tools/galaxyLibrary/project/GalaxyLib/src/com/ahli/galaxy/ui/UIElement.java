package com.ahli.galaxy.ui;

/**
 * 
 * @author Ahli
 * 
 */
public abstract class UIElement {
	String name = "";
	
	public UIElement(final String name) {
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public abstract UIElement receiveFrameFromPath(String path);
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String removeLeftPathLevel(final String path) {
		final int i = path.indexOf('/');
		if (i == -1) {
			return null;
		}
		final String newPath = path.substring(i + 1);
		return newPath;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String getLeftPathLevel(final String path) {
		final int i = path.indexOf('/');
		if (i == -1) {
			return path;
		}
		return path.substring(0, i);
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract Object deepClone();
	
	@Override
	public String toString() {
		return "<UIElement name='" + name + "'>";
	}
}
