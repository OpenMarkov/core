package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;


@Constraint (name = "OnlyChanceNodes", defaultBehavior = ConstraintBehavior.NO)
public class OnlyChanceNodes extends PNConstraint {

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> probNodes = probNet.getProbNodes();
		for (ProbNode probNode : probNodes) {
			if (probNode.getNodeType() != NodeType.CHANCE) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			if (((AddVariableEdit)edit).getNodeType() != NodeType.CHANCE) {
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "only chance nodes allowed";
    }

}
