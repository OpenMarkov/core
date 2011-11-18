package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;


@Constraint (name = "OnlyDiscreteVariables", defaultBehavior = ConstraintBehavior.NO)
public class OnlyDiscreteVariables extends PNConstraint {
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			Variable variable = ((AddVariableEdit)edit).getVariable(); 
			if (variable.getVariableType() != VariableType.FINITE_STATES) {
				return false;
			}
		}
		edits = 
			UtilConstraints.getEditsType(event, VariableTypeEdit.class);
		for (PNEdit edit : edits) {
			VariableType newType = ((VariableTypeEdit)edit).getNewVariableType(); 
			if (newType != VariableType.FINITE_STATES && newType !=
				VariableType.DISCRETIZED) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			if (variable.getVariableType() != VariableType.DISCRETIZED) {
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "all variables must be discrete";
    }

}
