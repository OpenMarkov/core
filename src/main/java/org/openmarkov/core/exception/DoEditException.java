/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

@OpenMarkovException(name = "DoEditException")
@SuppressWarnings("serial") public class DoEditException extends Exception {
	/**
	 * @param msg . <code>String</code>
	 */
	public DoEditException(String msg) {
		super(msg);
	}

	/**
	 * Writes the <code>exception</code> message and its stack trace.
	 *
	 * @param exception . <code>Exception</code>
	 */
	public DoEditException(Exception exception) {
		super(exception.getMessage());
	}

}
