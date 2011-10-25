package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.UndoableEditEvent;

import openmarkov.exceptions.CanNotDoEditException;
import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.networks.NodeType;
import openmarkov.networks.ProbNet;
import openmarkov.networks.ProbNode;
import openmarkov.undo.PNUndoableEditEvent;


public class AtLeastOneEffectivenessPotential implements PNConstraint {

	// Attributes.
	private static AtLeastOneEffectivenessPotential constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private AtLeastOneEffectivenessPotential() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>AtLeastOneEffectivenessPotential</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new AtLeastOneEffectivenessPotential();
		}
		return constraint;
	}
	
	@Override
	/** This method has no sense because this constraint is only used to check
	 * the whole <code>ProbNet</code> before execute the algorithm. */
	public boolean checkEvent(UndoableEditEvent event) {
		return false;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> effectivenessNodes = 
			probNet.getProbNodes(NodeType.EFFECTIVENESS);
		if (effectivenessNodes.size() == 0) {
			return false;
		}
		for (ProbNode costNode : effectivenessNodes) {
			ArrayList<ProbNode> parents = 
				ProbNet.getProbNodesOfNodes(costNode.getNode().getParents());
			Iterator<ProbNode> i = parents.iterator();
			boolean chanceOrDecision = false;
			ProbNode parent;
			do {
				parent = i.next();
				NodeType nodeType = parent.getNodeType();
				chanceOrDecision = nodeType == NodeType.CHANCE || 
					nodeType == NodeType.DECISION;
			} while (!chanceOrDecision && i.hasNext());
			if (!chanceOrDecision) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** This method has no sense because this constraint is only used to check
	 * the whole <code>ProbNet</code> before execute the algorithm. */
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException {
	}

	@Override
	/** This method has no sense because this constraint is only used to check
	 * the whole <code>ProbNet</code> before execute the algorithm. */
	public void undoableEditHappened(UndoableEditEvent e) {
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
