/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.dt;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class DANDecisionTreeNode extends DecisionTreeNode {
	
	@Override
	public double getUtility() {
		return utility;
	}

	@Override
	public double getScenarioProbability() {
		return scenarioProbability;
	}

	protected ProbNet dan;	

	public DANDecisionTreeNode(Node node,ProbNet network) {
		super(node);
		dan = network;
	}
	
	public DANDecisionTreeNode(Variable variable,ProbNet network) {
		this(network.getNode(variable),network);
	}

}
