/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;

/**
 * <code>NodeStateEdit</code> is a simple edit that allow modify the states of
 * one node.
 * 
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 * 
 */
@SuppressWarnings("serial")
public class NodeStateEdit extends SimplePNEdit {
	// Default increment between in discretized intervals
	private final int increment = 2;
	/**
	 * The new state
	 */
	private State newState;
	/**
	 * The last state before the edition
	 */
	private State lastState = new State("");
	/**
	 * index of the state selected in the view
	 */
	private int stateSelected;
	/**
	 * The node that the stats belongs to
	 */
	private ProbNode probNode = null;
	/**
	 * The last potential before the edition
	 */
	private ArrayList<Potential> lastPotential;
	/**
	 * The action to carry out
	 */
	private StateAction stateAction;
	/**
	 * The last partitioned interval before the edition
	 */
	private PartitionedInterval currentPartitionedInterval;
	private State[] lastStates;

	private Map<Link, double[]> linkRestrictionMap;

	/**
	 * Creates a new <code>NodeStateEdit</code> to carry out the specified
	 * action on the specified state.
	 * 
	 * @param probNode
	 *            the node that will be edited.
	 * @param stateAction
	 *            the action to carry out
	 * @param indexState
	 *            the index (in the table) associated to the state to edit
	 * @param newState
	 *            a new string for the state edited if the action is ADD.
	 */
	public NodeStateEdit(ProbNode probNode, StateAction stateAction,
			int indexState, String newState) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.newState = new State(newState);
		this.stateSelected = probNode.getVariable().getNumStates()
				- (indexState + 1);
		this.lastPotential = probNode.getPotentials();
		this.stateAction = stateAction;
		this.currentPartitionedInterval = probNode.getVariable()
				.getPartitionedInterval();
		this.lastStates = probNode.getVariable().getStates().clone();
		this.linkRestrictionMap = new HashMap<Link, double[]>();
	}

	@Override
	public void doEdit() throws DoEditException {
		State[] newObjectState = null;
		ArrayList<Node> nodes;
		Potential uniformPotential;
		ArrayList<Potential> potentials;
		switch (stateAction) {
		case ADD:
			// assume that the new state is added in last position
			newObjectState = new State[probNode.getVariable().getNumStates() + 1];
			int i = 0;
			for (State states : probNode.getVariable().getStates()) {
				newObjectState[i] = states;
				i++;
			}
			newObjectState[i] = newState;
			probNode.getVariable().setStates(newObjectState);

			// set uniform potential for the edited node and children
			uniformPotential = PotentialOperations.getUniformPotential(probNet,
					probNode.getVariable(), probNode.getNodeType());
			potentials = new ArrayList<Potential>();
			potentials.add(uniformPotential);

			probNode.setPotentials(potentials);

			nodes = probNode.getNode().getChildren();
			for (Node node : nodes) {
				potentials = new ArrayList<Potential>();
				ProbNode child = (ProbNode) node.getObject();
				uniformPotential = PotentialOperations.getUniformPotential(
						probNet, child.getVariable(), child.getNodeType());
				potentials.add(uniformPotential);
				child.setPotentials(potentials);
			}
			// if the node is dicretized add a new row in partitionedInterval
			// field of the node
			if (probNode.getVariable().getVariableType() == VariableType.DISCRETIZED) {
				PartitionedInterval newPartitionedInterval = getNewPartitionedInterval();
				probNode.getVariable().setPartitionedInterval(
						newPartitionedInterval);
			}
			stateSelected++;
			resetLink(probNode.getNode());
			break;
		case REMOVE:
			newObjectState = new State[probNode.getVariable().getNumStates() - 1];
			int i1 = 0;
			boolean found = false;
			for (State states : probNode.getVariable().getStates()) {
				if (i1 != stateSelected || found == true) {
					newObjectState[i1] = states;
					i1++;
				} else
					found = true;
			}
			probNode.getVariable().setStates(newObjectState);

			// set uniform potential for the edited node and children
			uniformPotential = PotentialOperations.getUniformPotential(probNet,
					probNode.getVariable(), probNode.getNodeType());
			potentials = new ArrayList<Potential>();
			potentials.add(uniformPotential);

			probNode.setPotentials(potentials);

			nodes = probNode.getNode().getChildren();
			for (Node node : nodes) {
				potentials = new ArrayList<Potential>();
				ProbNode child = (ProbNode) node.getObject();
				uniformPotential = PotentialOperations.getUniformPotential(
						probNet, child.getVariable(), child.getNodeType());
				potentials.add(uniformPotential);
				child.setPotentials(potentials);
			}
			resetLink(probNode.getNode());
			break;
		case DOWN:
			if (stateSelected > 0) {
				State state = probNode.getVariable().getStates()[stateSelected - 1];
				probNode.getVariable().getStates()[stateSelected - 1] = probNode
						.getVariable().getStates()[stateSelected];
				probNode.getVariable().getStates()[stateSelected] = state;
				resetLink(probNode.getNode());
			}
			break;
		case UP:
			if (stateSelected < probNode.getVariable().getNumStates()) {
				State state = probNode.getVariable().getStates()[stateSelected + 1];
				probNode.getVariable().getStates()[stateSelected + 1] = probNode
						.getVariable().getStates()[stateSelected];
				probNode.getVariable().getStates()[stateSelected] = state;
				resetLink(probNode.getNode());
			}

			break;
		case RENAME:
			if (stateSelected >= 0
					&& stateSelected < probNode.getVariable().getNumStates()) {
				newObjectState = new State[probNode.getVariable()
						.getNumStates()];
				int j = 0;
				for (State states : probNode.getVariable().getStates()) {
					newObjectState[j] = states;
					j++;
				}
				newObjectState[stateSelected] = newState;
				probNode.getVariable().setStates(newObjectState);

			}
			break;

		}

	}

	@Override
	public void undo() {
		super.undo();
		ArrayList<Node> nodes;
		probNode.getVariable().setStates(lastStates);
		probNode.setUniformPotential();
		// Update children information
		nodes = probNode.getNode().getChildren();
		for (Node node : nodes) {
			ProbNode child = (ProbNode) node.getObject();
			child.setUniformPotential();
		}

		if (probNode.getVariable().getVariableType() == VariableType.DISCRETIZED) {
			probNode.getVariable().setPartitionedInterval(
					currentPartitionedInterval);
		}
		for (Link link : linkRestrictionMap.keySet()) {
			try {
				link.initializesRestrictionsPotential();
			TablePotential restrictionPotential = (TablePotential) link
					.getRestrictionsPotential();
			restrictionPotential.setValues(linkRestrictionMap.get(link));
			} catch (NotEnoughMemoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Gets the new state created if the action was ADD
	 * 
	 * @return the new state
	 */
	public State getNewState() {
		return newState;
	}

	/**
	 * Gets the new state created if the action was ADD
	 * 
	 * @return the new state
	 */
	public State getLastState() {
		return lastState;
	}

	public ProbNode getProbNode() {
		return probNode;
	}

	public StateAction getStateAction() {
		return stateAction;
	}

	/**
	 * This method add a new default subInterval, in the current
	 * PartitionedInterval object
	 * 
	 * @return The PartitionedInterval object with a new default subInterval
	 */

	private PartitionedInterval getNewPartitionedInterval() {
		double limits[] = currentPartitionedInterval.getLimits();
		double newLimits[] = new double[limits.length + 1];
		boolean belongsToLeftSide[] = currentPartitionedInterval
				.getBelongsToLeftSide();
		boolean newBelongsToLeftSide[] = new boolean[limits.length + 1];
		for (int i = 0; i < limits.length; i++) {
			newLimits[i] = limits[i];
			newBelongsToLeftSide[i] = belongsToLeftSide[i];
		}
		newLimits[limits.length] = currentPartitionedInterval.getMax()
				+ increment;
		newBelongsToLeftSide[limits.length] = false;
		return new PartitionedInterval(newLimits, newBelongsToLeftSide);
	}

	/****
	 * This method resets the link restriction of the links of the node
	 * 
	 * @param node
	 */
	private void resetLink(Node node) {
		for (Link link : node.getLinks()) {
			if (link.hasRestrictions()) {
				try {
					double[] lastPotential = ((TablePotential) link
							.getRestrictionsPotential()).values.clone();
					linkRestrictionMap.put(link, lastPotential);
					link.resetRestrictionsPotential();
				} catch (NotEnoughMemoryException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
