/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * @author marias
 * @version 1.0
 */
@OpenMarkovException(name = "IncompatibleEvidenceException")
@SuppressWarnings("serial") public class IncompatibleEvidenceException extends Exception {

	// Constructor

	/**
	 * @param message
	 */
	public IncompatibleEvidenceException(String message) {
		super(message);
	}

}
