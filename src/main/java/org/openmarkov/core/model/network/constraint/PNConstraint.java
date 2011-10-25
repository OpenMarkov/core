package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;


/** A constraint is a condition that a model must fulfill.<p>
  * This interface inherits from <code>UndoableEditListener</code> because like
  * that all the classes that implement this interface will be able to receive 
  * the same messages than <code>UndoableEditListener</code> and they will be 
  * able to be referenced with same identifier. */
public interface PNConstraint extends PNUndoableEditListener {

	/** @param probNet. <code>ProbNet</code>
	 * @return <code>true</code> if the <code>probNet</code> fulfills the 
	 * constraint. */
	public boolean checkProbNet(ProbNet probNet);
	
	/** Make sure all editions of the event do not violate restrictions.
	 * @param probNet. <code>ProbNet</code>
	 * @param event <code>UndoableEditEvent</code>
     * @return <code>true</code> if the <code>ProbNet</code> will fulfill the
	 *  constraint after applying the <code>event</code> in a 
	 *  <code>ProbNet</code> that previously fulfilled the constraint. 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException;
	
}
