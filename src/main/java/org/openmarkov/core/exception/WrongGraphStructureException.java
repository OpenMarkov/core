package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class WrongGraphStructureException extends Exception {

	// Constructor
	/** @param message <code>String</code> */
	public WrongGraphStructureException(String message) {
		super(message);
	}
}
