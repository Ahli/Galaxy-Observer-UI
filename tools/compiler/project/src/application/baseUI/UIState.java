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
	private boolean nextAdditionShouldOverrideWhens = false;
	private boolean nextAdditionShouldOverrideActions = false;

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
	 * @return the nextAdditionShouldOverrideWhens
	 */
	public boolean isNextAdditionShouldOverrideWhens() {
		return nextAdditionShouldOverrideWhens;
	}

	/**
	 * @param nextAdditionShouldOverrideWhens
	 *            the nextAdditionShouldOverrideWhens to set
	 */
	public void setNextAdditionShouldOverrideWhens(boolean nextAdditionShouldOverrideWhens) {
		this.nextAdditionShouldOverrideWhens = nextAdditionShouldOverrideWhens;
	}

	/**
	 * @return the nextAdditionShouldOverrideActions
	 */
	public boolean isNextAdditionShouldOverrideActions() {
		return nextAdditionShouldOverrideActions;
	}

	/**
	 * @param nextAdditionShouldOverrideActions
	 *            the nextAdditionShouldOverrideActions to set
	 */
	public void setNextAdditionShouldOverrideActions(boolean nextAdditionShouldOverrideActions) {
		this.nextAdditionShouldOverrideActions = nextAdditionShouldOverrideActions;
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
	 * @return
	 */
	@Override
	public Object deepClone() {
		UIState clone = new UIState(name);
		clone.setNextAdditionShouldOverrideWhens(nextAdditionShouldOverrideWhens);
		clone.setNextAdditionShouldOverrideActions(nextAdditionShouldOverrideActions);

		// clone whens
		for (UIElement when : whens) {
			clone.getWhens().add((UIAttribute) when.deepClone());
		}

		// clone actions
		for (UIElement action : actions) {
			clone.getActions().add((UIAttribute) action.deepClone());
		}

		return clone;
	}
}
