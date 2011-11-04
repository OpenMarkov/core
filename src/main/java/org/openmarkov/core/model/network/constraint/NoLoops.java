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

public class NoLoops implements PNConstraint {

	// Attributes.
	private static NoLoops constraint = null;

	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private NoLoops() {
	}

	// Methods
	/**
	 * Singleton pattern.
	 * 
	 * @return The unique instance. <code>DistinctVariableNames</code>
	 */
	public static NoLoops getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoLoops();
		}
		return constraint;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event)
	throws NotEnoughMemoryException, NonProjectablePotentialException,
	WrongCriterionException {
		ArrayList<PNEdit> edits = UtilConstraints.getEditsType(event,
				AddLinkEdit.class);
		ProbNet probNet = ((PNUndoableEditEvent) event).getProbNet();
		Graph graph = probNet.getGraph();
		for (PNEdit edit : edits) {
			Variable variable1 = ((AddLinkEdit) edit).getVariable1();
			Node node1 = probNet.getProbNode(variable1).getNode();
			Variable variable2 = ((AddLinkEdit) edit).getVariable2();
			Node node2 = probNet.getProbNode(variable2).getNode();
			if (graph.existsPath(node2, node1, false)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		Graph graph = probNet.getGraph();
		ArrayList<Node> nodesGraph = graph.getNodes();
		boolean probNetOK = true;
		boolean directed;
		for (Node node1 : nodesGraph) {
			ArrayList<Node> neighbors = node1.getNeighbors();
			for (Node node2 : neighbors) {
				if (node2.isChild(node1)) {
					graph.removeLink(node1, node2, true);
					directed = true;
				} else if (node1.isSibling(node2)) {
					graph.removeLink(node1, node2, false);
					directed = false;
				} else {
					continue;
				}
				if (graph.existsPath(node2, node1, false)) {
					probNetOK = false;
				}
				if (directed) {
					graph.addLink(node1, node2, true);
				} else {
					graph.addLink(node1, node2, false);
				}
				if (!probNetOK) {
					return probNetOK;
				}
			}
		}
		return probNetOK;
	}

	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
	throws ConstraintViolationException, CanNotDoEditException,
	NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
					"ConstraintViolationException adding link in probNet: "
							+ "no loops allowed");
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
