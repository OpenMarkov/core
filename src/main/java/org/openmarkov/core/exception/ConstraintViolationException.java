package org.openmarkov.core.exception;

/** Thrown when trying to do an edit that violates one of the 
 * <code>PNConstraints</code> of the <code>ProbNet</code>
 * @see openmarkov.graphs.Link#Link(openmarkov.Node, openmarkov.Node, boolean) */
@SuppressWarnings("serial")
public class ConstraintViolationException extends Exception {

	// Constructor
	/** @param message <code>String</code> */
	public ConstraintViolationException(String message) {
		super(message);
	}

}
