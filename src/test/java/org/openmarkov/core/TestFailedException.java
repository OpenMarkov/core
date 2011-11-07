package org.openmarkov.core;

@SuppressWarnings("serial")
public class TestFailedException extends Exception {

	public TestFailedException(Exception e) {
		System.err.println(e.getMessage());
	}

}
