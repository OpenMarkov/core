package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;


public class OnlyDiscreteVariables implements PNConstraint {

	// Attributes.
	private static OnlyDiscreteVariables constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private OnlyDiscreteVariables() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. <code>OnlyDiscreteVariables</code> */
	public static OnlyDiscreteVariables getUniqueInstance() {
		if (constraint == null) {
			constraint = new OnlyDiscreteVariables();
		}
		return constraint;
	}
	
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
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding variable in probNet: "+
				"all variables must be discrete");
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
	}

	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
