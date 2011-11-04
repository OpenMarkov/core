package org.openmarkov.core.action;

import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;


/** An edition is one action defined over a Probabilistic Network. */
public interface PNEdit extends UndoableEdit {

	/** Puts into effect the edition. 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public void doEdit() throws DoEditException, NotEnoughMemoryException, 
		NonProjectablePotentialException, WrongCriterionException;
	public void setSignificant(boolean significant);
	
	
}
