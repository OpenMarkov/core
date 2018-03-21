/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * Thrown when the <code>Potential</code> cannot be projected into a set of
 * <code>TablePotential</code>s given the evidence supplied.
 */
@SuppressWarnings("serial") public class NonProjectablePotentialException extends Exception {

	public NonProjectablePotentialException(String string) {
		super(string);
	}

	public NonProjectablePotentialException(String string, Throwable cause) {
		super(string, cause);
	}

}
