package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.potential.Potential;


@Constraint (name = "AllChanceVariablesHaveChancePotentials", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class AllChanceVariablesHaveChancePotentials extends PNConstraint {
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) {
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> chanceNodes = probNet.getProbNodes(NodeType.CHANCE);
		for (ProbNode chanceNode : chanceNodes) {
			Variable variable = chanceNode.getVariable();
			ArrayList<Potential> potentialsNode = chanceNode.getPotentials();
			boolean hasPotential = false;
			for (Potential potential : potentialsNode) {
				hasPotential = hasPotential || 
				    (potential.getVariables().get(0) == variable);
			}
			if (!hasPotential) {
				return false;
			}
		}
		return true;
	}

	@Override
    protected String getMessage ()
    {
        return "chance variable without potential";
    }

}
