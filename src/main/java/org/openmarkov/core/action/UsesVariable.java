package org.openmarkov.core.action;

import org.openmarkov.core.model.network.Variable;

/** Declares a method <code>getVariable</code> used in edits that manages one 
 * single variable. */
public interface UsesVariable {

	/** @return A <code>Variable</code> */
	public Variable getVariable();
	
}
