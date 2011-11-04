package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class DoEditException extends Exception {

	/** @param msg. <code>String</code> */
	public DoEditException(String msg) {
		super(msg);
	}
	
	/** Writes the <code>exception</code> message and its stack trace.
	 * @param exception. <code>Exception</code> */
	public DoEditException(Exception exception) {
		super(exception.getMessage() + "\n" + exception.getStackTrace());
	}
	
}
