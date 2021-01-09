// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.exception;

import java.io.Serial;

/**
 * Exception of the UI Parsing/Validation.
 *
 * @author Ahli
 */
public class UIException extends Exception {
	
	@Serial
	private static final long serialVersionUID = -4719408127547238653L;
	
	/**
	 * Constructor.
	 *
	 * @param message
	 * 		the message
	 */
	public UIException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor.
	 *
	 * @param message
	 * 		the message
	 * @param e
	 * 		the contained Exception
	 */
	public UIException(final String message, final Exception e) {
		super(message, e);
	}
	
	/**
	 * Constructor.
	 *
	 * @param e
	 * 		the contained Exception
	 */
	public UIException(final Exception e) {
		super(e);
	}
}
