package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;

import openmarkov.exceptions.CanNotDoEditException;
import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.exceptions.NonProjectablePotentialException;
import openmarkov.exceptions.NotEnoughMemoryException;
import openmarkov.exceptions.WrongCriterionException;
import openmarkov.graphs.Node;
import openmarkov.networks.ProbNet;
import openmarkov.undo.PNUndoableEditEvent;
import openmarkov.undo.edit.PNEdit;


public class OnlyUndirectedLinks implements PNConstraint {

	// Attributes.
	private static OnlyUndirectedLinks constraint = null;
	
	private String explanation;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private OnlyUndirectedLinks() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. <code>OnlyUndirectedLinks</code> */
	public static OnlyUndirectedLinks getUniqueInstance() {
		if (constraint == null) {
			constraint = new OnlyUndirectedLinks();
		}
		return constraint;
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
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, NotEnoughMemoryException, 
	NonProjectablePotentialException, WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding link in graph: " + 
						explanation + ". no directed links allowed");
		}		
	}
	
	/** This method is void in all <code>PNConstraint</code>s because 
	 * constraints are only interested in editions before its invocation. */
	public void undoableEditHappened(UndoableEditEvent event) {
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Node> nodes = probNet.getGraph().getNodes();
		for (Node node : nodes) {
			// Only check children because with this is enough
			// to look for directed links
			if (node.getChildren().size() != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		for (PNEdit edit : edits) {
			if (((AddLinkEdit)edit).isDirected()) {
				AddLinkEdit addLink = (AddLinkEdit)edit;
				explanation = new String(
					addLink.getVariable1() + " --> " + addLink.getVariable2());
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
