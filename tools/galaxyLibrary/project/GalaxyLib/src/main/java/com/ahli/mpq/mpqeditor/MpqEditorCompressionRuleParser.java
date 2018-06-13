package com.ahli.mpq.mpqeditor;

public final class MpqEditorCompressionRuleParser {
	
	private MpqEditorCompressionRuleParser() {
		// no instances
	}
	
	public static MpqEditorCompressionRule parse(final String ruleString) {
		if (ruleString.startsWith("M" )) {
			return parseRuleMask(ruleString);
		} else if (ruleString.startsWith("S" )) {
			return parseRuleSize(ruleString);
		} else if (ruleString.startsWith("D" )) {
			return parseRuleDefault(ruleString);
		}
		throw new IllegalArgumentException("Unknown type of rule string: " + ruleString);
	}
	
	private static MpqEditorCompressionRule parseRuleMask(final String ruleString) {
		final int start = ruleString.indexOf(':') + 1;
		final int end = ruleString.indexOf('=');
		final MpqEditorCompressionRule rule = new MpqEditorCompressionRuleMask(ruleString.substring(start, end));
		parseAbstractFields(rule, ruleString);
		return rule;
	}
	
	private static MpqEditorCompressionRule parseRuleSize(final String ruleString) {
		final int start = ruleString.indexOf(':') + 1;
		final int dividerIndex = ruleString.indexOf('-');
		final int end = ruleString.indexOf('=');
		final int minSize = Integer.parseInt(ruleString.substring(start, dividerIndex));
		final int maxSize = Integer.parseInt(ruleString.substring(dividerIndex + 1, end));
		final MpqEditorCompressionRule rule = new MpqEditorCompressionRuleSize(minSize, maxSize);
		parseAbstractFields(rule, ruleString);
		return rule;
	}
	
	private static MpqEditorCompressionRule parseRuleDefault(final String ruleString) {
		final MpqEditorCompressionRule rule = new MpqEditorCompressionRuleDefault();
		parseAbstractFields(rule, ruleString);
		return rule;
	}
	
	private static void parseAbstractFields(final MpqEditorCompressionRule rule, final String ruleString) {
		final String ruleStr = ruleString.trim();
		final int i = ruleStr.indexOf('=');
		if (i != -1) {
			if (ruleStr.charAt(i + 4) == '1') {
				rule.setSingleUnit(true);
			} else if (ruleStr.charAt(i + 4) == '2') {
				rule.setMarkedForDeletion(true);
			}
			
			if (ruleStr.charAt(i + 6) == '1') {
				rule.setEncrypt(true);
			} else if (ruleStr.charAt(i + 6) == '3') {
				rule.setEncrypt(true);
				rule.setEncryptAdjusted(true);
			}
			
			if (ruleStr.charAt(i + 8) == '2') {
				rule.setCompress(true);
			}
			
			rule.setCompressionMethod(parseCompressionMethod(ruleStr.substring(ruleStr.indexOf(',') + 2)));
		}
	}
	
	private static MpqEditorCompressionRuleMethod parseCompressionMethod(final String methodString) {
		if (methodString.startsWith("02", 8)) {
			return MpqEditorCompressionRuleMethod.ZLIB;
		} else if (methodString.startsWith("08", 8)) {
			return MpqEditorCompressionRuleMethod.PKWARE;
		} else if (methodString.startsWith("10", 8)) {
			return MpqEditorCompressionRuleMethod.BZIP2;
		} else if (methodString.startsWith("12", 8)) {
			return MpqEditorCompressionRuleMethod.LZMA;
		} else if (methodString.startsWith("20", 8)) {
			return MpqEditorCompressionRuleMethod.SPARSE;
		} else if (methodString.startsWith("22", 8)) {
			return MpqEditorCompressionRuleMethod.SPARSE_ZLIB;
		} else if (methodString.startsWith("30", 8)) {
			return MpqEditorCompressionRuleMethod.SPARSE_BZIP2;
		}
		return MpqEditorCompressionRuleMethod.NONE;
	}
}
