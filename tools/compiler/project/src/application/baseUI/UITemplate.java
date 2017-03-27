package application.baseUI;

/**
 * 
 * @author Ahli
 *
 */
public class UITemplate {

	private String fileName = "";
	private UIFrame frame = null;
	private boolean isLocked = false;

	/**
	 * 
	 * @param fileName
	 * @param frame
	 */
	public UITemplate(String fileName, UIFrame frame) {
		this.fileName = fileName;
		this.frame = frame;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the frame
	 */
	public UIFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame
	 *            the frame to set
	 */
	public void setFrame(UIFrame frame) {
		this.frame = frame;
	}

	/**
	 * @return the isLocked
	 */
	public boolean isLocked() {
		return isLocked;
	}

	/**
	 * @param isLocked
	 *            the isLocked to set
	 */
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

}
