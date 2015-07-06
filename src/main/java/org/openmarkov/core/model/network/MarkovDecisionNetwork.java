/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MarkovNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * This class is a type of Markov network created from an influence diagram that
 * can generate and store the partial order.
 * @author Manuel Arias
 */
public class MarkovDecisionNetwork extends ProbNet {

	// Attributes
/*	*//** Partial partialOrder of chance and decision nodes *//*
	private PartialOrder partialOrder;*/

	private Set<TablePotential> constantPotentials;




	@Override
	public MarkovDecisionNetwork copy() {
		MarkovDecisionNetwork copyNet = new MarkovDecisionNetwork(MarkovNetworkType.getUniqueInstance());
		//copyNet.partialOrder = partialOrder;
		copyNet = (MarkovDecisionNetwork) auxCopy(copyNet);
		try {
			copyNet.setNetworkType(MarkovNetworkType.getUniqueInstance());
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		copyNet.constantPotentials = new HashSet<>();
		copyNet.constantPotentials.addAll(constantPotentials);
		return copyNet;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}


	/**
	 *
	 * Creates a <code>MarkovDecisionNetwork</code> without utility nodes from
	 * the received network. It computes the partial order if 'useTrivialPartialOrder' is false; otherwise, it
	 * considers that all the variables are unordered.
	 * @param originalNet ProbNet
	 */
	public MarkovDecisionNetwork(ProbNet originalNet) {
		super();
		//constructPartialOrder(originalNet);
		addVariablesAndUndirectedLinks(originalNet);
		constantPotentials = new HashSet<>();
	}

/*	public void constructPartialOrder(ProbNet originalNet) {
		partialOrder = new PartialOrder(originalNet);
	}*/

	/**
	 * Adds the variables in the received <code>Potential</code> to this
	 * <code>MarkovNet</code>, creates links between those variables creating
	 * cliques and assigns the <code>potential</code> to the conditioned
	 * variable (the first one).
	 *
	 * @argCondition At least one potential depends on at least one variable
	 *               (otherwise the network would have no node, and it would be
	 *               impossible to assign constant potentials)
	 * @param projectedTablePotentials
	 *            <code>ArrayList</code> of <code>Potential</code>s
	 * @return A Markov Network in witch potentials are used to create cliques.
	 *         (<code>ProbNet</code>).
	 */
	public MarkovDecisionNetwork(ProbNet originalNet,
								 List<? extends Potential> projectedTablePotentials) {
		super(MarkovNetworkType.getUniqueInstance());
		//constructPartialOrder(originalNet);
		try {
			addConstraint(new OnlyUndirectedLinks(), true);
			// addConstraint (new OnlyDiscreteVariables (), false);
		} catch (ConstraintViolationException e) {
			// Unreachable code
			e.printStackTrace();
		}
		for (Potential potential : projectedTablePotentials) {
			if (potential.getVariables().size()>0)
			{
				addPotential(originalNet, potential);
			}
			else
			{
				constantPotentials.add((TablePotential) potential);
			}
		}
	}

	/**
	 * @param networkType
	 */
	public MarkovDecisionNetwork(NetworkType networkType) {
		// TODO Auto-generated constructor stub
	}


	/**
	 * Adds the received potential to the list of potentials of the first
	 * variable.
	 *
	 * @param originalNet
	 *            . <code>ProbNet</code>
	 * @preCondition network contains at least one variable
	 * @argCondition If A is the first variable in the potential and
	 *               B<sub>0</sub> ... B<sub>n</sub> are the others, there must
	 *               be a directed link B<sub>i</sub> -> A for every variable
	 *               B<sub>i</sub> in the potential (other than A)
	 * @param potential
	 *            . <code>Potential</code>
	 * @return The <code>Node</code> in which the <code>potential</code>
	 *         received has been added.
	 */
	// TODO addPotential should be common to all ProbNet's
	public void addPotential(ProbNet originalNet, Potential potential) {
		List<Variable> potentialVariables = potential.getVariables();
		// the node where the potential will be stored
		// TODO hacerlo con edits
		if (potential.getVariables().size() == 0) {
			// it is a constant potential;
			// adds it to any variable of the network
			getNodes().get(0).addPotential(potential);
		} else {
			// the potential depends on several variables
			for (Variable variable : potentialVariables) {
				// if (originalNet.getNode(variable)!=null){
				if (getNode(variable) == null) {
					Node node = originalNet.getNode(variable);
					NodeType nodeType = node.getNodeType();
					addNode(variable, nodeType);
				}
				// }
			}
			getNode(potentialVariables.get(0)).addPotential(potential);
			int numVariables = potentialVariables.size();
			for (int i = 0; i < numVariables - 1; i++) {
				Variable variable1 = potentialVariables.get(i);
				for (int j = i + 1; j < numVariables; j++) {
					Variable variable2 = potentialVariables.get(j);
					try {
						addLink(variable1, variable2, false);
					} catch (NodeNotFoundException e) {
						// Unreachable code because the variables are in the net
					}
				}
			}
		}
	}

	/**
	 * Adds chance and decision nodes to this object from originalID
	 *
	 * @param originalID
	 *            . <code>ProbNet</code>
	 */
	// TODO addVariablesAndLinks should be common to all ProbNet's
	private void addVariablesAndUndirectedLinks(ProbNet originalID) {
		for (List<Variable> variables : BasicOperations.calculatePartialOrder(originalID)) {
			for (Variable variable : variables) {
				Node node = originalID.getNode(variable);
				NodeType nodeType = node.getNodeType();
				addNode(variable, nodeType);
			}
		}

		List<Potential> potentials = originalID.getPotentials();
		for (Potential potential : potentials) {
			addPotential(potential);
		}
	}

	// Methods
	/**
	 * @param potential
	 *            <code>Potential</code>
	 * @argCondition All potential variables already exists in this network
	 */
	// TODO addPotential should be common to all ProbNet's
	public Node addPotential(Potential potential) {
		int numVariables = potential.getNumVariables();
		Node node = null;
		if (numVariables >= 1) {
			if (numVariables > 1) { // creates a clique using undirected links
				addUndirectedLinks(potential);
			}
			// add the potential to the corresponding node in the MarkovNet
			node = getNode(potential.getVariable(0));
			node.addPotential(potential);
		} else { // The potential is a constant.
			constantPotentials.add((TablePotential) potential);
		}
		return node;
	}

/*	*//**
	 * @return the partial order
	 *//*
	public PartialOrder getPartialOrder() {
		return partialOrder;
	}*/

	/**
	 * Creates a clique with undirected links between the nodes of the received
	 * <code>potential</code>.
	 *
	 * @argCondition All the potential variables belongs to this network.
	 * @param potential
	 *            <code>Potential</code>
	 */
	// TODO addLinks should be common to all ProbNet's
	private void addUndirectedLinks(Potential potential) {
		List<Variable> variablesPotential = potential.getVariables();
		int potentialSize = variablesPotential.size();
		for (int i = 0; i < potentialSize - 1; i++) {
			Node node1 = getNode(variablesPotential.get(i));
			for (int j = i + 1; j < potentialSize; j++) {
				Node node2 = getNode(variablesPotential.get(j));
				if (!isSibling(node1, node2)) {
					addLink(node1, node2, false);
				}
			}
		}
	}

	/**
	 * @param potentials a Collection<TablePotential>
	 */
	public void removePotentials(Collection<TablePotential> potentials) {
		for (TablePotential potential : potentials)	{
			this.removePotential(potential);
		}
	}

	@Override
	public Node removePotential(Potential potential) {
		Node node;
		if (potential.getVariables().size()>0){
			node = super.removePotential(potential);
		}
		else{
			constantPotentials.remove(potential);
			node = null;
		}
		return node;
	}

	/**
	 * @param role a PotentialRole
	 */
	public void removePotentials(PotentialRole role) {
		for (Potential pot:this.getPotentials()){
			if (pot.getPotentialRole()==role){
				removePotential(pot);
			}
		}
		constantPotentials = new HashSet<>();

	}

	@Override
	public List<Potential> getPotentials() {
		List<Potential> pots = super.getPotentials();
		pots.addAll(constantPotentials);
		return pots;
	}

	@Override
	public List<Potential> getPotentialsByRole(PotentialRole role) {
		List<Potential> pots = super.getPotentialsByRole(role);
		for(Potential constantPotential : constantPotentials)
		{
			if(constantPotential.getPotentialRole().equals(role))
			{
				pots.add(constantPotential);
			}
		}
		return pots;
	}

	public Set<TablePotential> getConstantPotentials() {
		return constantPotentials;
	}

	public void setConstantPotentials(Set<TablePotential> constantPotentials) {
		this.constantPotentials = constantPotentials;
	}

}
