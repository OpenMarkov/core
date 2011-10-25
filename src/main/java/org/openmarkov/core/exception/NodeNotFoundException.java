package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class NodeNotFoundException extends WrongGraphStructureException {

	// Constructor
	/** @param message <code>String</code> */
	public NodeNotFoundException(String message) {
		super(message);
	}

}
