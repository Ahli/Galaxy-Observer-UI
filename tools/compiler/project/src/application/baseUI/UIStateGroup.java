package application.baseUI;

import java.util.ArrayList;

/**
 * 
 * @author Ahli
 *
 */
public class UIStateGroup extends UIElement {

	private String defaultState = "";
	private ArrayList<UIState> states = new ArrayList<>();

	/**
	 * 
	 * @param name
	 */
	public UIStateGroup(String name) {
		super(name);
	}

	/**
	 * @return the defaultState
	 */
	public String getDefaultState() {
		return defaultState;
	}

	/**
	 * @param defaultState
	 *            the defaultState to set
	 */
	public void setDefaultState(String defaultState) {
		this.defaultState = defaultState;
	}

	/**
	 * @return the states
	 */
	public ArrayList<UIState> getStates() {
		return states;
	}

	/**
	 * @param states
	 *            the states to set
	 */
	public void setStates(ArrayList<UIState> states) {
		this.states = states;
	}

}
