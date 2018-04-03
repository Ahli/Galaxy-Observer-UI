package application.ui.navigation;

/**
 * Class for Notifications used by the NavigationController.
 */
public class Notification {
	private String text;
	private int navPageIndex;
	
	public Notification(final String text, final int navPageIndex) {
		setText(text);
		setNavPageIndex(navPageIndex);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(final String text) {
		this.text = text;
	}
	
	public int getNavPageIndex() {
		return navPageIndex;
	}
	
	public void setNavPageIndex(final int navPageIndex) {
		this.navPageIndex = navPageIndex;
	}
}
