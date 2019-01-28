// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.abstracts;

import com.ahli.util.DeepCopyable;

import java.util.List;

/**
 * @author Ahli
 */
public abstract class UIElement implements DeepCopyable {
	
	private String name;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		element's name
	 */
	public UIElement(final String name) {
		this.name = name != null ? name.intern() : null;
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
		this.name = name != null ? name.intern() : null;
	}
	
	/**
	 * Returns the correct frame based on a specified path. For example, the frame that a path in a template
	 * references.
	 *
	 * @param path
	 * 		path of an element
	 * @return UIFrame element
	 */
	public abstract UIElement receiveFrameFromPath(String path);
	
	@Override
	public String toString() {
		return "<UIElement name='" + name + "'>";
	}
	
	/**
	 * Returns all child UIElements.
	 *
	 * @return
	 */
	public abstract List<UIElement> getChildren();
}
