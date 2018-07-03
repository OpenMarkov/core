/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;
@SuppressWarnings("serial") public class DivideByZeroException extends OpenMarkovException {

	// Constructor

	/**
	 * @param message <code>String</code>
	 */
	public DivideByZeroException(String message) {
		super(message);
	}

}
