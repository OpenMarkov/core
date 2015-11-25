/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class NodeNotFoundException extends Exception {

	// Constructor
	/** @param message <code>String</code> */
	public NodeNotFoundException(String message) {
		super(message);
	}
	/** @param network TODO
	 * @param variableName */
	public NodeNotFoundException(ProbNet network, String variableName) {
		super("Variable: " + variableName + 
				" not found in network " + network.getName() + ".");
	}

    public NodeNotFoundException(Node node) {
        this(node.getProbNet(), node.getName());
    }
    
    public NodeNotFoundException(ProbNet network, Variable variable) {
        this(network, variable.getName());
    }

}
