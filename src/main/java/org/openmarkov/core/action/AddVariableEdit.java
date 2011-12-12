/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class AddVariableEdit extends SimplePNEdit {

	// Attributes
	private Variable variable;
	
	private NodeType nodeType;

	// Constructor
	/** @param probNet <code>ProbNet</code>
	 * @param variable <code>Variable</code>
	 * @param nodeType <code>NodeType</code> */
	public AddVariableEdit(
			ProbNet probNet, Variable variable, NodeType nodeType) {
		super(probNet);
		this.variable = variable;
		this.nodeType = nodeType;
	}

	// Methods
	@Override
	public void doEdit() {
		if (variable != null) {
			probNet.addVariable(variable, nodeType);
		}
	}
	
	public void undo() {
		super.undo();
		if (variable != null) {
			probNet.removeProbNode(probNet.getProbNode(variable));
		}
	}
	
	/** @return variable <code>Variable</code> */
	public Variable getVariable() {
		return variable;
	}

	/** @return nodeType <code>NodeType</code> */
	public NodeType getNodeType() {
		return nodeType;
	}
	
	public String toString() {
		return new String("AddNodeEdit: " +	variable.getName());
	}

}
