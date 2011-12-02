package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;


/**
 * Only continuous variables constraint.
 * 
 * @author mpalacios based in OnlyDiscreteVariables class.
 * @version 1.0 
 *  */

@Constraint (name = "OnlyContinuousVariables", defaultBehavior = ConstraintBehavior.NO)
public class OnlyContinuousVariables extends PNConstraint {
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			Variable variable = ((AddVariableEdit)edit).getVariable(); 
			if (variable.getVariableType() != VariableType.NUMERIC) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			if (variable.getVariableType() != VariableType.NUMERIC) {
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "all variables must be continuous";
    }

}
