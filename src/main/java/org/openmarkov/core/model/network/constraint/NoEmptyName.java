package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.ChangeVariableNameEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "NoEmptyName", defaultBehavior = ConstraintBehavior.YES)
public class NoEmptyName extends PNConstraint {

	@Override
	public boolean checkEvent(UndoableEditEvent event)
	throws NotEnoughMemoryException, NonProjectablePotentialException,
	WrongCriterionException {
		// AddVariableEdit
		ArrayList<PNEdit> edits = UtilConstraints.getEditsType(event,
				AddVariableEdit.class);
		for (PNEdit edit : edits) {
			String name = ((AddVariableEdit) edit).getVariable().getName();
			if ((name == null) || (name.contentEquals(""))) {
				return false;
			}
		}
		// ChangeVariableNameEdit
		edits = UtilConstraints.getEditsType(event,
				ChangeVariableNameEdit.class);
		for (PNEdit edit : edits) {
			String name = ((ChangeVariableNameEdit) edit).getNewName();
			if ((name == null) || (name.contentEquals(""))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			String name = variable.getName();
			if ((name == null) || (name.contentEquals(""))) {
				return false;
			}
		}
		return true;
	}


    @Override
    protected String getMessage ()
    {
        return "there should be no empty names";
    }

}
