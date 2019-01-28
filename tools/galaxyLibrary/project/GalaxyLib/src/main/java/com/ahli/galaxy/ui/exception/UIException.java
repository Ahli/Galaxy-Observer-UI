// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.exception;

/**
 * Exception of the UI Parsing/Validation.
 *
 * @author Ahli
 */
public class UIException extends Exception {
	
	/**
	 * Constructor.
	 *
	 * @param string
	 * 		the message
	 */
	public UIException(final String string) {
		super(string);
	}
	
	/**
	 * Constructor.
	 *
	 * @param string
	 * 		the message
	 * @param e
	 * 		the contained Exception
	 */
	public UIException(final String string, final Exception e) {
		super(string, e);
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
