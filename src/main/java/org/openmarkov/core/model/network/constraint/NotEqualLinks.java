package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import openmarkov.exceptions.CanNotDoEditException;
import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.exceptions.NonProjectablePotentialException;
import openmarkov.exceptions.NotEnoughMemoryException;
import openmarkov.exceptions.WrongCriterionException;
import openmarkov.networks.ProbNet;
import openmarkov.undo.PNUndoableEditEvent;

public class NotEqualLinks implements PNConstraint{

	
private static NotEqualLinks constraint=null;
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>NotEqualLinks</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new NotEqualLinks();
		}
		return constraint;
	}
	
	
	
	
	
	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException,
			NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		// TODO Auto-generated method stub
		
	}

	public void undoEditHappened(PNUndoableEditEvent event) {
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
	public boolean checkEvent(UndoableEditEvent event)
			throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		// TODO Auto-generated method stub
		return false;
	}

}
