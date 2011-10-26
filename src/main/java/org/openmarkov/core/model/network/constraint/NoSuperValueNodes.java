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


public class NoSuperValueNodes implements PNConstraint {

	// Attributes.
	private static NoSuperValueNodes constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	public NoSuperValueNodes() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>DistinctVariableNames</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoSuperValueNodes();
		}
		return constraint;
	}
	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding a super-value node.");
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> probNodes = probNet.getProbNodes();
		for (ProbNode probNode : probNodes) {
			NodeType nodeType = probNode.getNodeType();
			if ((nodeType == NodeType.SV_PRODUCT) || 
					(nodeType == NodeType.SV_SUM)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		// AddVariableEdit
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		for (PNEdit edit : edits) {
			NodeType nodeType = ((AddVariableEdit)edit).getNodeType();
			if ((nodeType == NodeType.SV_PRODUCT) || 
					(nodeType == NodeType.SV_SUM)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
