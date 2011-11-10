package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.LinkEdit;
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
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "NoCycles", defaultBehavior = ConstraintBehavior.YES)
public class NoCycles implements PNConstraint {

	// Attributes.
	private static NoCycles constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private NoCycles() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>DistinctVariableNames</code> */
	public static NoCycles getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoCycles();
		}
		return constraint;
	}
	
	@Override
	public boolean checkProbNet(ProbNet probNet) {
		Graph graph = probNet.getGraph();
		ArrayList<Node> nodesGraph = graph.getNodes();
		for (Node parent : nodesGraph) {
			ArrayList<Node> children = parent.getChildren();
			for (Node child : children) {
				if (graph.existsPath(child, parent, true)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	/** @param event <code>UndoableEditEvent</code>
	 * @return <code>true</code> if <code>event</code> comply with this 
	 *   constraint */
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		ProbNet probNet = ((PNUndoableEditEvent)event).getProbNet();
		//int u=0;
		Graph graph = probNet.getGraph();
		for (PNEdit edit : edits) {
			if (((AddLinkEdit)edit).isDirected()) { // checks constraint
				Variable variable1 = ((AddLinkEdit)edit).getVariable1(); 
				Node node1 = probNet.getProbNode(variable1).getNode();
				Variable variable2 = ((AddLinkEdit)edit).getVariable2(); 
				Node node2 = probNet.getProbNode(variable2).getNode();
				if (graph.existsPath(node2, node1, true)) {
					return false;
				}
			}
		}
		ArrayList<PNEdit> edits2 = 
			UtilConstraints.getEditsType(event, LinkEdit.class);
		for (PNEdit edit : edits2) {
			if (((LinkEdit)edit).isDirected()) { // checks constraint
				Variable variable1 = ((LinkEdit)edit).getProbNode1().
					getVariable(); 
				Node node1 = ((LinkEdit)edit).getProbNode1().getNode();
				Node node2 = ((LinkEdit)edit).getProbNode2().getNode();
				if (graph.existsPath(node2, node1, true)) {
					return false;
				}
			}
		}
		return true;
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
	throws ConstraintViolationException, CanNotDoEditException, 
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding link in probNet: "+
				"no cycles allowed");
		}
	}

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