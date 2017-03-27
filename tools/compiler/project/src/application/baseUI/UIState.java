package application.baseUI;

import java.util.ArrayList;

/**
 * 
 * @author Ahli
 *
 */
public class UIState extends UIElement {

	private ArrayList<UIAttribute> whens = new ArrayList<>();
	private ArrayList<UIAttribute> actions = new ArrayList<>();
	private boolean nextAdditionShouldOverride = false; // TODO implement & keep when/action management within this class to auto-override all whens/actions

	/**
	 * 
	 * @param name
	 */
	public UIState(String name) {
		super(name);
	}

	/**
	 * @return the whens
	 */
	public ArrayList<UIAttribute> getWhens() {
		return whens;
	}

	/**
	 * @param whens
	 *            the whens to set
	 */
	public void setWhens(ArrayList<UIAttribute> whens) {
		this.whens = whens;
	}

	/**
	 * @return the actions
	 */
	public ArrayList<UIAttribute> getActions() {
		return actions;
	}

	/**
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(ArrayList<UIAttribute> actions) {
		this.actions = actions;
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

}
