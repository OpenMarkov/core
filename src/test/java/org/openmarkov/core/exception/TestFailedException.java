package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class TestFailedException extends Exception {

	public TestFailedException(Exception e) {
		System.err.println(e.getMessage());
	}

}
