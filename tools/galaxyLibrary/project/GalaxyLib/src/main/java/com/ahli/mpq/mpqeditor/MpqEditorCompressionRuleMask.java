// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

public class MpqEditorCompressionRuleMask extends MpqEditorCompressionRule {
	private String mask;
	
	public MpqEditorCompressionRuleMask(final String mask) {
		super();
		this.mask = mask;
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param original
	 */
	public MpqEditorCompressionRuleMask(final MpqEditorCompressionRuleMask original) {
		super(original);
		mask = original.mask;
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
	
	@Override
	public Object deepCopy() {
		return new MpqEditorCompressionRuleMask(this);
	}
}
