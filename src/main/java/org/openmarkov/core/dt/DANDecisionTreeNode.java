package org.openmarkov.core.dt;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class DANDecisionTreeNode extends DecisionTreeNode {
	
	protected ProbNet dan;	

	public DANDecisionTreeNode(Node node,ProbNet network) {
		super(node);
		dan = network;
	}
	
	public DANDecisionTreeNode(Variable variable,ProbNet network) {
		this(network.getNode(variable),network);
	}

}
