package com.ahli.mpq.mpqeditor;

public class MpqEditorCompressionRuleDefault extends MpqEditorCompressionRule {
	
	public MpqEditorCompressionRuleDefault() {
		super();
	}
	
	@Override
	public String toString() {
		return "Default=" + super.getAttributeString() + ", " + super.getCompressionMethodString() + ", 0xFFFFFFFF";
	}
	
}
