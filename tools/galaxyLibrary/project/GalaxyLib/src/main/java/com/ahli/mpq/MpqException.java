// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import java.io.Serial;

/**
 * Exception created while handling MPQ archives.
 *
 * @author Ahli
 */
public class MpqException extends Exception {
	
	@Serial
	private static final long serialVersionUID = -2467412777989985295L;
	
	/**
	 * @param message
	 */
	public MpqException(final String message) {
		super(message);
	}
	
	/**
	 * @param e
	 */
	public MpqException(final Exception e) {
		super(e);
	}
	
	/**
	 * @param message
	 * @param e
	 */
	public MpqException(final String message, final Exception e) {
		super(message, e);
	}
}
