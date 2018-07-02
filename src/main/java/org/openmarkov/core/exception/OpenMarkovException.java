/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

public class OpenMarkovException extends Exception {

	public OpenMarkovException() { }

	public OpenMarkovException(String message) {
		super(message);
	}

	public OpenMarkovException(String message, Throwable cause) {
		super(message, cause);
	}
}
