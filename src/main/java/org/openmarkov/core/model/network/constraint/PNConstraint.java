/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;


/** A constraint is a condition that a model must fulfill.<p>
  * This class implements <code>PNUndoableEditListener</code> because like
  * that all the classes that implement this interface will be able to receive 
  * the same messages than <code>UndoableEditListener</code> and they will be 
  * able to be referenced with same identifier. */
public abstract class PNConstraint implements PNUndoableEditListener, Checkable {

    @Override
    public void undoableEditHappened (UndoableEditEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    /** Given a <code>probNet</code> that complies with this constraint, this
     * method checks that after the application of the <code>edit</code> 
     * contained in the <code>event</code> received, the 
     * <code>probNet</code> continues complying with this constraint. 
     * @param event <code>UndoableEditEvent</code>
     * @throws CanNotDoEditException 
     * @throws ConstraintViolationException 
     * @throws NotEnoughMemoryException 
     * @throws WrongCriterionException 
     * @throws NonProjectablePotentialException */
    @Override
    public void undoableEditWillHappen (PNUndoableEditEvent event)
        throws ConstraintViolationException,
        CanNotDoEditException,
        NotEnoughMemoryException,
        NonProjectablePotentialException,
        WrongCriterionException
    {
        if (!checkEvent(event)) {
            throw new ConstraintViolationException (
                                                    "ConstraintViolationException doing edition "
                                                            + event.getEdit ().getPresentationName ()
                                                            + " in probNet: "
                                                            + getMessage ());
        }
        
    }

    protected abstract String getMessage ();

    @Override
    public void undoEditHappened (PNUndoableEditEvent event)
    {
        // TODO Auto-generated method stub
        
    }

    /** @param probNet. <code>ProbNet</code>
	 * @return <code>true</code> if the <code>probNet</code> fulfills the 
	 * constraint. */
	public abstract boolean checkProbNet(ProbNet probNet);
	
	/** Make sure all editions of the event do not violate restrictions.
	 * @param probNet. <code>ProbNet</code>
	 * @param event <code>UndoableEditEvent</code>
     * @return <code>true</code> if the <code>ProbNet</code> will fulfill the
	 *  constraint after applying the <code>event</code> in a 
	 *  <code>ProbNet</code> that previously fulfilled the constraint. 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public abstract boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException;
	
	@Override
    public String toString() {
        return this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object paramObject)
    {
        return paramObject.getClass () == this.getClass ();
    }	
	
	
}
