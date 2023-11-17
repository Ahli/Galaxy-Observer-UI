// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class MpqEditorCompressionRuleSize extends MpqEditorCompressionRule {
	
	@Serial
	private static final long serialVersionUID = -3164363488174206663L;
	
	/**
	 * -- SETTER --
	 *  Sets the minimum file size for this rule. Must be greater or equal zero.
	 *
	 * @param minSize
	 */
	@Setter
	private int minSize;
	private final int maxSize;
	
	public MpqEditorCompressionRuleSize(final int minSize, final int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param original
	 */
	public MpqEditorCompressionRuleSize(final MpqEditorCompressionRuleSize original) {
		super(original);
		minSize = original.minSize;
		maxSize = original.maxSize;
	}
	
//	@Override
//	public boolean isValidRule() {
//		return super.isValidRule() && minSize >= 0 && maxSize >= minSize;
//	}
	
	@Override
	public String toString() {
		return "Size:" + minSize + "-" + maxSize + "=" + getAttributeString() + ", " + getCompressionMethodString() +
				", 0xFFFFFFFF";
	}
	
	//	/**
//	 * Sets the maximum file size for this rule. Must be greater or equal to minSize (and zero).
//	 *
//	 * @param maxSize
//	 */
//	public void setMaxSize(final int maxSize) {
//		this.maxSize = maxSize;
//	}
	
	@Override
	public Object deepCopy() {
		return new MpqEditorCompressionRuleSize(this);
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
		final Object[] thatSignatureFields = ((MpqEditorCompressionRuleSize) obj).getSignatureFields();
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
		return new Object[] { maxSize, minSize, getCompressionMethod(), getAttributeString() };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
