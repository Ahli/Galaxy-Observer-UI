// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.util.StringInterner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
/*
 * NOTES: using null for certain often occurring values did not yield in
 * performance improvements
 */
public class UIFrame extends UIElement {
	public static final String ZERO = "0";
	private static final String THIS = "$this";
	private static final String MIN = "MIN";
	private static final String MAX = "MAX";
	private static final String[] DFLT_POS = { MIN, MIN, MAX, MAX };
	private static final String[] DFLT_OFFSET = { ZERO, ZERO, ZERO, ZERO };
	private static final String[] DFLT_RELATIVE = { THIS, THIS, THIS, THIS };
	
	private String[] pos = new String[4];
	private String[] offset = new String[4];
	private String[] relative = new String[4];
	
	private List<UIAttribute> attributes;
	private String type;
	private List<UIElement> children;
	
	public UIFrame() {
		super(null);
		init();
		attributes = null;
		children = null;
	}
	
	/**
	 * Initializes variables
	 */
	private void init() {
		relative = DFLT_RELATIVE;
		pos = DFLT_POS;
		offset = DFLT_OFFSET;
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
		init();
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
		if (relative != DFLT_RELATIVE) {
			System.arraycopy(relative, 0, clone.relative, 0, 4);
		}
		if (offset != DFLT_OFFSET) {
			System.arraycopy(offset, 0, clone.offset, 0, 4);
		}
		if (pos != DFLT_POS) {
			System.arraycopy(pos, 0, clone.pos, 0, 4);
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
		this.type = StringInterner.intern(type);
	}
	
	@Override
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
	
	@Override
	public List<UIElement> getChildrenRaw() {
		return children;
	}
	
	/**
	 * @param value
	 */
	public void addAttribute(final UIAttribute value) {
		final String key = value.getName();
		if (attributes != null) {
			for (int i = 0, len = attributes.size(); i < len; i++) {
				if (attributes.get(i).getName().equalsIgnoreCase(key)) {
					attributes.set(i, value);
					return;
				}
			}
		} else {
			attributes = new ArrayList<>(1);
		}
		attributes.add(value);
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
	 * @return
	 */
	public List<UIAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>(0);
		}
		return attributes;
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
		pos = DFLT_POS;
		if (THIS.equalsIgnoreCase(relative)) {
			this.relative = DFLT_RELATIVE;
		} else {
			if (this.relative == DFLT_RELATIVE) {
				this.relative = new String[] { THIS, THIS, THIS, THIS };
			}
			this.relative[0] = StringInterner.intern(relative);
			this.relative[1] = this.relative[0];
			this.relative[2] = this.relative[0];
			this.relative[3] = this.relative[0];
		}
		if (offset == null || ZERO.equals(offset)) {
			this.offset = DFLT_OFFSET;
		} else {
			if (this.offset == DFLT_OFFSET) {
				this.offset = new String[] { ZERO, ZERO, ZERO, ZERO };
			}
			this.offset[0] = StringInterner.intern(offset);
			this.offset[1] = this.offset[0];
			this.offset[2] = StringInterner.intern(Integer.toString((Integer.parseInt(offset) * (-1))));
			this.offset[3] = this.offset[2];
		}
	}
	
	/**
	 * @param side
	 * @param relative
	 * @param pos
	 * @param offset
	 */
	public void setAnchor(final UIAnchorSide side, final String relative, final String pos, final String offset) {
		setAnchorRelative(side, relative);
		setAnchorPos(side, pos);
		setAnchorOffset(side, offset);
	}
	
	/**
	 * @param side
	 * @param relative
	 */
	public void setAnchorRelative(final UIAnchorSide side, final String relative) {
		if (this.relative == DFLT_RELATIVE) {
			if (THIS.equalsIgnoreCase(relative)) {
				return;
			}
			this.relative = new String[] { THIS, THIS, THIS, THIS };
		}
		this.relative[side.ordinal()] = THIS.equalsIgnoreCase(relative) ? THIS : StringInterner.intern(relative);
		if (THIS.equalsIgnoreCase(this.relative[0]) && THIS.equalsIgnoreCase(this.relative[1]) &&
				THIS.equalsIgnoreCase(this.relative[2]) && THIS.equalsIgnoreCase(this.relative[3])) {
			this.relative = DFLT_RELATIVE;
		}
	}
	
	/**
	 * @param side
	 * @param pos
	 */
	public void setAnchorPos(final UIAnchorSide side, final String pos) {
		if (this.pos == DFLT_POS) {
			if (DFLT_POS[side.ordinal()].equalsIgnoreCase(pos)) {
				return;
			}
			this.pos = new String[] { MIN, MIN, MAX, MAX };
		}
		this.pos[side.ordinal()] =
				MAX.equalsIgnoreCase(pos) ? MAX : (MIN.equalsIgnoreCase(pos) ? MIN : StringInterner.intern(pos));
		if (MIN.equals(this.pos[0]) && MIN.equals(this.pos[1]) && MAX.equals(this.pos[2]) && MAX.equals(this.pos[3])) {
			this.pos = DFLT_POS;
		}
	}
	
	/**
	 * @param side
	 * @param offset
	 */
	public void setAnchorOffset(final UIAnchorSide side, final String offset) {
		if (this.offset == DFLT_OFFSET) {
			if (ZERO.equals(offset)) {
				return;
			}
			this.offset = new String[] { ZERO, ZERO, ZERO, ZERO };
		}
		this.offset[side.ordinal()] = ZERO.equals(offset) ? ZERO : StringInterner.intern(offset);
		if (ZERO.equals(this.offset[0]) && ZERO.equals(this.offset[1]) && ZERO.equals(this.offset[2]) &&
				ZERO.equals(this.offset[3])) {
			this.offset = DFLT_OFFSET;
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
			if (children != null) {
				for (final UIElement curElem : children) {
					// curName cannot be null here
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
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UIFrame) obj).getSignatureFields();
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
		return new Object[] { getName(), type, relative, pos, offset, attributes, children };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
	
}
