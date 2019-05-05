// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.navigation;

/**
 * Class for Notifications used by the NavigationController.
 */
public class Notification {
	private String text;
	private int navPageIndex;
	
	public Notification(final String text, final int navPageIndex) {
		this.text = text;
		this.navPageIndex = navPageIndex;
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
