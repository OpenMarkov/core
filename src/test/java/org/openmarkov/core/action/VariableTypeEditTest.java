/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;


public class VariableTypeEditTest {

	private Node finiteStatesNode;
	private Node discretizedNode;
	private Node numericNode;
	private ProbNet probNet;

	private static ProbNet getProbNet4Test() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		// Variables
		Variable varA = new Variable("A", "absent", "mild", "moderate", "severe");
		Variable varB = new Variable("B", "yes", "its possible", "maybe not", "no");
		Variable varC = new Variable("C");

		// Nodes
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);

		nodeB.getVariable().setPartitionedInterval(new PartitionedInterval(nodeB.getVariable().getDefaultInterval(4),
				nodeB.getVariable().getDefaultBelongs(4)));

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeB, true);
		probNet.addLink(nodeA, nodeC, true);

		// Potentials
		UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeA.setPotential(potA);

		UniformPotential potB = new UniformPotential(Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.setPotential(potB);

		UniformPotential potC = new UniformPotential(Arrays.asList(varC, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeC.setPotential(potC);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	@BeforeAll public void setUp() throws Exception {
		probNet = getProbNet4Test();
		finiteStatesNode = probNet.getNode("A");
		discretizedNode = probNet.getNode("B");
		numericNode = probNet.getNode("C");
	}

	@Test public void testNumeric2Discretized() {
		State[] defaultStates = numericNode.getProbNet().getDefaultStates();
		State[] states = numericNode.getVariable().getStates().clone();
		PartitionedInterval currentInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
				.clone();
		VariableTypeEdit edit = new VariableTypeEdit(numericNode, VariableType.DISCRETIZED);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check the states of the node (if there is one)
		if (states.length != 1) {
			int i = 0;
			for (State state : states) {
				assertTrue(state == numericNode.getVariable().getStates()[i++]);
			}
		} else {
			// If numeric variable don't have any state, we check for default
			// states
			int i = 0;

			for (State state : defaultStates) {
				assertTrue(state.toString().equals(numericNode.getVariable().getStates()[i++].toString()));
			}
		}

		// Undo changes
		probNet.getPNESupport().undo();
		// Gest the undoed interval
		PartitionedInterval undoedInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
				.clone();

		// Check the number of intervals
		assertTrue(currentInterval.getNumSubintervals() == undoedInterval.getNumSubintervals());

		// Check the limits
		for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
			assertTrue(undoedInterval.getLimit(i) == currentInterval.getLimit(i));
		}

	}

	@Test public void testNumeric2FiniteStates() {
		State[] defaultStates = numericNode.getProbNet().getDefaultStates();
		State[] states = numericNode.getVariable().getStates().clone();
		PartitionedInterval currentInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
				.clone();
		VariableTypeEdit edit = new VariableTypeEdit(numericNode, VariableType.FINITE_STATES);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check the states of the node (if there is one)
		if (states.length != 1) {
			int i = 0;
			for (State state : states) {
				assertTrue(state == numericNode.getVariable().getStates()[i++]);
			}
		} else {
			// If numeric variable don't have any state, we check for default
			// states
			int i = 0;

			for (State state : defaultStates) {
				assertTrue(state.toString().equals(numericNode.getVariable().getStates()[i++].toString()));
			}
		}

		// Undo changes
		probNet.getPNESupport().undo();
		// Gest the undoed interval
		PartitionedInterval undoedInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
				.clone();

		// Check the number of intervals
		assertTrue(currentInterval.getNumSubintervals() == undoedInterval.getNumSubintervals());

		// Check the limits
		for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
			assertTrue(undoedInterval.getLimit(i) == currentInterval.getLimit(i));
		}
	}

	@Test public void testFiniteStates2Discretized() {
		State[] defaultStates = finiteStatesNode.getProbNet().getDefaultStates();
		State[] states = finiteStatesNode.getVariable().getStates().clone();

		VariableTypeEdit edit = new VariableTypeEdit(finiteStatesNode, VariableType.DISCRETIZED);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check the states of the node (if there is one)
		if (states.length != 1) {
			int i = 0;
			for (State state : states) {
				assertTrue(state == finiteStatesNode.getVariable().getStates()[i++]);
			}
		} else {
			// If numeric variable don't have any state, we check for default
			// states
			int i = 0;

			for (State state : defaultStates) {
				assertTrue(state == finiteStatesNode.getVariable().getStates()[i++]);
			}
		}
		// Check if the number of intervals is equals to the number of previous
		// states
		PartitionedInterval interval = (PartitionedInterval) finiteStatesNode.getVariable().getPartitionedInterval()
				.clone();
		assertTrue(states.length == finiteStatesNode.getVariable().getPartitionedInterval().getNumSubintervals());
		// Undo changes
		probNet.getPNESupport().undo();
	}

	@Test public void testFiniteStates2Numeric() {
		State[] defaultStates = finiteStatesNode.getProbNet().getDefaultStates();
		State[] states = finiteStatesNode.getVariable().getStates().clone();

		VariableTypeEdit edit = new VariableTypeEdit(finiteStatesNode, VariableType.NUMERIC);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check the states of the node (if there is one)
		if (states.length != 1) {
			int i = 0;
			for (State state : states) {
				assertTrue(state == finiteStatesNode.getVariable().getStates()[i++]);
			}
		} else {
			// If numeric variable don't have any state, we check for default
			// states
			int i = 0;

			for (State state : defaultStates) {
				assertTrue(state == finiteStatesNode.getVariable().getStates()[i++]);
			}
		}
		// Check if the number of intervals is equals to one
		assertTrue(1 == finiteStatesNode.getVariable().getPartitionedInterval().getNumSubintervals());
		// Undo changes
		probNet.getPNESupport().undo();
	}

	@Test public void testDiscretized2FiniteStates() {
		State[] states = discretizedNode.getVariable().getStates().clone();
		PartitionedInterval currentInterval = (PartitionedInterval) discretizedNode.getVariable()
				.getPartitionedInterval().clone();
		VariableTypeEdit edit = new VariableTypeEdit(discretizedNode, VariableType.FINITE_STATES);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check that the number of states must be different than one
		assertTrue(states.length != 1);

		int j = 0;
		for (State state : states) {
			assertTrue(state == discretizedNode.getVariable().getStates()[j++]);
		}

		// Undo changes
		probNet.getPNESupport().undo();
		// Gest the undoed interval
		PartitionedInterval undoedInterval = (PartitionedInterval) discretizedNode.getVariable()
				.getPartitionedInterval().clone();

		// Check the number of intervals
		assertTrue(currentInterval.getNumSubintervals() == undoedInterval.getNumSubintervals());

		// Check the limits
		for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
			assertTrue(undoedInterval.getLimit(i) == currentInterval.getLimit(i));
		}
	}

	@Test public void testDiscretized2Numeric() {
		State[] states = discretizedNode.getVariable().getStates().clone();
		PartitionedInterval currentInterval = (PartitionedInterval) discretizedNode.getVariable()
				.getPartitionedInterval().clone();
		VariableTypeEdit edit = new VariableTypeEdit(discretizedNode, VariableType.DISCRETIZED);
		try {
			probNet.getPNESupport().withUndo = true;
			probNet.doEdit(edit);

		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		// Check that the number of states must be different than one
		assertTrue(states.length != 1);

		int j = 0;
		for (State state : states) {
			assertTrue(state == discretizedNode.getVariable().getStates()[j++]);
		}

		// Undo changes
		probNet.getPNESupport().undo();
		// Gest the undoed interval
		PartitionedInterval undoedInterval = (PartitionedInterval) discretizedNode.getVariable()
				.getPartitionedInterval().clone();

		// Check the number of intervals
		assertTrue(currentInterval.getNumSubintervals() == undoedInterval.getNumSubintervals());

		// Check the limits
		for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
			assertTrue(undoedInterval.getLimit(i) == currentInterval.getLimit(i));
		}
	}
}
