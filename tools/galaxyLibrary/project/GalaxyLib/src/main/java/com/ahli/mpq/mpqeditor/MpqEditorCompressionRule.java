// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq.mpqeditor;

import com.ahli.util.DeepCopyable;

public abstract class MpqEditorCompressionRule implements DeepCopyable {
	private boolean singleUnit;
	private boolean compress;
	private boolean encrypt;
	private boolean encryptAdjusted;
	private boolean includeSectorChecksum;
	private boolean markedForDeletion;
	
	private MpqEditorCompressionRuleMethod compressionMethod = MpqEditorCompressionRuleMethod.NONE;
	
	public MpqEditorCompressionRule() {
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param original
	 */
	public MpqEditorCompressionRule(final MpqEditorCompressionRule original) {
		singleUnit = original.singleUnit;
		compress = original.compress;
		encrypt = original.encrypt;
		encryptAdjusted = original.encryptAdjusted;
		includeSectorChecksum = original.includeSectorChecksum;
		markedForDeletion = original.markedForDeletion;
		compressionMethod = original.compressionMethod;
	}
	
	/**
	 * Checks if the rule is valid.
	 *
	 * @return
	 */
	public boolean isValidRule() {
		return (!singleUnit || !encrypt) && (encrypt || !encryptAdjusted) && (!includeSectorChecksum || compress);
	}
	
	public boolean isSingleUnit() {
		return singleUnit;
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
	
	public boolean isCompress() {
		return compress;
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
	
	public boolean isEncrypt() {
		return encrypt;
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
	
	public boolean isEncryptAdjusted() {
		return encryptAdjusted;
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
	
	public boolean isIncludeSectorChecksum() {
		return includeSectorChecksum;
	}
	
	/**
	 * Requires compression to be enabled.
	 *
	 * @param includeSectorChecksum
	 */
	public MpqEditorCompressionRule setIncludeSectorChecksum(final boolean includeSectorChecksum) {
		this.includeSectorChecksum = includeSectorChecksum;
		return this;
	}
	
	public boolean isMarkedForDeletion() {
		return markedForDeletion;
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
		switch (compressionMethod) {
			case ZLIB:
				return "0x00000002";
			case PKWARE:
				return "0x00000008";
			case BZIP2:
				return "0x00000010";
			case LZMA:
				return "0x00000012";
			case SPARSE:
				return "0x00000020";
			case SPARSE_ZLIB:
				return "0x00000022";
			case SPARSE_BZIP2:
				return "0x00000030";
			default:
				return "0x00000000";
		}
	}
	
	public MpqEditorCompressionRuleMethod getCompressionMethod() {
		return compressionMethod;
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
	
}
