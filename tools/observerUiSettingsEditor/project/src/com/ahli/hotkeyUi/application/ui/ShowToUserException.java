package com.ahli.hotkeyUi.application.ui;

/**
 * An Exception that is meant to be shown to the user of the application. The
 * Exception should be localized.
 * 
 * @author Ahli
 */
public final class ShowToUserException extends Exception {
	
	private static final long serialVersionUID = 6225880770962102124L;
	
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
