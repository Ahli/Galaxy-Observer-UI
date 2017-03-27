package application.baseUI;

/**
 * 
 * @author Ahli
 *
 */
public abstract class UIElement {
	String name = "";

	public UIElement(String name) {
		this.name = name;
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

}
