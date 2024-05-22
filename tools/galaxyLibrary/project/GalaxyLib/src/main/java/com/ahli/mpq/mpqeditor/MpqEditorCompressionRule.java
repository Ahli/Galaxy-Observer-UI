// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import com.ahli.cloning.DeepCopyable;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Getter
public abstract class MpqEditorCompressionRule implements DeepCopyable, Serializable {
	
	@Serial
	private static final long serialVersionUID = -8266828245771802518L;
	
	private boolean singleUnit;
	private boolean compress;
	private boolean encrypt;
	private boolean encryptAdjusted;
	private boolean includeSectorChecksum;
	private boolean markedForDeletion;
	
	private MpqEditorCompressionRuleMethod compressionMethod = MpqEditorCompressionRuleMethod.NONE;
	
	protected MpqEditorCompressionRule() {
		// nothing to do
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param original
	 */
	protected MpqEditorCompressionRule(final MpqEditorCompressionRule original) {
		singleUnit = original.singleUnit;
		compress = original.compress;
		encrypt = original.encrypt;
		encryptAdjusted = original.encryptAdjusted;
		includeSectorChecksum = original.includeSectorChecksum;
		markedForDeletion = original.markedForDeletion;
		compressionMethod = original.compressionMethod;
	}
	
	/**
	 * Instead of being divided to 0x1000-bytes blocks, the file is stored as single unit. Requires encrypt to be
	 * disabled.
	 *
	 * @param singleUnit
	 */
	public MpqEditorCompressionRule setSingleUnit(final boolean singleUnit) {
		this.singleUnit = singleUnit;
		return this;
	}
	
	/**
	 * File is compressed using combination of compression methods.
	 *
	 * @param compress
	 */
	public MpqEditorCompressionRule setCompress(final boolean compress) {
		this.compress = compress;
		return this;
	}
	
	/**
	 * Requires singleUnit to be disabled.
	 *
	 * @param encrypt
	 */
	public MpqEditorCompressionRule setEncrypt(final boolean encrypt) {
		this.encrypt = encrypt;
		return this;
	}
	
	/**
	 * Requires encrypt to be enabled.
	 *
	 * @param encryptAdjusted
	 */
	public MpqEditorCompressionRule setEncryptAdjusted(final boolean encryptAdjusted) {
		this.encryptAdjusted = encryptAdjusted;
		return this;
	}
	
	/**
	 * File is a deletion marker, indicating that the file no longer exists. This is used to allow patch archives to
	 * delete files present in lower-priority archives in the search chain. The file usually has length of 0 or 1 byte
	 * and its name is a hash.
	 *
	 * @param markedForDeletion
	 */
	public MpqEditorCompressionRule setMarkedForDeletion(final boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
		return this;
	}
	
	/**
	 * Returns the attributes as String to be used in the rule's String representation.
	 *
	 * @return
	 */
	public String getAttributeString() {
		return "0x0" + (markedForDeletion ? '2' : (singleUnit ? '1' : '0')) + '0' +
				(encrypt ? (encryptAdjusted ? '3' : '1') : '0') + '0' + (compress ? '2' : '0') + "00";
	}
	
	/**
	 * Returns the rule as a String entry.
	 *
	 * @return
	 */
	@Override
	public abstract String toString();
	
	/**
	 * @return
	 */
	public String getCompressionMethodString() {
		return switch (compressionMethod) {
			case ZLIB -> "0x00000002";
			case PKWARE -> "0x00000008";
			case BZIP2 -> "0x00000010";
			case LZMA -> "0x00000012";
			case SPARSE -> "0x00000020";
			case SPARSE_ZLIB -> "0x00000022";
			case SPARSE_BZIP2 -> "0x00000030";
			default -> "0x00000000";
		};
	}
	
	/**
	 * Sets the compression method that is used when compression is enabled.
	 *
	 * @param compressionMethod
	 */
	public MpqEditorCompressionRule setCompressionMethod(final MpqEditorCompressionRuleMethod compressionMethod) {
		this.compressionMethod = compressionMethod;
		return this;
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
		final Object[] thatSignatureFields = ((MpqEditorCompressionRule) obj).getSignatureFields();
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
		return new Object[] { singleUnit, compress, encrypt, encryptAdjusted, includeSectorChecksum,
				markedForDeletion };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
