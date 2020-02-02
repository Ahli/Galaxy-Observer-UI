// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.navigation;

/**
 * Notifications used by the NavigationController.
 */
public class Notification {
	private final String id;
	private final String text;
	private final int navPageIndex;
	
	public Notification(final String text, final int navPageIndex, final String id) {
		this.text = text;
		this.navPageIndex = navPageIndex;
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getText() {
		return text;
	}
	
	public int getNavPageIndex() {
		return navPageIndex;
	}
}
