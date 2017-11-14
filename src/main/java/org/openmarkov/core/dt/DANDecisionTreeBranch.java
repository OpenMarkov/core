package org.openmarkov.core.dt;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

public class DANDecisionTreeBranch extends DecisionTreeBranch {

	@Override
	public double getUtility() {
		return utility;
	}

/*	@Override
	public double getBranchProbability() {
		return scenarioProbability;
	}*/

	@Override
	public double getScenarioProbability() {
		return scenarioProbability;
	}

	public DANDecisionTreeBranch(ProbNet probNet) {
		super(probNet);
	}

	public DANDecisionTreeBranch(ProbNet dan, Variable x, State state) {
		super(dan,x,state);
	}

}
