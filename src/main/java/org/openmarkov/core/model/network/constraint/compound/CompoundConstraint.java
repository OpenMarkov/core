package org.openmarkov.core.model.network.constraint.compound;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;


/** A compound constraint is full-filled when all the constraints that are 
 *  included are full-filled. */
public class CompoundConstraint implements PNConstraint {

	// Attributes
	/** List of constraints that compounds this PNConstraint */
	protected ArrayList<PNConstraint> constraints;
	
	// Constructor
	public CompoundConstraint() {
		constraints = new ArrayList<PNConstraint>();
	}

	// Methods
	@Override
	/** Check event in every constraint.
	 * @param event. <code>UndoableEditEvent</code> */
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		for (PNConstraint constraint : constraints) {
			if (!constraint.checkEvent(event)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** Check event in every constraint.
	 * @param event. <code>PNUndoableEditEvent</code> */
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		for (PNConstraint constraint : constraints) {
			constraint.undoableEditWillHappen(event);
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
	}

	@Override
	/** Checks all the constraints.
	 * @param probNet. <code>ProbNet</code> */
	public boolean checkProbNet(ProbNet probNet) {
		for (PNConstraint constraint : constraints) {
			if (!constraint.checkProbNet(probNet)) {
				return false;
			}
		}
		return true;
	}
	
	/** @return <code>true</code> if <code>constraintClass</code> is included
	 *  in <code>constraints</code>. */
	public boolean hasConstraint(Class<?> constraintClass) {
		for (PNConstraint constraint : constraints) {
			if (constraint.getClass() == constraintClass) {
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
