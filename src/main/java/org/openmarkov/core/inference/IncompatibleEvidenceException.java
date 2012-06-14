package org.openmarkov.core.inference;

@SuppressWarnings("serial")
public class IncompatibleEvidenceException extends Exception
{
	/** @param message */
	public IncompatibleEvidenceException(String message) {
		super(message);
	}
}
