package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class CanNotAccessFileException extends Exception {

	// Constructor
	/** @param fileName */
	public CanNotAccessFileException(String fileName) {
		super("It is not possible to access file: " + fileName);
	}

}
