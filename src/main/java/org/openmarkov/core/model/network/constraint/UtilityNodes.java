package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import openmarkov.exceptions.CanNotDoEditException;
import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.networks.NodeType;
import openmarkov.networks.ProbNet;
import openmarkov.networks.ProbNode;
import openmarkov.networks.potentials.Potential;
import openmarkov.undo.PNUndoableEditEvent;


public class UtilityNodes implements PNConstraint {

	// Attributes.
	private static UtilityNodes un = null;
	
	// Constructor
	/** This constructor is private to not allow anyone to invoke it. */
	private UtilityNodes() {
	}
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>UtilityNodes</code> */
	public static UtilityNodes getUniqueInstance() {
		if (un == null) {
			un = new UtilityNodes();
		}
		return un;
	}
	
	@Override
	public boolean checkEvent(UndoableEditEvent event) {
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> utilityNodes = 
			probNet.getProbNodes(NodeType.UTILITY);
		int numUtilityNodes = utilityNodes.size();
		if (numUtilityNodes == 0) {
			return false;
		} else { // check same number of utility nodes and utility potentials
			ArrayList<Potential> potentials = probNet.getPotentials();
			int numUtilityPontentials = 0;
			for (Potential potential : potentials) {
				if (potential.isUtility()) {
					numUtilityPontentials++;
				}
			}
			return (numUtilityPontentials == numUtilityNodes);
		}
	}

	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException {
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
