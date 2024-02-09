/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.HashSet;
import java.util.Set;

public class LinkTest {
	private State[] stateA, stateB;
	private Variable varA, varB;
	private Node nodeA, nodeB;
	private Link<Node> link;

	@BeforeAll public void setUp() throws Exception {
		stateA = new State[] { new State("A1"), new State("A2"), new State("A3") };
		stateB = new State[] { new State("B1"), new State("B2") };
		varA = new Variable("A", stateA);
		varB = new Variable("B", stateB);
		ProbNet net = new ProbNet();
		nodeA = net.addNode(varA, NodeType.CHANCE);
		nodeB = net.addNode(varB, NodeType.CHANCE);
		link = new Link<>(nodeA, nodeB, true);
		link.initializesRestrictionsPotential();
	}

	@Test public void testRestrictionsPotential() {
		Assertions.assertTrue(link.hasRestrictions());
		Assertions.assertFalse(link.hasTotalRestriction());

		for (int i = 0; i < stateA.length; i++) {
			for (int j = 0; j < stateB.length; j++) {
				Assertions.assertEquals(1, link.areCompatible(stateA[i], stateB[j]));
			}
		}

		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		Assertions.assertEquals(0, link.areCompatible(stateA[0], stateB[0]));
		Assertions.assertFalse(link.hasTotalRestriction());
		link.setCompatibilityValue(stateA[0], stateB[0], 1);
		Assertions.assertEquals(1, link.areCompatible(stateA[0], stateB[1]));
		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		link.setCompatibilityValue(stateA[0], stateB[1], 0);
		Assertions.assertTrue(link.hasTotalRestriction());

		Set<State> statesRestrictTotally = link.getStatesRestrictTotally();
		Set<State> expectedStates = new HashSet<>();
		expectedStates.add(stateA[0]);
		Assertions.assertTrue(statesRestrictTotally.equals(expectedStates));
		link.setCompatibilityValue(stateA[2], stateB[0], 0);
		statesRestrictTotally = link.getStatesRestrictTotally();
		Assertions.assertTrue(statesRestrictTotally.equals(expectedStates));
		link.setCompatibilityValue(stateA[1], stateB[0], 0);
		statesRestrictTotally = link.getStatesRestrictTotally();
		Assertions.assertTrue(statesRestrictTotally.equals(expectedStates));
		link.setCompatibilityValue(stateA[2], stateB[1], 0);
		statesRestrictTotally = link.getStatesRestrictTotally();
		expectedStates.add(stateA[2]);
		Assertions.assertTrue(statesRestrictTotally.equals(expectedStates));

	}

	@Test public void testRevelationArc() {
		Assertions.assertFalse(link.hasRevealingConditions());
		link.addRevealingState(stateA[0]);
		Assertions.assertEquals(1, link.getRevealingStates().size());
		Assertions.assertTrue(link.getRevealingStates().contains(stateA[0]));
		link.removeRevealingState(stateA[0]);
		Assertions.assertFalse(link.getRevealingStates().contains(stateA[0]));
		Assertions.assertEquals(0, link.getRevealingIntervals().size());
	}

}
