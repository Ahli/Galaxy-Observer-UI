// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import java.util.Arrays;
import java.util.Objects;

public class MpqEditorCompressionRuleSize extends MpqEditorCompressionRule {
	private int minSize;
	private int maxSize;
	
	public MpqEditorCompressionRuleSize(final int minSize, final int maxSize) {
		super();
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
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
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
