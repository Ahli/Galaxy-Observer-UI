// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.ui;

import java.io.Serial;

/**
 * An Exception that is meant to be shown to the user of the application. The Exception should be localized.
 *
 * @author Ahli
 */
public final class ShowToUserException extends Exception {
	
	@Serial
	private static final long serialVersionUID = -464012745193110294L;
	
	/**
	 * Constructor.
	 *
	 * @param message
	 */
	public ShowToUserException(final String message) {
		super(message);
	}
	
}
