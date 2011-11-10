package org.openmarkov.core.action;

import javax.swing.event.UndoableEditListener;

import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;


public interface PNUndoableEditListener extends UndoableEditListener {

	/** An undoable edit will happen 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
    public void undoableEditWillHappen(PNUndoableEditEvent event) 
    throws ConstraintViolationException, CanNotDoEditException, 
    NotEnoughMemoryException, NonProjectablePotentialException, 
    WrongCriterionException;
    
    public void undoEditHappened(PNUndoableEditEvent event);
    
}
