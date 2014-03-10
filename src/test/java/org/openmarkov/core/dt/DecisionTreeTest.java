package org.openmarkov.core.dt;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;

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
		ProbNet decideTestDAN = NetsFactory.buildDecideTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestDAN);
		Assert.assertEquals(94.312, decisionTree.getUtility(), 0.0001);
	}
	
	@Test
	public void testDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = NetsFactory.buildDatingDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(datingDAN);
		Assert.assertEquals(9.4076, decisionTree.getUtility(), 0.0001);
	}
	
	@Test
	public void testReactorDAN() throws NodeNotFoundException {
		ProbNet reactorDAN = NetsFactory.buildReactorDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(reactorDAN);
		Assert.assertEquals(8.76, decisionTree.getUtility(), 0.0001);
	}		

	@Test
	public void testDiabetesDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = NetsFactory.buildDiabetesDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(diabetesDAN);
		Assert.assertEquals(9.8261, decisionTree.getUtility(), 0.0001);
	}		
	
	@Test
	public void testTwoTestsDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = NetsFactory.buildTwoTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(diabetesDAN);
		Assert.assertEquals(9.3324, decisionTree.getUtility(), 0.0001);
	}			
	
}
