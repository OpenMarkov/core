package org.openmarkov.core.exception;

/** Thrown when the <code>Potential</code> cannot be projected into a set of 
 * <code>TablePotential</code>s given the evidence supplied.*/
@SuppressWarnings("serial")
public class NonProjectablePotentialException extends Exception {

	public NonProjectablePotentialException(String string) {
		super(string);
	}


}
