package application.baseUI;

import java.util.ArrayList;

public class DefaultUIElement {
	String name = "";
	String type = "";
	ArrayList<DefaultUIElement> children = null;

	public DefaultUIElement(String name, String type) {
		this.name = name;
		this.type = type;
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
	public ArrayList<DefaultUIElement> getChildren() {
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
	public boolean addChild(DefaultUIElement child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children.add(child);
	}
}
