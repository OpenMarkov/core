package org.openmarkov.core.dt;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

public class DecisionTreeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDecideTestID() {
		ProbNet decideTestID = NetsFactory
				.createInfluenceDiagramDecisionTestProblem(0.14, 0.91, 0.97);
		
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestID);
		Assert.assertEquals(94.312, decisionTree.getUtility(), 0.0001);
	}

	@Test
	public void testDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = buildDecideTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestDAN);
		Assert.assertEquals(94.312, decisionTree.getUtility(), 0.0001);
	}

	private ProbNet buildDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableY = new Variable("Y", "negative", "positive");
		Variable variableD = new Variable("D","no","yes");
		Variable variableT = new Variable("T","no","yes");
		Variable variableU1 = new Variable("U1");
		Variable variableU2 = new Variable("U2");
		
		ProbNode nodeX = decideTestDAN.addProbNode(variableX, NodeType.CHANCE);
		ProbNode nodeY = decideTestDAN.addProbNode(variableY, NodeType.CHANCE);
		ProbNode nodeU1 = decideTestDAN.addProbNode(variableU1, NodeType.UTILITY);
		ProbNode nodeU2 = decideTestDAN.addProbNode(variableU2, NodeType.UTILITY);
		ProbNode nodeD = decideTestDAN.addProbNode(variableD, NodeType.DECISION);
		decideTestDAN.addProbNode(variableT, NodeType.DECISION);
		
		decideTestDAN.getGraph().makeLinksExplicit(false);
		decideTestDAN.addLink(variableX, variableY, true);
		decideTestDAN.addLink(variableX, variableU1, true);
		decideTestDAN.addLink(variableD, variableY, true);
		decideTestDAN.addLink(variableD, variableT, true);
		decideTestDAN.addLink(variableD, variableU2, true);
		decideTestDAN.addLink(variableT, variableU1, true);
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialY = new TablePotential(Arrays.asList(variableY, variableD, variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialY.values = new double [] {0, 0, 0.97, 0.03, 0, 0, 0.09, 0.91};
		nodeY.setPotential(potentialY);
		
		TablePotential potentialU1 = new TablePotential(variableU1, Arrays.asList(variableX, variableT));
		potentialU1.values = new double [] {100, 30, 90, 80};
		nodeU1.setPotential(potentialU1);		
		
		TablePotential potentialU2 = new TablePotential(variableU1, Arrays.asList(variableD));
		potentialU2.values = new double [] {0, -2};
		nodeU2.setPotential(potentialU2);		
		
		Link link = decideTestDAN.getGraph().getLink(nodeD.getNode(), nodeY.getNode(), true);
		link.initializesRestrictionsPotential();
		TablePotential restrictionsPotential = (TablePotential)link.getRestrictionsPotential();
		restrictionsPotential.values = new double[]{0,1,0,1};
		
		link.setRevealingStates(Arrays.asList(variableD.getStates()[1]));
		
		return decideTestDAN;
	}
	
}
