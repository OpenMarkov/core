package org.openmarkov.core.exception;

public class NotEvaluableNetworkException extends Exception {

	private static final long serialVersionUID = -6555375975623328551L;

	// Constructor
	/** @param message <code>String</code> */
	public NotEvaluableNetworkException(Exception e) {
		super(e.getMessage());
	}

	public NotEvaluableNetworkException(String message) {
		super(message);
	}

}
