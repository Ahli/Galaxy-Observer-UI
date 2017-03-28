package application.baseUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Ahli
 *
 */
public class UIState extends UIElement {

	private ArrayList<UIAttribute> whens = new ArrayList<>();
	private ArrayList<UIAttribute> actions = new ArrayList<>();
	private boolean nextAdditionShouldOverride = false;

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
	 * @param nextAdditionShouldOverride
	 *            the nextAdditionShouldOverride to set
	 */
	public void setNextAdditionShouldOverride(boolean nextAdditionShouldOverride) {
		this.nextAdditionShouldOverride = nextAdditionShouldOverride;
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
		UIState clone = (UIState) super.clone();
		clone.setNextAdditionShouldOverride(nextAdditionShouldOverride);

		// clone whens
		for (UIElement when : whens) {
			clone.getWhens().add((UIAttribute) when.clone());
		}

		// clone actions
		for (UIElement action : actions) {
			clone.getActions().add((UIAttribute) action.clone());
		}

		return clone;
	}
}
