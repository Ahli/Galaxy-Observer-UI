package com.ahli.galaxy.ui;

/**
 * 
 * @author Ahli
 *
 */
public abstract class UIElement {
	String name = "";
	
	public UIElement(String name) {
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
	public void setName(String name) {
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
	public static String removeLeftPathLevel(String path) {
		int i = path.indexOf('/');
		if (i == -1) {
			return null;
		}
		String newPath = path.substring(i + 1);
		return newPath;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String getLeftPathLevel(String path) {
		int i = path.indexOf('/');
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
