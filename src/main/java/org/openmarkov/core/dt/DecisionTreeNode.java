/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.dt;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

public class DecisionTreeNode implements DecisionTreeElement {
	protected double utility = Double.NEGATIVE_INFINITY;
	protected double scenarioProbability = Double.NEGATIVE_INFINITY;
	private Variable variable = null;
	private NodeType nodeType = null;
	private List<DecisionTreeElement> children = null;
	private DecisionTreeElement parent = null;
	private ProbNet network;

	
	public DecisionTreeNode(Node node) {
		this.variable = node.getVariable();
		this.nodeType = node.getNodeType();
		List<Potential> potentials = node.getPotentials();
		children = new ArrayList<>();
	}
	
	
	public DecisionTreeNode(Node node, ProbNet network) {
		this(node);
		this.network = network;
	}

	public DecisionTreeNode(Variable variable, ProbNet probNet) {
		this(probNet.getNode(variable), probNet);
	}

	/**
	 * Returns the variable.
	 *
	 * @return the Variable.
	 */
	public Variable getVariable() {
		return variable;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * Returns the children.
	 *
	 * @return the children.
	 */
	public List<DecisionTreeElement> getChildren() {
		return children;
	}

	public double getUtility() {
 		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}

	public EvidenceCase getBranchStates() {
		return (parent != null) ? parent.getBranchStates() : new EvidenceCase();
	}

	public boolean isBestDecision(DecisionTreeElement branch) {
		boolean isBestDecision = false;
		if (nodeType == NodeType.DECISION) {
			isBestDecision = true;
			double thisUtility = branch.getUtility();
			for (DecisionTreeElement otherBranch : children) {
				isBestDecision &= thisUtility >= otherBranch.getUtility();
			}
		}
		return isBestDecision;
	}

	public double getScenarioProbability() {
		return scenarioProbability;
	}

	public void setScenarioProbability(double scenarioProbability) {
		this.scenarioProbability = scenarioProbability;
	}

	public void addChild(DecisionTreeElement child) {
		child.setParent(this);
		children.add(child);
	}

	@Override public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DecisionTreeNode [variable=");
		builder.append(variable.getName());
		builder.append(", children=").append(children);
		builder.append("]");
		return builder.toString();
	}

	@Override public void setParent(DecisionTreeElement parent) {
		this.parent = parent;
	}
	
	public ProbNet getNetwork() {
		return network;
	}
	
	public void copy(DecisionTreeNode node) {
		utility = node.utility;
		scenarioProbability = node.scenarioProbability;
		variable = node.variable;
		nodeType = node.nodeType;
		children = node.children;
		parent = node.parent;
		network = node.network;
	}



}
