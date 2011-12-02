package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Variable;

/** OpenMarkov launches this exception when trying to access a <code>Variable</code>
 * in an <code>EvidenceCase</code> that does not exist. */
@SuppressWarnings("serial")
public class NoFindingException extends Exception {

	public NoFindingException(Variable variable) {
		super("The variable " + variable + " does not exists in EvidenceCase");
	}

}
