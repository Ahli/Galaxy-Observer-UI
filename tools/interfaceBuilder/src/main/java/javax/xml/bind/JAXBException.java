package javax.xml.bind;

import java.io.PrintWriter;

public class JAXBException extends Exception {
	
	static final long serialVersionUID = -5621384651494307979L;
	/**
	 * Vendor specific error code
	 */
	private final String errorCode;
	/**
	 * Exception reference
	 */
	private volatile Throwable linkedException;
	
	/**
	 * Construct a JAXBException with the specified detail message.  The errorCode and linkedException will default to
	 * null.
	 *
	 * @param message
	 * 		a description of the exception
	 */
	public JAXBException(final String message) {
		this(message, null, null);
	}
	
	/**
	 * Construct a JAXBException with the specified detail message, vendor specific errorCode, and linkedException.
	 *
	 * @param message
	 * 		a description of the exception
	 * @param errorCode
	 * 		a string specifying the vendor specific error code
	 * @param exception
	 * 		the linked exception
	 */
	public JAXBException(final String message, final String errorCode, final Throwable exception) {
		super(message);
		this.errorCode = errorCode;
		linkedException = exception;
	}
	
	/**
	 * Construct a JAXBException with the specified detail message and vendor specific errorCode.  The linkedException
	 * will default to null.
	 *
	 * @param message
	 * 		a description of the exception
	 * @param errorCode
	 * 		a string specifying the vendor specific error code
	 */
	public JAXBException(final String message, final String errorCode) {
		this(message, errorCode, null);
	}
	
	/**
	 * Construct a JAXBException with a linkedException.  The detail message and vendor specific errorCode will default
	 * to null.
	 *
	 * @param exception
	 * 		the linked exception
	 */
	public JAXBException(final Throwable exception) {
		this(null, null, exception);
	}
	
	/**
	 * Construct a JAXBException with the specified detail message and linkedException.  The errorCode will default to
	 * null.
	 *
	 * @param message
	 * 		a description of the exception
	 * @param exception
	 * 		the linked exception
	 */
	public JAXBException(final String message, final Throwable exception) {
		this(message, null, exception);
	}
	
	/**
	 * Get the vendor specific error code
	 *
	 * @return a string specifying the vendor specific error code
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	/**
	 * Get the linked exception
	 *
	 * @return the linked Exception, null if none exists
	 */
	public Throwable getLinkedException() {
		return linkedException;
	}
	
	/**
	 * Add a linked Exception.
	 *
	 * @param exception
	 * 		the linked Exception (A null value is permitted and indicates that the linked exception does not exist or is
	 * 		unknown).
	 */
	public void setLinkedException(final Throwable exception) {
		linkedException = exception;
	}
	
	/**
	 * Returns a short description of this JAXBException.
	 */
	@Override
	public String toString() {
		return linkedException == null ? super.toString() :
				super.toString() + "\n - with linked exception:\n[" + linkedException.toString() + "]";
	}
	
	/**
	 * Prints this JAXBException and its stack trace (including the stack trace of the linkedException if it is
	 * non-null) to the PrintStream.
	 *
	 * @param s
	 * 		PrintStream to use for output
	 */
	@Override
	public void printStackTrace(final java.io.PrintStream s) {
		super.printStackTrace(s);
	}
	
	/**
	 * Prints this JAXBException and its stack trace (including the stack trace of the linkedException if it is
	 * non-null) to {@code System.err}.
	 */
	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}
	
	/**
	 * Prints this JAXBException and its stack trace (including the stack trace of the linkedException if it is
	 * non-null) to the PrintWriter.
	 *
	 * @param s
	 * 		PrintWriter to use for output
	 */
	@Override
	public void printStackTrace(final PrintWriter s) {
		super.printStackTrace(s);
	}
	
	@Override
	public Throwable getCause() {
		return linkedException;
	}
}