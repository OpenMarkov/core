package org.openmarkov.core.dt;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;

public class DANDecisionTreeNode extends DecisionTreeNode {
	
	protected ProbNet dan;	

	public DANDecisionTreeNode(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

}
