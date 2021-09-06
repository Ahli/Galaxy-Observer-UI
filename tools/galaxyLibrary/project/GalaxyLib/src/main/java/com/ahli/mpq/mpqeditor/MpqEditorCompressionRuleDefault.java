// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import java.util.Arrays;
import java.util.Objects;

public class MpqEditorCompressionRuleDefault extends MpqEditorCompressionRule {
	
	public MpqEditorCompressionRuleDefault() {
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
		return "Default=" + getAttributeString() + ", " + getCompressionMethodString() + ", 0xFFFFFFFF";
	}
	
	@Override
	public Object deepCopy() {
		return new MpqEditorCompressionRuleDefault(this);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((MpqEditorCompressionRuleDefault) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; ++i) {
			if (signatureFields[i] instanceof Object[] signatureFieldObjArray) {
				if (!Arrays.deepEquals(signatureFieldObjArray, (Object[]) thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { getCompressionMethod(), getAttributeString() };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
