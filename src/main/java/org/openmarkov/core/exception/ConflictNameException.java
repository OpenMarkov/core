/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

@OpenMarkovException(name = "ConflictNameException")
@SuppressWarnings("serial") public class ConflictNameException extends Exception {

	public ConflictNameException(String msg) {
		super(msg);
	}
}
