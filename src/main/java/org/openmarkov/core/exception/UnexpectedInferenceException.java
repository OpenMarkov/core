/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

@OpenMarkovException(name = "UnexpectedInferenceException")
@SuppressWarnings("serial") public class UnexpectedInferenceException extends Exception {

	// Constructor

	/**
	 * @param message
	 */
	public UnexpectedInferenceException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnexpectedInferenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
