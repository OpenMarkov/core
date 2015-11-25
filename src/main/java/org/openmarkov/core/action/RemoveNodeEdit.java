/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;


import org.apache.log4j.Logger;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

	

@SuppressWarnings("serial")
public class RemoveNodeEdit extends SimplePNEdit implements UsesVariable {

		// Attributes
		/** Node associated to variable */
		private Node node;
		
		protected Variable variable;
		
		
		// Constructor
		/** @param probNet <code>ProbNet</code>
		 * @param node <code>Node</code> */
		public RemoveNodeEdit(ProbNet probNet, Node node) {
			super(probNet);
			this.variable = node.getVariable();
			this.node = node;
		}
		
		public RemoveNodeEdit(ProbNet probNet, Variable variable) {
			super(probNet);
			this.variable = variable;
			this.node = probNet.getNode(variable);
		}

		// Methods
		@Override
		public void doEdit() throws DoEditException {
			if (node == null) {
					throw new DoEditException("Trying to access a null node");
			}
			probNet.removeNode(node);
			
		}
		
		public void undo() {
			super.undo();
			probNet.addNode(node);
		}

		/** @return nodeType <code>NodeType</code> */
		public NodeType getNodeType() {
			return node.getNodeType();
		}

		/** @return variable <code>Variable</code> */
		public Variable getVariable() {
			return variable;
		}

		public String toString() {
			StringBuilder buffer = new StringBuilder("RemoveNodeEdit: ");
			if (variable == null) {
				buffer.append("null");
			} else {
				buffer.append(variable.getName());
			}
			return buffer.toString();
		}

	

}
