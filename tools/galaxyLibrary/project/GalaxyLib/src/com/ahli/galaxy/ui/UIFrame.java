package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ahli
 */
/*
 * NOTES: using null for certain often occurring values did not yield in
 * performance improvements
 */
public class UIFrame extends UIElement {
	private static final String THIS = "$this";
	private static final String[] POSI = { "Min", "Max" };
	private static final String ZERO = "0";
	
	/**
	 *
	 */
	private static final long serialVersionUID = -4610017347746925885L;
	
	private final List<UIAttribute> attributes;
	private final String[] pos = new String[4];
	private final String[] offset = new String[4];
	private final String[] relative = new String[4];
	private String type;
	private List<UIElement> children;
	
	/**
	 * @param name
	 * @param type
	 */
	public UIFrame(final String name, final String type) {
		super(name);
		init();
		attributes = new ArrayList<>();
		children = new ArrayList<>();
	}
	
	/**
	 * Initializes variables
	 */
	private void init() {
		relative[0] = THIS;
		relative[1] = THIS;
		relative[2] = THIS;
		relative[3] = THIS;
		
		pos[0] = POSI[0];
		pos[1] = POSI[0];
		pos[2] = POSI[1];
		pos[3] = POSI[1];
		offset[0] = ZERO;
		offset[1] = ZERO;
		offset[2] = ZERO;
		offset[3] = ZERO;
	}
	
	/**
	 * @param name
	 * @param initialAttributesCapacity
	 * @param initialChildrenCapacity
	 */
	public UIFrame(final String name, final int initialAttributesCapacity, final int initialChildrenCapacity) {
		super(name);
		init();
		attributes = new ArrayList<>(initialAttributesCapacity);
		children = new ArrayList<>(initialChildrenCapacity);
	}
	
	/**
	 * @param name
	 */
	public UIFrame(final String name) {
		super(name);
		type = null;
		attributes = new ArrayList<>();
		children = new ArrayList<>();
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIFrame clone = new UIFrame(getName(), attributes.size(), children.size());
		clone.type = type;
		final List<UIElement> childrenClone = clone.children;
		for (int i = 0, len = children.size(); i < len; i++) {
			childrenClone.add((UIElement) children.get(i).deepCopy());
		}
		for (int i = 0, len = attributes.size(); i < len; i++) {
			clone.attributes.add((UIAttribute) attributes.get(i).deepCopy());
		}
		for (int i = 0; i <= 3; i++) {
			clone.relative[i] = relative[i];
			clone.offset[i] = offset[i];
			clone.pos[i] = pos[i];
		}
		return clone;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type
	 * 		the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}
	
	/**
	 * @return
	 */
	public List<UIElement> getChildren() {
		return children;
	}
	
	/**
	 * @param children
	 */
	public void setChildren(final List<UIElement> children) {
		this.children = children;
	}
	
	/**
	 * @param value
	 */
	public UIAttribute addAttribute(final UIAttribute value) {
		final String key = value.getName();
		for (int i = 0, len = attributes.size(); i < len; i++) {
			if (attributes.get(i).getName().equalsIgnoreCase(key)) {
				return attributes.set(i, value);
			}
		}
		attributes.add(value);
		return null;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public UIAttribute getValue(final String key) {
		for (int i = 0, len = attributes.size(); i < len; i++) {
			final UIAttribute a = attributes.get(i);
			if (a.getName().equalsIgnoreCase(key)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * @param side
	 * @param relative
	 */
	public void setAnchorRelative(final UIAnchorSide side, final String relative) {
		this.relative[side.ordinal()] = relative;
	}
	
	/**
	 * @param side
	 * @param offset
	 */
	public void setAnchorOffset(final UIAnchorSide side, final String offset) {
		this.offset[side.ordinal()] = offset;
	}
	
	/**
	 * @param side
	 * @param pos
	 */
	public void setAnchorPos(final UIAnchorSide side, final String pos) {
		this.pos[side.ordinal()] = pos;
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorRelative(final UIAnchorSide side) {
		return relative[side.ordinal()];
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorOffset(final UIAnchorSide side) {
		return offset[side.ordinal()];
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorPos(final UIAnchorSide side) {
		return pos[side.ordinal()];
	}
	
	/**
	 * @param relative
	 * @param offset
	 */
	public void setAnchor(final String relative, final String offset) {
		this.relative[0] = relative;
		this.relative[1] = relative;
		this.relative[2] = relative;
		this.relative[3] = relative;
		pos[0] = POSI[0];
		pos[1] = POSI[0];
		pos[2] = POSI[1];
		pos[3] = POSI[1];
		if (offset != null) {
			this.offset[0] = offset;
			this.offset[1] = offset;
			this.offset[2] = Integer.toString((Integer.parseInt(offset) * (-1)));
			this.offset[3] = Integer.toString((Integer.parseInt(offset) * (-1)));
		} else {
			this.offset[0] = "0";
			this.offset[1] = "0";
			this.offset[2] = "0";
			this.offset[3] = "0";
		}
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
				for (final UIElement curElem : children) {
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
		return "<Frame type='" + type + "' name='" + getName() + "'>";
	}
	
}
