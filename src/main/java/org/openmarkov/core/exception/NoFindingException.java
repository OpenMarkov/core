/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Variable;

/**
 * OpenMarkov launches this exception when trying to access a <code>Variable</code>
 * in an <code>EvidenceCase</code> that does not exist.
 */
@OpenMarkovException(name = "NoFindingException")
@SuppressWarnings("serial") public class NoFindingException extends Exception {

	public NoFindingException(Variable variable) {
		super("The variable " + variable + " does not exists in EvidenceCase");
	}

	public NoFindingException(String variableName) {
		super("The variable " + variableName + " does not exists in EvidenceCase");
	}

}
