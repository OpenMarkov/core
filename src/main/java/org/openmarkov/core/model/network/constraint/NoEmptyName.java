package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.ChangeVariableNameEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class NoEmptyName implements PNConstraint {

	// Attributes.
	private static NoEmptyName constraint = null;

	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private NoEmptyName() {
	}

	// Methods
	/**
	 * Singleton pattern.
	 * 
	 * @return The unique instance. <code>DistinctVariableNames</code>
	 */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoEmptyName();
		}
		return constraint;
	}

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
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException,
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
					"ConstraintViolationException adding variable with a name that"
							+ "already exists in probNet.");
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
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

	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub

	}

}
