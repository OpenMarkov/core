/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

@OpenMarkovException(name = "ConstraintException")
@SuppressWarnings("serial") public class ConstraintException extends Exception {

	public ConstraintException(String className, String message) {
		super("Error getting Instance for " + className + " . " + message);
	}

	public ConstraintException(String className) {
		this(className, "");
	}

}
