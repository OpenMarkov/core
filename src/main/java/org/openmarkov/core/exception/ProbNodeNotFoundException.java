package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class ProbNodeNotFoundException extends Exception {

	// Constructor
	/** @param networkName TODO
	 * @param message */
	public ProbNodeNotFoundException(String networkName, String variableName) {
		super("Variable: " + variableName + 
				" not found in network " + networkName + ".");
	}

}
