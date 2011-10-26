package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


public class NoSelfLoops implements PNConstraint {

	// Attributes.
	private static NoSelfLoops constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private NoSelfLoops() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>DistinctVariableNames</code> */
	public static NoSelfLoops getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoSelfLoops();
		}
		return constraint;
	}
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		for (PNEdit edit : edits) {
			Variable variable1 = ((AddLinkEdit)edit).getVariable1(); 
			Variable variable2 = ((AddLinkEdit)edit).getVariable2(); 
			if (variable1 == variable2) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		Graph graph = probNet.getGraph();
		ArrayList<Node> nodes = graph.getNodes();
		for (Node node : nodes) {
			if (node.isChild(node) || node.isSibling(node)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding link in probNet: "+
				"no self loops allowed.");
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
	}

	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
