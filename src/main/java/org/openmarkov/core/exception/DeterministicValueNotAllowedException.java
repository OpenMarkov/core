package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class DeterministicValueNotAllowedException extends Exception {

	// Constructor
	/** @param message <code>String</code> */
	public DeterministicValueNotAllowedException(String message) {
		super(message);
	}

}
