package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;

public class UnlabeledLinks implements PNConstraint {

	
	private static UnlabeledLinks constraint=null;
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>UnlabeledLinks</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new UnlabeledLinks();
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

	@Override
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
