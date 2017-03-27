package application.baseUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 * 
 * @author Ahli
 *
 */
public class UIAttribute extends UIElement {

	public UIAttribute(String name) {
		super(name);
	}

	private Map<String, String> values = new HashMap<>();

	/**
	 * @return the values
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(Map<String, String> values) {
		this.values = values;
	}

}
