package com.ahli.galaxy.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Ahli
 *
 */
public class UIFrame extends UIElement {
	private final static Logger LOGGER = LogManager.getLogger(UIFrame.class);
	
	String type = "";
	ArrayList<UIElement> children = new ArrayList<>();
	private Map<String, UIAttribute> attributes = new HashMap<>();
	String[] pos = new String[4];
	String[] offset = new String[4];
	String[] relative = new String[4];
	
	/**
	 * 
	 * @param name
	 * @param type
	 */
	public UIFrame(String name, String type) {
		super(name);
		this.type = type;
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<UIElement> getChildren() {
		return children;
	}
	
	/**
	 * 
	 * @param children
	 */
	public void setChildren(ArrayList<UIElement> children) {
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
	public void setAttributes(Map<String, UIAttribute> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * 
	 * @param side
	 * @param relative
	 */
	public void setAnchorRelative(UIAnchorSide side, String relative) {
		this.relative[getAnchorSideIndex(side)] = relative;
	}
	
	/**
	 * 
	 * @param side
	 * @param offset
	 */
	public void setAnchorOffset(UIAnchorSide side, String offset) {
		this.offset[getAnchorSideIndex(side)] = offset;
	}
	
	/**
	 * 
	 * @param side
	 * @param pos
	 */
	public void setAnchorPos(UIAnchorSide side, String pos) {
		this.pos[getAnchorSideIndex(side)] = pos;
	}
	
	/**
	 * 
	 * @param side
	 * @return
	 */
	public String getAnchorRelative(UIAnchorSide side) {
		return relative[getAnchorSideIndex(side)];
	}
	
	/**
	 * 
	 * @param side
	 * @return
	 */
	public String getAnchorOffset(UIAnchorSide side) {
		return offset[getAnchorSideIndex(side)];
	}
	
	/**
	 * 
	 * @param side
	 * @return
	 */
	public String getAnchorPos(UIAnchorSide side) {
		return pos[getAnchorSideIndex(side)];
	}
	
	/**
	 * 
	 * @param side
	 * @return
	 */
	private int getAnchorSideIndex(UIAnchorSide side) {
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
	 * 
	 * @param relative
	 * @param offset
	 */
	public void setAnchor(String relative, String offset) {
		this.relative[0] = relative;
		this.relative[1] = relative;
		this.relative[2] = relative;
		this.relative[3] = relative;
		this.pos[0] = "Min";
		this.offset[0] = offset;
		this.pos[1] = "Min";
		this.offset[1] = offset;
		this.pos[2] = "Max";
		this.offset[2] = Integer.toString((Integer.parseInt(offset) * (-1)));
		this.pos[3] = "Max";
		this.offset[3] = Integer.toString((Integer.parseInt(offset) * (-1)));
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(String path) {
		if (path == null || path.isEmpty()) {
			// end here
			return this;
		} else {
			// go deeper
			String curName = UIElement.getLeftPathLevel(path);
			LOGGER.debug("curName: " + curName);
			LOGGER.debug("children to check: " + children.size());
			for (UIElement curElem : children) {
				LOGGER.debug("checking child: " + curElem.getName());
				if (curName.equalsIgnoreCase(curElem.getName())) {
					// found right frame -> cut path
					String newPath = UIElement.removeLeftPathLevel(path);
					LOGGER.debug("match! newPath:" + newPath);
					return curElem.receiveFrameFromPath(newPath);
				}
			}
			return null;
		}
	}
	
	/**
	 * 
	 */
	@Override
	public Object deepClone() {
		UIFrame clone = new UIFrame(name, type);
		
		// clone attributes
		Map<String, UIAttribute> clonedAttributes = new HashMap<>();
		for (Entry<String, UIAttribute> entry : attributes.entrySet()) {
			UIAttribute clonedValue = (UIAttribute) entry.getValue().deepClone();
			clonedAttributes.put(entry.getKey(), clonedValue);
		}
		clone.setAttributes(clonedAttributes);
		
		// clone anchors
		clone.setAnchorOffset(UIAnchorSide.Top, offset[0]);
		clone.setAnchorOffset(UIAnchorSide.Left, offset[1]);
		clone.setAnchorOffset(UIAnchorSide.Bottom, offset[2]);
		clone.setAnchorOffset(UIAnchorSide.Right, offset[3]);
		clone.setAnchorPos(UIAnchorSide.Top, pos[0]);
		clone.setAnchorPos(UIAnchorSide.Left, pos[1]);
		clone.setAnchorPos(UIAnchorSide.Bottom, pos[2]);
		clone.setAnchorPos(UIAnchorSide.Right, pos[3]);
		clone.setAnchorRelative(UIAnchorSide.Top, relative[0]);
		clone.setAnchorRelative(UIAnchorSide.Left, pos[1]);
		clone.setAnchorRelative(UIAnchorSide.Bottom, pos[2]);
		clone.setAnchorRelative(UIAnchorSide.Right, pos[3]);
		
		for (UIElement child : children) {
			clone.getChildren().add((UIElement) child.deepClone());
		}
		
		return clone;
	}
	
}
