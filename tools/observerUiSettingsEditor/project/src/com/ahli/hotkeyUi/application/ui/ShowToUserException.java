package com.ahli.hotkeyUi.application.ui;

/**
 * An Exception that is meant to be shown to the user of the application. The
 * Exception should be localized.
 * 
 * @author Ahli
 *
 */
public class ShowToUserException extends Exception {
	
	public ShowToUserException(String msg) {
		super(msg);
	}
	
	private static final long serialVersionUID = 6225880770962102124L;
	
}
