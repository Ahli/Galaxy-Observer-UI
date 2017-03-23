package application.baseUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 * 
 * @author Ahli
 *
 */
public class DefaultUIAttribute {
	String name = "";
	Map<String,String> values = new HashMap<>();
	
	public DefaultUIAttribute(String name) {
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

	/**
	 * @return the children
	 */
	public Map<String,String> getValues() {
		return values;
	}

	/**
	 * 
	 * @param id
	 * @param value
	 */
	public void addValue(String id, String value) {
		values.put(id, value);
	}
}
