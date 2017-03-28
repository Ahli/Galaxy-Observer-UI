package application.baseUI;

/**
 * 
 * @author Ahli
 *
 */
public class UIConstant extends UIElement {
	String value = "";

	/**
	 * 
	 * @param name
	 */
	public UIConstant(String name) {
		super(name);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}

	/**
	 * 
	 */
	@Override
	public Object clone() {
		UIConstant clone = (UIConstant) super.clone();
		clone.setValue(value);

		return clone;
	}
}
