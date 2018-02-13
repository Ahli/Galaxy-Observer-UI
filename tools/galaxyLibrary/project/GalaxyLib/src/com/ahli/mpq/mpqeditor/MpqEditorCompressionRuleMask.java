package com.ahli.mpq.mpqeditor;

public class MpqEditorCompressionRuleMask extends MpqEditorCompressionRule {
	private String mask;
	
	public MpqEditorCompressionRuleMask(final String mask) {
		super();
		setMask(mask);
	}
	
	@Override
	public String toString() {
		return "Mask:" + getMask() + "=" + super.getAttributeString() + ", " + super.getCompressionMethodString() +
				", 0xFFFFFFFF";
	}
	
	public String getMask() {
		return mask;
	}
	
	/**
	 * Sets the mask to match file names. Use '*' as a wildcard.
	 *
	 * @param mask
	 */
	public void setMask(final String mask) {
		this.mask = mask;
	}
}
