/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;

/** Removes a node performing this steps:<ol>
 * <li>Collect all potentials with this node variable
 * <li>Multiply and eliminates the variable
 * <li>Removes the collected potentials
 * <li>Adds to the <code>probNet</code> the new potential
 * <li>Adds links between the node siblings
 * <li>Remove links between the node and its children, parents and siblings
 * <li>Removes the node
 * </ol> */
@SuppressWarnings("serial")
public class CRemoveNodeEdit extends CompoundPNEdit implements UsesVariable{

	// Attributes
	protected Variable variable;
	
	protected NodeType nodeType;
	
	protected ArrayList<Node> parents;

	protected ArrayList<Node> children;

	protected ArrayList<Node> siblings;
	
	protected ArrayList<Potential> marginalizedPotentials;

	protected ArrayList<Potential> allPotentials;
	
	// Constructor
	/** @param probNet </code>ProbNet</code>
	 * @param variable <code>Variable</code> */
	public CRemoveNodeEdit(ProbNet probNet, Variable variable) {
		super(probNet);
		this.variable = variable;
		this.nodeType = probNet.getProbNode(variable).getNodeType();
	}

	public void generateEdits() throws NotEnoughMemoryException {
		ProbNode probNode = probNet.getProbNode(variable);
		Node node = probNode.getNode();

		// gets neighbors of this node
		parents = node.getParents();
		children = node.getChildren();
		siblings = node.getSiblings();
		
		// collect potentials of this node ...
		ArrayList<Potential> potentialsContainingVariable = 
			probNet.extractPotentials(variable);

		Potential newPotential = null;
		// ... multiply and eliminate the variable
		try {
			newPotential = PotentialOperations.multiplyAndEliminate(
				potentialsContainingVariable, variable);
        } catch (NotEnoughMemoryException e) {
            throw e;
        } catch (PotentialOperationException e) {
			e.printStackTrace();
		}
		
		for (Potential potential : potentialsContainingVariable) {
			edits.add(new RemovePotentialEdit(probNet, potential));
		}

		// add a link between the siblings of the removed node
		for (Node node1 : siblings) {
			for (Node node2 : siblings) {
				if ((node1 != node2) && (!node1.isSibling(node2))) {
					addEdit(new AddLinkEdit(probNet, 
						((ProbNode)node1.getObject()).getVariable(), 
						((ProbNode)node2.getObject()).getVariable(), false, false));
				}
			}
		}
		
		// remove links between probNode and its parents, children and siblings
		for (Node parent : parents) {
			addEdit(new RemoveLinkEdit(probNet, 
				((ProbNode)parent.getObject()).getVariable(), 
				probNode.getVariable(), true, false));
		}
		for (Node child : children) {
			Variable variable = ((ProbNode)child.getObject()).getVariable();
			addEdit(new RemoveLinkEdit(probNet,	probNode.getVariable(), variable, true, false));
		}
		for (Node sibling : siblings) {
			addEdit(new RemoveLinkEdit(probNet, 
				((ProbNode)sibling.getObject()).getVariable(), 
				probNode.getVariable(), false, false));
		}

		// add edit to remove the variable
		addEdit(new RemoveNodeEdit(probNet, variable));
		
		// add edit to add the new potential
		edits.add(new AddPotentialEdit(probNet, newPotential));
	}
	
	public void undo() {
		super.undo();
	}

	/** @return variable <code>Variable</code> */
	public Variable getVariable() {
		return variable;
	}

	/** @return <code>String</code> */
	public String toString() {
		return new String("CompoundRemoveNodeEdit: " +	variable);
	}

}
