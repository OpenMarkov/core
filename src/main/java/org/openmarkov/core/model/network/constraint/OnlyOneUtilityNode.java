package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.ProbNet;


public class OnlyOneUtilityNode implements PNConstraint {

	// Attributes.
	private static OnlyOneUtilityNode constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	public OnlyOneUtilityNode() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>DistinctVariableNames</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new OnlyOneUtilityNode();
		}
		return constraint;
	}
	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
