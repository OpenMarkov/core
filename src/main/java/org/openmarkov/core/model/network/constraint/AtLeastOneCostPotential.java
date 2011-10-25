package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;


/** This constraint ensures that exists at least one cost potential and all of 
 *  them are children of a chance or a decision node. */
public class AtLeastOneCostPotential implements PNConstraint {

	// Attributes.
	private static AtLeastOneCostPotential constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private AtLeastOneCostPotential() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. <code>AtLeastOneCostPotential</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new AtLeastOneCostPotential();
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
		ArrayList<ProbNode> costNodes = probNet.getProbNodes(NodeType.COST);
		if (costNodes.size() == 0) {
			return false;
		}
		for (ProbNode costNode : costNodes) {
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
	public void undoableEditHappened(UndoableEditEvent arg0) {
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

}
