package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

public class LinkRestrictionPotentialOperationsTest {
	private ProbNet net;
	private Variable varA, varB, varC;
	private State[] stateA, stateB, stateC;
	private Link<Node> link, link2;

	// private Node nodeA, nodeB, nodeC;

	@Before
	public void setUp() throws NodeNotFoundException {

		stateA = new State[] { new State("A1"), new State("A2"),
				new State("A3") };
		stateB = new State[] { new State("B1"), new State("B2") };
		stateC = new State[] { new State("C1"), new State("C2") };
		varA = new Variable("A", stateA);
		varB = new Variable("B", stateB);
		varC = new Variable("C", stateC);
		ArrayList<Variable> variables = new ArrayList<Variable>();

		variables.add(varB);
		variables.add(varA);
		variables.add(varC);
		net = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		net.addNode(varA, NodeType.CHANCE);
		net.addNode(varB, NodeType.CHANCE);
		net.addNode(varC, NodeType.CHANCE);
		Node node = new Node(net, varA, NodeType.CHANCE);
		net.addNode(node);
		Node nodeB = new Node(net, varB, NodeType.CHANCE);
		net.addNode(nodeB);

		node = new Node(net, varC, NodeType.CHANCE);
		net.addNode(node);
		net.makeLinksExplicit(true);
		net.addLink(varA, varB, true);
		net.addLink(varC, varB, true);
		TablePotential potential = new TablePotential(variables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.addPotential(potential);
		net.addPotential(potential);
		List<Link<Node>> links = net.getLinks();
		for (Link<Node> link : links) {
			if (link.getNode1().getVariable().equals(varA)) {
				this.link = link;
			}
			if (link.getNode1().getVariable().equals(varC)) {
				this.link2 = link;
			}

		}
		link.initializesRestrictionsPotential();
	}

	@Test
	public void testHasLinkRestriction() throws NodeNotFoundException {
		Assert.assertTrue(LinkRestrictionPotentialOperations
				.hasLinkRestriction(this.net.getNode("B")));
		Assert.assertFalse(LinkRestrictionPotentialOperations
				.hasLinkRestriction(this.net.getNode("A")));

	}

	@Test
	public void testGetParentLinksWithRestriction()
			throws NodeNotFoundException {
		Assert.assertEquals(1, LinkRestrictionPotentialOperations
				.getParentLinksWithRestriction(net.getNode("B")).size());
		Assert.assertEquals(0, LinkRestrictionPotentialOperations
				.getParentLinksWithRestriction(net.getNode("A")).size());
	}

	@Test
	public void testUpdatePotentialByAddLinkRestriction()
			throws NodeNotFoundException {

		TablePotential probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
				.updatePotentialByAddLinkRestriction(net.getNode("B"),
						(TablePotential) link.getRestrictionsPotential(), 0, 0);
		int[] statesIndices = new int[] { 0, 0, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);
		statesIndices = new int[] { 0, 0, 1 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

		statesIndices = new int[] { 1, 0, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 1);

		statesIndices = new int[] { 0, 1, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0.5);

		statesIndices = new int[] { 0, 2, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0.5);

		probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
				.updatePotentialByAddLinkRestriction(net.getNode("B"),
						(TablePotential) link.getRestrictionsPotential(), 1, 1);

		statesIndices = new int[] { 1, 1, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);
		statesIndices = new int[] { 1, 1, 1 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

	}

	@Test
	public void testUpdatePotentialByLinkRestrictions()
			throws NodeNotFoundException {
		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		link2.initializesRestrictionsPotential();
		link2.setCompatibilityValue(stateC[0], stateB[0], 0);

		TablePotential probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
				.updatePotentialByLinkRestrictions(net.getNode("B"));
		int[] statesIndices = new int[] { 0, 0, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

		statesIndices = new int[] { 0, 0, 1 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

		statesIndices = new int[] { 0, 1, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

		statesIndices = new int[] { 0, 2, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 0);

		statesIndices = new int[] { 1, 0, 0 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 1.0);

		statesIndices = new int[] { 1, 0, 1 };
		Assert.assertTrue(probabilityPotential.getValue(
				probabilityPotential.getVariables(), statesIndices) == 1.0);

	}

	@Test
	public void testGetStateCombinationsWithLinkRestriction() throws NodeNotFoundException {
		link.setCompatibilityValue(stateA[0], stateB[0], 0);
		link2.initializesRestrictionsPotential();
		link2.setCompatibilityValue(stateC[0], stateB[0], 0);
		List<int[]> states=LinkRestrictionPotentialOperations
				.getStateCombinationsWithLinkRestriction(net.getNode("B"));
		Assert.assertEquals(5, states.size());
	}

}
