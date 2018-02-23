package com.ahli.mpq.mpqeditor;

public class MpqEditorCompressionRuleDefault extends MpqEditorCompressionRule {
	
	public MpqEditorCompressionRuleDefault() {
		super();
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param original
	 */
	public MpqEditorCompressionRuleDefault(final MpqEditorCompressionRuleDefault original) {
		super(original);
	}
	
	@Override
	public String toString() {
		return "Default=" + super.getAttributeString() + ", " + super.getCompressionMethodString() + ", 0xFFFFFFFF";
	}
	
	@Override
	public Object deepCopy() {
		return new MpqEditorCompressionRuleDefault(this);
	}
}
