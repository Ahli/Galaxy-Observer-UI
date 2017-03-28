package application.baseUI;

/**
 * 
 * @author Ahli
 *
 */
public abstract class UIElement implements Cloneable {
	String name = "";

	public UIElement(String name) {
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
	 * 
	 * @param path
	 * @return
	 */
	public abstract UIElement receiveFrameFromPath(String path);

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String removeLeftPathLevel(String path) {
		int i = path.indexOf('/');
		if (i == -1) {
			return null;
		}
		String newPath = path.substring(i + 1);
		return newPath;
	}
	
	/**
	 * 
	 */
	@Override
	public Object clone(){
		try {
			UIElement clone = (UIElement) super.clone();
			clone.setName(name);
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
