package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ahli
 */
/*
 * NOTES: using null for certain often occurring values did not yield in
 * performance improvements
 */
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIFrame extends UIElement {
	private static final String THIS = "$this";
	private static final String[] POSI = { "Min", "Max" };
	private static final String ZERO = "0";
	
	private final String[] pos = new String[4];
	private final String[] offset = new String[4];
	private final String[] relative = new String[4];
	private List<UIAttribute> attributes;
	private String type;
	private List<UIElement> children;
	
	public UIFrame() {
		super(null);
	}
	
	/**
	 * @param name
	 * @param type
	 */
	public UIFrame(final String name, final String type) {
		super(name);
		init();
		attributes = null;
		children = null;
		this.type = type;
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
		attributes = initialAttributesCapacity > 0 ? new ArrayList<>(initialAttributesCapacity) : null;
		children = initialChildrenCapacity > 0 ? new ArrayList<>(initialChildrenCapacity) : null;
	}
	
	/**
	 * @param name
	 */
	public UIFrame(final String name) {
		super(name);
		type = null;
		attributes = null;
		children = null;
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final int attrSize = attributes != null ? attributes.size() : 0;
		final int childrenSize = children != null ? children.size() : 0;
		final UIFrame clone = new UIFrame(getName(), attrSize, childrenSize);
		clone.type = type;
		if (childrenSize > 0) {
			for (int i = 0; i < childrenSize; i++) {
				clone.children.add((UIElement) children.get(i).deepCopy());
			}
		}
		if (attrSize > 0) {
			for (int i = 0; i < attrSize; i++) {
				clone.attributes.add((UIAttribute) attributes.get(i).deepCopy());
			}
		}
		for (int i = 0; i <= 3; i++) {
			clone.relative[i] = relative[i];
			clone.offset[i] = offset[i];
			clone.pos[i] = pos[i];
		}
		return clone;
	}
	
	/**
	 * @return the type, may be null if not set
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
	 * @return list of children
	 */
	public List<UIElement> getChildren() {
		if (children == null) {
			children = new ArrayList<>(0);
		}
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
		if (attributes != null) {
			for (int i = 0, len = attributes.size(); i < len; i++) {
				if (attributes.get(i).getName().equalsIgnoreCase(key)) {
					return attributes.set(i, value);
				}
			}
		} else {
			attributes = new ArrayList<>(1);
		}
		attributes.add(value);
		return null;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public UIAttribute getValue(final String key) {
		if (attributes != null) {
			for (int i = 0, len = attributes.size(); i < len; i++) {
				final UIAttribute a = attributes.get(i);
				if (a.getName().equalsIgnoreCase(key)) {
					return a;
				}
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
			this.offset[0] = ZERO;
			this.offset[1] = ZERO;
			this.offset[2] = ZERO;
			this.offset[3] = ZERO;
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
			if (curName != null && children != null) {
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
