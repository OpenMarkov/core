/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * This class implements explicit links.
 * 
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 * @see openmarkov.graphs.Node
 * @see openmarkov.graphs.Graph
 */
public class Link {

	// Attributes
	/** The first node. If the link is directed, this node is the parent. */
	private Node node1;

	/** The first node. If the link is directed, this node is the parent. */
	private Node node2;

	/** If true, the link is directed. Otherwise, it is an undirected link. */
	private boolean directed;

	/***
	 * If true, the link has a link restriction.
	 */
	private boolean linkRestriction;

	/****
	 * Potential that contains the value of compatibility for the combinations
	 * of the variables of node1 and node2
	 */
	private TablePotential restrictionsPotential;

	// Constructors
	/**
	 * Creates an unlabelled link and sets the cross references in the nodes.
	 * This constructor should be called only from the <code>addLink</code>
	 * function in the class Graph.
	 * 
	 * @param node1
	 *            <code>Node</code>.
	 * @param node2
	 *            <code>Node</code>.
	 * @param directed
	 *            <code>boolean</code>.
	 * @argCondition Both nodes must belong to the same graph.
	 */
	public Link(Node node1, Node node2, boolean directed) {
		Graph graph = node1.getGraph();
		this.node1 = node1;
		this.node2 = node2;
		this.directed = directed;
		graph.uf_addImplicitLink(this);
		node1.uf_addLink(this);
		node2.uf_addLink(this);
		linkRestriction = false;
	}

	// Methods
	/**
	 * @return The parent (if the link is directed) or the first node (if the
	 *         link is undirected).
	 * @consultation
	 */
	public Node getNode1() {
		return node1;
	}

	/**
	 * @return The child (if the link is directed) or the second node (if the
	 *         link is undirected).
	 * @consultation
	 */
	public Node getNode2() {
		return node2;
	}

	/**
	 * @param node
	 *            <code>Node</code>.
	 * @return <code>true</code> if the link contains <code>node</code>.
	 * @consultation
	 */
	public boolean contains(Node node) {
		return ((node1 == node) || (node2 == node));
	}

	/**
	 * @return <code>true</code> if the link is directed, false if it is
	 *         undirected
	 * @consultation
	 */
	public boolean isDirected() {
		return directed;
	}

	/******
	 * @return<code>true</code> if the link has a linkRestriction
	 *                          associates,false otherwise
	 * @consultation
	 */
	public boolean hasRestrictions() {
		return linkRestriction;
	}

	/**
	 * Initializes a TablePotential for the variable associated to node1 and
	 * node2, whose values are all 1.
	 * 
	 * @throws NotEnoughMemoryException
	 */
	public void initializesRestrictionsPotential()
			throws NotEnoughMemoryException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(((ProbNode) node1.getObject()).getVariable());
		variables.add(((ProbNode) node2.getObject()).getVariable());
		restrictionsPotential = new TablePotential(variables,
				PotentialRole.LINK_RESTRICTION);
		linkRestriction = true;
	}

	/*****
	 * Resets the TablePotential for the variables associated to node1 and node2
	 * to its initial state.
	 * 
	 * @throws NotEnoughMemoryException
	 */
	public void resetRestrictionsPotential() throws NotEnoughMemoryException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(((ProbNode) node1.getObject()).getVariable());
		variables.add(((ProbNode) node2.getObject()).getVariable());
		restrictionsPotential = new TablePotential(variables,
				PotentialRole.LINK_RESTRICTION);
		linkRestriction = false;
	}

	/*****
	 * Assigns the value of the parameter compatibility to the combination of
	 * the variables state1 and state2.
	 * 
	 * @param state1
	 *            state of the variable of node1
	 * @param state2
	 *            state of the variable of node2
	 * @param compatibility
	 *            value of compatibility
	 */
	public void setCompatibilityValue(State state1, State state2,
			int compatibility) {
		int[] indexes = new int[2];
		indexes[0] = restrictionsPotential.getVariable(0).getStateIndex(state1);
		indexes[1] = restrictionsPotential.getVariable(1).getStateIndex(state2);
		ArrayList<Variable> variables = restrictionsPotential.getVariables();
		restrictionsPotential.setValue(variables, indexes, compatibility);
	}

	/******
	 * Returns the compatibility value of the combination of state1 and state2.
	 * 
	 * @param state1
	 *            state of the variable of node1.
	 * @param state2
	 *            state of the variable of node2.
	 * @return the value 1 for compatibility and 0 for incompatibility.
	 */

	public int areCompatible(State state1, State state2) {
		int[] indexes = new int[2];
		indexes[0] = restrictionsPotential.getVariable(0).getStateIndex(state1);
		indexes[1] = restrictionsPotential.getVariable(1).getStateIndex(state2);
		ArrayList<Variable> variables = restrictionsPotential.getVariables();

		return (int) restrictionsPotential.getValue(variables, indexes);

	}

	/****
	 * 
	 * @return the potential of the the link restriction.
	 */
	public Potential getRestrictionsPotential() {
		return restrictionsPotential;
	}

	/****
	 * Assigns the potential to the restrictionPotential of the link
	 * @param potential
	 */

	public void setRestrictionsPotential(Potential potential) {
		this.restrictionsPotential = (TablePotential) potential;
	}

	/** @return String */
	public String toString() {
		StringBuffer buffer = new StringBuffer(node1.toString());
		if (directed) {
			buffer.append(" --- ");
		} else {
			buffer.append(" --> ");
		}
		buffer.append(node2.toString());
		return buffer.toString();
	}

}
