package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.potential.Potential;


@Constraint (name = "UtilityNodes", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class UtilityNodes extends PNConstraint {
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) {
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> utilityNodes = 
			probNet.getProbNodes(NodeType.UTILITY);
		int numUtilityNodes = utilityNodes.size();
		if (numUtilityNodes == 0) {
			return false;
		} else { // check same number of utility nodes and utility potentials
			ArrayList<Potential> potentials = probNet.getPotentials();
			int numUtilityPontentials = 0;
			for (Potential potential : potentials) {
				if (potential.isUtility()) {
					numUtilityPontentials++;
				}
			}
			return (numUtilityPontentials == numUtilityNodes);
		}
	}

    @Override
    protected String getMessage ()
    {
        // TODO Auto-generated method stub
        return "";
    }

}
