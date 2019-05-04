// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.ui;

/**
 * An Exception that is meant to be shown to the user of the application. The Exception should be localized.
 *
 * @author Ahli
 */
public final class ShowToUserException extends Exception {
	
	/**
	 * Constructor.
	 *
	 * @param msg
	 */
	public ShowToUserException(final String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 *
	 * @param msg
	 * @param e
	 */
	public ShowToUserException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Constructor.
	 *
	 * @param e
	 */
	public ShowToUserException(final Exception e) {
		super(e);
	}
	
}
