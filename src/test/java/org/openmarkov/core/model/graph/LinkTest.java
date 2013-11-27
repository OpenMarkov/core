package org.openmarkov.core.model.graph;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

public class LinkTest {
	private State[] stateA, stateB;
	private Variable varA, varB;
	private Node nodeA, nodeB;
	private Link link;

	@Before
	public void setUp() throws Exception {
		Graph graph = new Graph();
		stateA = new State[] { new State("A1"), new State("A2"),
				new State("A3") };
		stateB = new State[] { new State("B1"), new State("B2") };
		varA = new Variable("A", stateA);
		varB = new Variable("B", stateB);
		ProbNet net = new ProbNet();
		nodeA = new Node(graph, new ProbNode(net, varA, NodeType.CHANCE));
		nodeB = new Node(graph, new ProbNode(net, varB, NodeType.CHANCE));
		link = new Link(nodeA, nodeB, true);
		link.initializesRestrictionsPotential();
	}

	@Test
	public void testRestrictionsPotential() {
		Assert.assertTrue(link.hasRestrictions());
		Assert.assertFalse(link.hasTotalRestriction());

		for (int i = 0; i < stateA.length; i++) {
			for (int j = 0; j < stateB.length; j++) {
				Assert.assertEquals(1, link.areCompatible(stateA[i], stateB[j]));
			}
		}

		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		Assert.assertEquals(0, link.areCompatible(stateA[0], stateB[0]));
		Assert.assertFalse(link.hasTotalRestriction());
		link.setCompatibilityValue(stateA[0], stateB[0], 1);
		Assert.assertEquals(1, link.areCompatible(stateA[0], stateB[1]));
		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		link.setCompatibilityValue(stateA[0], stateB[1], 0);
		Assert.assertTrue(link.hasTotalRestriction());
	}

	@Test
	public void testRevelationArc() {
		Assert.assertFalse(link.hasRevealingConditions());
		link.addRevealingState(stateA[0]);
		Assert.assertEquals(1, link.getRevealingStates().size());
		Assert.assertTrue(link.getRevealingStates().contains(stateA[0]));
		link.removeRevealingState(stateA[0]);
		Assert.assertFalse(link.getRevealingStates().contains(stateA[0]));
		Assert.assertEquals(0, link.getRevealingIntervals().size());
	}

}
