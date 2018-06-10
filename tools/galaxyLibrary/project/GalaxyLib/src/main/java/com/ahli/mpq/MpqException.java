package com.ahli.mpq;

/**
 * Exception created while handling MPQ archives.
 *
 * @author Ahli
 */
public class MpqException extends Exception {
	
	/**
	 * @param string
	 */
	public MpqException(final String string) {
		super(string);
	}
	
	/**
	 * @param e
	 */
	public MpqException(final Exception e) {
		super(e);
	}
	
	/**
	 * @param msg
	 * @param e
	 */
	public MpqException(final String msg, final Exception e) {
		super(msg, e);
	}
}
