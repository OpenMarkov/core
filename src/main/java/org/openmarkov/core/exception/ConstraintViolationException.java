/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * Thrown when trying to do an edit that violates one of the
 * <code>PNConstraints</code> of the <code>ProbNet</code>
 *
 * @see org.openmarkov.core.model.graph.Link#Link(Object, Object, boolean)
 */
@SuppressWarnings("serial") public class ConstraintViolationException extends Exception {

	// Constructor

	/**
	 * @param message <code>String</code>
	 */
	public ConstraintViolationException(String message) {
		super(message);
	}

}
