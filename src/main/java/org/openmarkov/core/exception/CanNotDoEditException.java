/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;
@OpenMarkovException(name = "CanNotDoEditException")
@SuppressWarnings("serial") public class CanNotDoEditException extends Exception {

	// Constructor

	/**
	 * @param message message of the exception
	 */
	public CanNotDoEditException(String message) {
		super(message);
	}

}
