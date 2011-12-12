package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class NotEnoughMemoryException extends PotentialOperationException {

	// Constructor
	/** @param message <code>String</code> */
	public NotEnoughMemoryException(String message) {
		super(message);
	}

}
