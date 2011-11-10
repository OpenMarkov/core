package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;


public class AllChanceVariablesHaveChancePotentials implements PNConstraint {

	// Attributes.
	private static AllChanceVariablesHaveChancePotentials constraint = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private AllChanceVariablesHaveChancePotentials() {
	}

	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>AllChanceVariablesHaveChancePotentials</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new AllChanceVariablesHaveChancePotentials();
		}
		return constraint;
	}
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) {
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> chanceNodes = probNet.getProbNodes(NodeType.CHANCE);
		for (ProbNode chanceNode : chanceNodes) {
			Variable variable = chanceNode.getVariable();
			ArrayList<Potential> potentialsNode = chanceNode.getPotentials();
			boolean hasPotential = false;
			for (Potential potential : potentialsNode) {
				hasPotential = hasPotential || 
				    (potential.getVariables().get(0) == variable);
			}
			if (!hasPotential) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException chance variable without " + 
				"potential.");
		}
	}

	@Override
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
