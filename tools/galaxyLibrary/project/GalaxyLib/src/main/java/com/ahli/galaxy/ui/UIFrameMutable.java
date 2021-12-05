// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIFrame;
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
public class UIFrameMutable extends UIElementAbstract implements UIFrame {
	public static final String ZERO = "0";
	public static final String THIS = "$this";
	public static final String MIN = "Min";
	public static final String MAX = "Max";
	private static final String[] DFLT_POS = { MIN, MIN, MAX, MAX };
	private static final String[] DFLT_OFFSET = { ZERO, ZERO, ZERO, ZERO };
	private static final String[] DFLT_RELATIVE = { THIS, THIS, THIS, THIS };
	
	private String[] pos;
	private String[] offset;
	private String[] relative;
	
	private List<UIAttribute> attributes;
	private String type;
	private List<UIElement> children;
	
	/**
	 * Constructor for Kryo
	 */
	private UIFrameMutable() {
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
	public UIFrameMutable(final String name, final String type) {
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
	public UIFrameMutable(final String name, final int initialAttributesCapacity, final int initialChildrenCapacity) {
		super(name);
		init();
		attributes = initialAttributesCapacity > 0 ? new ArrayList<>(initialAttributesCapacity) : null;
		children = initialChildrenCapacity > 0 ? new ArrayList<>(initialChildrenCapacity) : null;
	}
	
	/**
	 * @param name
	 */
	public UIFrameMutable(final String name) {
		super(name);
		init();
		type = null;
		attributes = null;
		children = null;
	}
	
	@Override
	public Object deepCopy() {
		final int attrSize = attributes != null ? attributes.size() : 0;
		final int childrenSize = children != null ? children.size() : 0;
		final UIFrameMutable clone = new UIFrameMutable(getName(), attrSize, childrenSize);
		clone.type = type;
		if (childrenSize > 0) {
			for (int i = 0; i < childrenSize; ++i) {
				clone.children.add((UIElement) children.get(i).deepCopy());
			}
		}
		if (attrSize > 0) {
			for (int i = 0; i < attrSize; ++i) {
				clone.attributes.add((UIAttribute) attributes.get(i).deepCopy());
			}
		}
		if (!Arrays.equals(relative, DFLT_RELATIVE)) {
			clone.relative = new String[4];
			System.arraycopy(relative, 0, clone.relative, 0, 4);
		}
		if (!Arrays.equals(offset, DFLT_OFFSET)) {
			clone.offset = new String[4];
			System.arraycopy(offset, 0, clone.offset, 0, 4);
		}
		if (!Arrays.equals(pos, DFLT_POS)) {
			clone.pos = new String[4];
			System.arraycopy(pos, 0, clone.pos, 0, 4);
		}
		return clone;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
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
	
	@Override
	public List<UIElement> getChildrenRaw() {
		return children;
	}
	
	@Override
	public void addAttribute(final UIAttribute value) {
		final String key = value.getName();
		if (attributes != null) {
			for (int i = 0, len = attributes.size(); i < len; ++i) {
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
	
	@Override
	public UIAttribute getValue(final String key) {
		if (attributes != null) {
			for (final UIAttribute a : attributes) {
				if (a.getName().equalsIgnoreCase(key)) {
					return a;
				}
			}
		}
		return null;
	}
	
	@Override
	public List<UIAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>(0);
		}
		return attributes;
	}
	
	@Override
	public List<UIAttribute> getAttributesRaw() {
		return attributes;
	}
	
	@Override
	public String getAnchorRelative(final UIAnchorSide side) {
		return relative[side.ordinal()];
	}
	
	@Override
	public String getAnchorOffset(final UIAnchorSide side) {
		return offset[side.ordinal()];
	}
	
	@Override
	public String getAnchorPos(final UIAnchorSide side) {
		return pos[side.ordinal()];
	}
	
	@Override
	public void setAnchor(final String relative, final String offset) {
		pos = DFLT_POS;
		if (THIS.equalsIgnoreCase(relative)) {
			this.relative = DFLT_RELATIVE;
		} else {
			if (Arrays.equals(this.relative, DFLT_RELATIVE)) {
				this.relative = new String[4];
			}
			this.relative[0] = StringInterner.intern(relative);
			this.relative[1] = this.relative[0];
			this.relative[2] = this.relative[0];
			this.relative[3] = this.relative[0];
		}
		if (offset == null || ZERO.equals(offset)) {
			this.offset = DFLT_OFFSET;
		} else {
			if (Arrays.equals(this.offset, DFLT_OFFSET)) {
				this.offset = new String[4];
			}
			this.offset[0] = StringInterner.intern(offset);
			this.offset[1] = this.offset[0];
			this.offset[2] = StringInterner.intern(Integer.toString((Integer.parseInt(offset) * (-1))));
			this.offset[3] = this.offset[2];
		}
	}
	
	@Override
	public void setAnchor(final UIAnchorSide side, final String relative, final String pos, final String offset) {
		setAnchorRelative(side, relative);
		setAnchorPos(side, pos);
		setAnchorOffset(side, offset);
	}
	
	@Override
	public void setAnchorRelative(final UIAnchorSide side, final String relative) {
		if (Arrays.equals(this.relative, DFLT_RELATIVE)) {
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
	
	@Override
	public void setAnchorPos(final UIAnchorSide side, final String pos) {
		if (Arrays.equals(this.pos, DFLT_POS)) {
			if (DFLT_POS[side.ordinal()].equalsIgnoreCase(pos)) {
				return;
			}
			this.pos = new String[] { MIN, MIN, MAX, MAX };
		}
		if (MAX.equalsIgnoreCase(pos)) {
			this.pos[side.ordinal()] = MAX;
		} else {
			this.pos[side.ordinal()] = MIN.equalsIgnoreCase(pos) ? MIN : StringInterner.intern(pos);
		}
		if (MIN.equals(this.pos[0]) && MIN.equals(this.pos[1]) && MAX.equals(this.pos[2]) && MAX.equals(this.pos[3])) {
			this.pos = DFLT_POS;
		}
	}
	
	@Override
	public void setAnchorOffset(final UIAnchorSide side, final String offset) {
		if (Arrays.equals(this.offset, DFLT_OFFSET)) {
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
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final UIFrameMutable uiFrame)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return uiFrame.canEqual(this) && Arrays.deepEquals(pos, uiFrame.pos) &&
				Arrays.deepEquals(offset, uiFrame.offset) && Arrays.deepEquals(relative, uiFrame.relative) &&
				Objects.equals(attributes, uiFrame.attributes) && Objects.equals(type, uiFrame.type) &&
				Objects.equals(children, uiFrame.children);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIFrameMutable);
	}
	
	@Override
	public final int hashCode() {
		@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
		int result = Objects.hash(super.hashCode(), attributes, type, children);
		result = 31 * result + Arrays.hashCode(pos);
		result = 31 * result + Arrays.hashCode(offset);
		result = 31 * result + Arrays.hashCode(relative);
		return result;
	}
}
