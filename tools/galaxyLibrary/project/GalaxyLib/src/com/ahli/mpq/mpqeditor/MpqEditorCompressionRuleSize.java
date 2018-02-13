package com.ahli.mpq.mpqeditor;

public class MpqEditorCompressionRuleSize extends MpqEditorCompressionRule {
	private int minSize = 0;
	private int maxSize = 0;
	
	public MpqEditorCompressionRuleSize(final int minSize, final int maxSize) {
		super();
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
	
	@Override
	public boolean isValidRule() {
		return super.isValidRule() && minSize >= 0 && maxSize >= minSize;
	}
	
	@Override
	public String toString() {
		return "Size:" + minSize + "-" + maxSize + "=" + super.getAttributeString() + ", " +
				super.getCompressionMethodString() + ", 0xFFFFFFFF";
	}
	
	public int getMinSize() {
		return minSize;
	}
	
	/**
	 * Sets the minimum file size for this rule. Must be greater or equal zero.
	 *
	 * @param minSize
	 */
	public void setMinSize(final int minSize) {
		this.minSize = minSize;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	/**
	 * Sets the maximum file size for this rule. Must be greater or equal to minSize (and zero).
	 *
	 * @param maxSize
	 */
	public void setMaxSize(final int maxSize) {
		this.maxSize = maxSize;
	}
}
