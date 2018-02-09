package com.ahli.galaxy.ui;

import com.ahli.util.DeepCopyable;

import java.io.Serializable;

/**
 * @author Ahli
 */
public abstract class UIElement implements Serializable, DeepCopyable {
	/**
	 *
	 */
	private static final long serialVersionUID = -9128521308405405698L;
	
	private String name;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		element's name
	 */
	public UIElement(final String name) {
		this.name = name;
	}
	
	/**
	 * Removes the left/top-most level of the specified path and returns the remaining.
	 *
	 * @param path
	 * @return
	 */
	public static String removeLeftPathLevel(final String path) {
		final int i = path.indexOf('/');
		return (i == -1) ? null : path.substring(i + 1);
	}
	
	/**
	 * @param path
	 * @return
	 */
	public static String getLeftPathLevel(final String path) {
		final int i = path.indexOf('/');
		return (i == -1) ? path : path.substring(0, i);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 * 		the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * Returns the correct frame based on a specified path. For example, the frame
	 * that a path in a template references.
	 *
	 * @param path
	 * 		path of an element
	 * @return Frame element
	 */
	public abstract UIElement receiveFrameFromPath(String path);
	
	@Override
	public String toString() {
		return "<UIElement name='" + name + "'>";
	}
}
