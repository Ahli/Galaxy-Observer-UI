// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import lombok.Getter;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class MpqEditorCompressionRuleMask extends MpqEditorCompressionRule {
	
	@Serial
	private static final long serialVersionUID = 1058198721980855682L;
	
	private final String mask;
	
	public MpqEditorCompressionRuleMask(final String mask) {
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
		return "Mask:" + getMask() + "=" + getAttributeString() + ", " + getCompressionMethodString() + ", 0xFFFFFFFF";
	}
	
	@Override
	public Object deepCopy() {
		return new MpqEditorCompressionRuleMask(this);
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
		final Object[] thatSignatureFields = ((MpqEditorCompressionRuleMask) obj).getSignatureFields();
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
		return new Object[] { mask, getCompressionMethod(), getAttributeString() };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
