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
			for (UIElement curElem : states) {
				if (path.equalsIgnoreCase(curElem.getName())) {
					// found right frame -> cut path
					String newPath = UIElement.removeLeftPathLevel(path);
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
	public Object clone() {
		UIStateGroup clone = (UIStateGroup) super.clone();
		clone.setDefaultState(defaultState);

		// clone states
		for (UIState state : states) {
			clone.getStates().add((UIState) state.clone());
		}

		return clone;
	}
}
