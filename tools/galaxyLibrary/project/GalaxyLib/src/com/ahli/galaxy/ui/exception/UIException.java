package com.ahli.galaxy.ui.exception;

/**
 * Exception of the UI Parsing/Validation.
 *
 * @author Ahli
 */
public class UIException extends Exception {
	
	/**
	 *
	 */
	private static final long serialVersionUID = -3550226727396888948L;
	
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
