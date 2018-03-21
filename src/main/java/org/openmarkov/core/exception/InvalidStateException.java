/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial") public class InvalidStateException extends Exception {

	// Constructor

	/**
	 * @param msg <code>String</code>
	 */
	public InvalidStateException(String msg) {
		super(msg);
	}

	/**
	 * Generate a message for this exception given a variable and a missing
	 * state
	 *
	 * @param variable     <code>Variable</code>
	 * @param missingState <code>String</code>
	 */
	public static String generateMsg(Variable variable, String missingState) {
		String msg = "InvalidStateException trying access state " + missingState + " in variable: " + variable.getName()
				+ ".\nThis variable has these states:";
		State[] states = variable.getStates();
		for (int i = 0; i < states.length - 1; i++) {
			msg = msg + states[i].toString() + ", ";
		}
		msg = msg + states[states.length - 1].toString() + ".";
		return msg;
	}

}
