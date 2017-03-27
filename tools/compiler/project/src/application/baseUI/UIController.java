package application.baseUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Ahli
 *
 */
public class UIController extends UIElement {
	private Map<String, String> values = new HashMap<>();
	private ArrayList<UIAttribute> keys = new ArrayList<>();
	private boolean nextAdditionShouldOverride = false; // TODO implement & keep key management within this class to auto-override all keys
	private boolean nameIsImplicit = true;
	
	/**
	 * 
	 * @param name
	 */
	public UIController(String name) {
		super(name);
	}

	/**
	 * @return the keys
	 */
	public ArrayList<UIAttribute> getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(ArrayList<UIAttribute> keys) {
		this.keys = keys;
	}

	/**
	 * @return the nextAdditionShouldOverride
	 */
	public boolean isNextAdditionShouldOverride() {
		return nextAdditionShouldOverride;
	}

	/**
	 * @param nextAdditionShouldOverride the nextAdditionShouldOverride to set
	 */
	public void setNextAdditionShouldOverride(boolean nextAdditionShouldOverride) {
		this.nextAdditionShouldOverride = nextAdditionShouldOverride;
	}

	/**
	 * @return the values
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	/**
	 * @return the nameIsImplicit
	 */
	public boolean isNameIsImplicit() {
		return nameIsImplicit;
	}

	/**
	 * @param nameIsImplicit the nameIsImplicit to set
	 */
	public void setNameIsImplicit(boolean nameIsImplicit) {
		this.nameIsImplicit = nameIsImplicit;
	}

}
