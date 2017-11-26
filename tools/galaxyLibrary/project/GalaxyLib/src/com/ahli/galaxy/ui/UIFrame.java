package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ahli
 */
public class UIFrame extends UIElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4610017347746925885L;
	
	private static final Logger LOGGER = LogManager.getLogger(UIFrame.class);
	
	private String type = "";
	private List<UIElement> children = null;
	private Map<String, UIAttribute> attributes = null;
	// private ArrayList<Pair<String, UIAttribute>> attributes = null;
	private final String[] pos = new String[4];
	private final String[] offset = new String[4];
	private final String[] relative = new String[4];
	
	/**
	 * @param name
	 * @param type
	 */
	public UIFrame(final String name, final String type) {
		super(name);
		this.type = type;
		init();
		attributes = new HashMap<>(1, 1f);
		// attributes = new ArrayList<>();
		children = new ArrayList<>();
	}
	
	/**
	 * @param name
	 * @param type
	 */
	public UIFrame(final String name, final String type, final int initialAttributesCapacity,
			final int initialChildrenCapacity) {
		super(name);
		this.type = type;
		init();
		attributes = new HashMap<>(initialAttributesCapacity, 1f);
		// attributes = new ArrayList<>(initialAttributesCapacity);
		children = new ArrayList<>(initialChildrenCapacity);
	}
	
	/**
	 * Initializes variables
	 */
	private void init() {
		relative[0] = "$this";
		pos[0] = "Min";
		offset[0] = "0";
		relative[1] = "$this";
		pos[1] = "Min";
		offset[1] = "0";
		relative[2] = "$this";
		pos[2] = "Max";
		offset[2] = "0";
		relative[3] = "$this";
		pos[3] = "Max";
		offset[3] = "0";
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object clone() {
		final UIFrame clone = new UIFrame(getName(), type, attributes.size(), children.size());
		final List<UIElement> childrenClone = clone.children;
		for (int i = 0; i < children.size(); i++) {
			childrenClone.add((UIElement) children.get(i).clone());
		}
		final Map<String, UIAttribute> clonedMap = clone.attributes;
		final Object[] entries = attributes.entrySet().toArray();
		for (int fix = 0, i = fix; i < entries.length; i++) {
			@SuppressWarnings("unchecked")
			final Entry<String, UIAttribute> entry = (Entry<String, UIAttribute>) entries[i];
			clonedMap.put(entry.getKey(), entry.getValue());
		}
		// for (int i = 0; i < attributes.size(); i++) {
		// final Pair<String, UIAttribute> p = attributes.get(i);
		// clone.attributes.add(new Pair<>(p.getKey(), (UIAttribute)
		// p.getValue().clone()));
		// }
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
	 *            the type to set
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
	 * @return the attributes
	 */
	public Map<String, UIAttribute> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(final Map<String, UIAttribute> attributes) {
		this.attributes = attributes;
	}
	
	// /**
	// * @return the attributes
	// */
	// public ArrayList<Pair<String, UIAttribute>> getAttributes() {
	// return attributes;
	// }
	//
	// /**
	// * @param attributes
	// * the attributes to set
	// */
	// public void setAttributes(final ArrayList<Pair<String, UIAttribute>>
	// attributes) {
	// this.attributes = attributes;
	// }
	//
	// /**
	// *
	// * @param key
	// * @param value
	// */
	// public UIAttribute addAttribute(final String key, final UIAttribute value) {
	// final Pair<String, UIAttribute> newPair = new Pair<>(key, value);
	// final int i = attributes.indexOf(newPair);
	// if (i == -1) {
	// attributes.add(newPair);
	// return null;
	// } else {
	// return attributes.set(i, newPair).getValue();
	// }
	// }
	//
	// /**
	// *
	// * @param key
	// * @return
	// */
	// public UIAttribute getValue(final String key) {
	// int i;
	// Pair<String, UIAttribute> p = null;
	// for (i = 0; i < attributes.size(); i++) {
	// p = attributes.get(0);
	// if (p.getKey().equals(key)) {
	// return p.getValue();
	// }
	// }
	// return null;
	// }
	
	/**
	 * @param side
	 * @param relative
	 */
	public void setAnchorRelative(final UIAnchorSide side, final String relative) {
		this.relative[getAnchorSideIndex(side)] = relative;
	}
	
	/**
	 * @param side
	 * @param offset
	 */
	public void setAnchorOffset(final UIAnchorSide side, final String offset) {
		this.offset[getAnchorSideIndex(side)] = offset;
	}
	
	/**
	 * @param side
	 * @param pos
	 */
	public void setAnchorPos(final UIAnchorSide side, final String pos) {
		this.pos[getAnchorSideIndex(side)] = pos;
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorRelative(final UIAnchorSide side) {
		return relative[getAnchorSideIndex(side)];
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorOffset(final UIAnchorSide side) {
		return offset[getAnchorSideIndex(side)];
	}
	
	/**
	 * @param side
	 * @return
	 */
	public String getAnchorPos(final UIAnchorSide side) {
		return pos[getAnchorSideIndex(side)];
	}
	
	/**
	 * @param side
	 * @return
	 */
	private int getAnchorSideIndex(final UIAnchorSide side) {
		switch (side) {
			case Top:
				return 0;
			case Left:
				return 1;
			case Bottom:
				return 2;
			default:
				return 3;
		}
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
		pos[0] = "Min";
		this.offset[0] = offset;
		pos[1] = "Min";
		this.offset[1] = offset;
		pos[2] = "Max";
		this.offset[2] = Integer.toString((Integer.parseInt(offset) * (-1)));
		pos[3] = "Max";
		this.offset[3] = Integer.toString((Integer.parseInt(offset) * (-1)));
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
			LOGGER.trace("curName: " + curName);
			LOGGER.trace("children to check: " + children.size());
			for (final UIElement curElem : children) {
				LOGGER.trace("checking child: " + curElem.getName());
				if (curName.equalsIgnoreCase(curElem.getName())) {
					// found right frame -> cut path
					final String newPath = UIElement.removeLeftPathLevel(path);
					LOGGER.trace("match! newPath:" + newPath);
					return curElem.receiveFrameFromPath(newPath);
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
