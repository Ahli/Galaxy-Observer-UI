package application.baseUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Ahli
 *
 */
public class UIFrame extends UIElement {
	String type = "";
	ArrayList<UIElement> children = null;
	private Map<String, UIAttribute> attributes = new HashMap<>();
	String[] pos = new String[4];
	String[] offset = new String[4];
	String[] side = new String[4];
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
	 * @return the children
	 */
	public ArrayList<UIElement> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	/**
	 * 
	 * @param child
	 *            child to add
	 * @return whether child was added or not
	 */
	public boolean addChild(UIElement child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children.add(child);
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
	 * @param anchor
	 * @param relative
	 */
	public void setRelative(UIAnchor anchor, String relative) {
		this.relative[getAnchorIndex(anchor)] = relative;
	}

	/**
	 * 
	 * @param anchor
	 * @param offset
	 */
	public void setOffset(UIAnchor anchor, String offset) {
		this.offset[getAnchorIndex(anchor)] = offset;
	}

	/**
	 * 
	 * @param anchor
	 * @param pos
	 */
	public void setPos(UIAnchor anchor, String pos) {
		this.pos[getAnchorIndex(anchor)] = pos;
	}

	/**
	 * 
	 * @param anchor
	 * @return
	 */
	public String getRelative(UIAnchor anchor) {
		return relative[getAnchorIndex(anchor)];
	}

	/**
	 * 
	 * @param anchor
	 * @return
	 */
	public String getOffset(UIAnchor anchor) {
		return offset[getAnchorIndex(anchor)];
	}

	/**
	 * 
	 * @param anchor
	 * @return
	 */
	public String getPos(UIAnchor anchor) {
		return pos[getAnchorIndex(anchor)];
	}

	/**
	 * 
	 * @param anchor
	 * @return
	 */
	private int getAnchorIndex(UIAnchor anchor) {
		switch (anchor) {
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
		this.offset[2] = (Integer.parseInt(offset)*(-1)) + "";
		this.pos[3] = "Max";
		this.offset[3] = (Integer.parseInt(offset)*(-1)) + "";
	}

}
