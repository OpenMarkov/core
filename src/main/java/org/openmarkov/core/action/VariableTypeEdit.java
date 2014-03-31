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
import java.util.List;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.Util;

@SuppressWarnings("serial")
public class VariableTypeEdit extends SimplePNEdit {
	// private ProbNet probNet;
	private Node probNode;
	private VariableType newType;
	private VariableType currentType;

	public VariableTypeEdit(Node probNode, VariableType newType) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.newType = newType;
		this.currentType = probNode.getVariable().getVariableType();

	}

	@Override
	public void doEdit() throws DoEditException {
		probNode.getVariable().setVariableType(newType);
		if (currentType != newType) {
			if ((newType.compareTo(VariableType.DISCRETIZED) == 0 && currentType
					.compareTo(VariableType.FINITE_STATES) == 0)
					|| newType.compareTo(VariableType.FINITE_STATES) != 0
					&& currentType.compareTo(VariableType.DISCRETIZED) == 0) {
				// from discretized to finite states or vice versa
			} else {
				// from numeric to finite states or discretized or vice versa
				//if child is utility to potential to be set depends on the type of the other parents
				//it is not always uniform
				setUniformPotential2ProbNode(probNode);
				for (Node child : probNet.getChildren(probNode)) {
					if (child.getNodeType() == NodeType.UTILITY) {
						List<Potential> newPotentials = new ArrayList<Potential>();
						if (child.onlyNumericalParents()) {// utility and numerical parents sum
							for (Potential oldPotential : child.getPotentials ())
							{
								// Update potential
								Potential newPotential = new SumPotential ( oldPotential.getVariables (),
										oldPotential.getPotentialRole ());
								newPotential.setUtilityVariable (oldPotential.getUtilityVariable ());
								newPotentials.add (newPotential);
							}
						}else if (!child.onlyNumericalParents()) {//mixture of finite states and numerical Uniform
							for (Potential oldPotential : child.getPotentials ())
							{
								// Update potential
								Potential newPotential = new UniformPotential (oldPotential.getVariables (),
										oldPotential.getPotentialRole ());
								newPotential.setUtilityVariable (oldPotential.getUtilityVariable ());
								newPotentials.add (newPotential);
							}
						}
						child.setPotentials (newPotentials);
					} else {
						//if child is not utility always change potential to Uniform
						setUniformPotential2ProbNode(child);
					}
				}
			}
		}
		if (currentType.compareTo(VariableType.NUMERIC) == 0 ) // if current type
			// is numeric
		{
			probNode.getVariable().setStates(
					probNode.getProbNet().getDefaultStates());
			List<Variable> variables = new ArrayList<Variable>();
			if (probNode.getNodeType() != NodeType.UTILITY) {
				variables.add(probNode.getVariable());
			}
			for (Node node : probNet.getParents(probNode)) {
				variables.add(node.getVariable());
			}
			UniformPotential uniformPotential = new UniformPotential(variables,
					probNode.getPotentials().get(0).getPotentialRole());
			List<Potential> potentials = new ArrayList<Potential>(1);
			potentials.add(uniformPotential);
			probNode.setPotentials(potentials);
			probNode.setUniformPotential();
		}

		resetLink(probNode);

	}

	@Override
	public void undo() {
		super.undo();
		probNode.getVariable().setVariableType(currentType);
	}

	public VariableType getNewVariableType() {
		return newType;
	}

	public Node getProbNode() {

		return this.probNode;
	}

	/****
	 * This method resets the link restriction of the links of the node
	 * 
	 * @param node
	 */
	private void resetLink(Node node) {

		List<Node> children = probNet.getChildren(node);
		for (Node child : children) {
			Link<Node> link = probNet.getLink(node, child, true);
			if (link.hasRevealingConditions()) {
				link.setRevealingIntervals(new ArrayList<PartitionedInterval>());
				link.setRevealingStates(new ArrayList<State>());
			}
		}

		for (Link<Node> link : probNet.getLinks(node)) {
			if (link.hasRestrictions()) {
				link.setRestrictionsPotential(null);
			}

		}
	}
	
	public void setUniformPotential2ProbNode(Node node) {
		
	    List<Potential> newListPotentials = new ArrayList<Potential> ();
	    List<Variable> variables = new ArrayList<Variable>();
		Variable thisVariable;
		List<Potential> potentials = node.getPotentials();
		PotentialRole role = potentials.get(0).getPotentialRole();
        // first, this variable. The potentials is not null
		if (node.getNodeType() == NodeType.UTILITY)
			thisVariable = potentials.get( 0 ).getUtilityVariable();
		else{
			thisVariable = potentials.get( 0 ).getVariable( 0 );
			variables.add(thisVariable);
		}
		
		int numOfCellsInTable = thisVariable.getNumStates();
		double initialValue = Util.round( 1 / (new Double(numOfCellsInTable)), 
				"0.01");
		    // add now all the parents 
		
		for (Node parent: node.getParents()) {
			//TODO Revisar, ¿Solo se agrega/elimina un padre a la vez?
			//mpalacios
			//the set of variables could be changed, so , have to be updated.
			variables.add(parent.getVariable());
			numOfCellsInTable *= parent.getVariable().getNumStates();
		}
		// sets a new table with new columns and with all the same values
		double[] table = new double[numOfCellsInTable] ;
		for (int i=0; i<numOfCellsInTable; i++) {
			table[i] = initialValue;
		}
		// and finally, create the potential and the list of potentials
		
		// TODO Comprobar que efectivamente es un CONDITIONAL_PROBABILITY
		UniformPotential uniformPotential = new UniformPotential(variables, role);
		
		newListPotentials.add( uniformPotential );
		
		if (node.getNodeType() == NodeType.UTILITY && role == PotentialRole.UTILITY){
			//tablePotential.getVariables().remove(0);
			uniformPotential.setUtilityVariable(thisVariable);
		}
		node.setPotentials(newListPotentials);
		
	}	

}
