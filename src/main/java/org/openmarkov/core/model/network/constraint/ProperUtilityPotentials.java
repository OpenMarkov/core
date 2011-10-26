package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveNodeEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.potential.Potential;


public class ProperUtilityPotentials implements PNConstraint {

	// Attributes.
	private static ProperUtilityPotentials constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private ProperUtilityPotentials() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. <code>ProperUtilityPotentials</code> */
	public static ProperUtilityPotentials getUniqueInstance() {
		if (constraint == null) {
			constraint = new ProperUtilityPotentials();
		}
		return constraint;
	}
	
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> utilityNodes = 
			probNet.getProbNodes(NodeType.UTILITY);
		if (utilityNodes.size() == 0) {
			return false;
		}
		for (ProbNode utilityNode : utilityNodes) {
			ArrayList<Potential> utilityPotentials =utilityNode.getPotentials();
			if ((utilityPotentials == null) || (utilityPotentials.size() == 0)){
				return false;
			}
		}
		return true;
	}

	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		ProbNet probNet = ((PNUndoableEditEvent)event).getProbNet();
		int numUtilities = probNet.getNumNodes(NodeType.UTILITY);
		for (PNEdit edit : edits) {
			if (((AddVariableEdit)edit).getNodeType() == NodeType.UTILITY) {
				numUtilities = numUtilities + 1;
			}
		}		
		edits = 
			UtilConstraints.getEditsType(event, RemoveNodeEdit.class);
		for (PNEdit edit : edits) {
			if (((RemoveNodeEdit)edit).getNodeType() == NodeType.UTILITY) {
				numUtilities = numUtilities - 1;
			}
		}		
		return (numUtilities > 0);
	}

	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException there is at least one utility " +
				"variable without utility potential or there are no utility " +
				"potentials");
		}
	}

	public void undoableEditHappened(UndoableEditEvent event) {
	}

	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
