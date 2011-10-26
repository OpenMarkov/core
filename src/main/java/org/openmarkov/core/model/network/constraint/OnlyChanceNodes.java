package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;


public class OnlyChanceNodes implements PNConstraint {

	// Attributes.
	private static OnlyChanceNodes constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private OnlyChanceNodes() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. <code>OnlyChanceNodes</code> */
	public static OnlyChanceNodes getUniqueInstance() {
		if (constraint == null) {
			constraint = new OnlyChanceNodes();
		}
		return constraint;
	}
	
	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> probNodes = probNet.getProbNodes();
		for (ProbNode probNode : probNodes) {
			if (probNode.getNodeType() != NodeType.CHANCE) {
				return false;
			}
		}
		return true;
	}

	/** Given a <code>probNet</code> that complies with this constraint, this
	 * method checks that after the application of the <code>edit</code> 
	 * contained in the <code>event</code> received, the 
	 * <code>probNet</code> continues complying with this constraint. 
	 * @param event <code>UndoableEditEvent</code>
	 * @throws ConstraintViolationException 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			if (((AddVariableEdit)edit).getNodeType() != NodeType.CHANCE) {
				throw new ConstraintViolationException(
					"ConstraintViolationException: only chance nodes allowed");
			}
		}
	}

	public void undoableEditHappened(UndoableEditEvent e) {
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			if (((AddVariableEdit)edit).getNodeType() != NodeType.CHANCE) {
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
