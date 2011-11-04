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
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;


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
