package org.openmarkov.core.dt;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
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
		Assert.assertEquals(10.0627, decisionTree.getUtility(), 0.0001);
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
	
	@Test
	public void testNtests() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet nTestsDAN = NetsFactory.buildNTestsDAN(4);
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(nTestsDAN);
		System.out.println(decisionTree.getUtility());
	}
	
	@Test
	public void testEvalDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = NetsFactory.buildDecideTestDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(94.312, evaluator.getMEU(decideTestDAN), 0.0001);
	}
	
	@Test
	public void testEvalDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = NetsFactory.buildDatingDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.4076, evaluator.getMEU(datingDAN), 0.0001);
	}
	
	@Test
	public void testEvalReactorDAN() throws NodeNotFoundException {
		ProbNet reactorDAN = NetsFactory.buildReactorDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(10.0627, evaluator.getMEU(reactorDAN), 0.0001);
	}		

	@Test
	public void testEvalDiabetesDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = NetsFactory.buildDiabetesDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.8261, evaluator.getMEU(diabetesDAN), 0.0001);
	}		
	
	@Test
	public void testEvalTwoTestsDAN() throws NodeNotFoundException {
		ProbNet twoTestsDAN = NetsFactory.buildTwoTestDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.3324, evaluator.getMEU(twoTestsDAN), 0.0001);
	}	
	
	@Test
	public void testEvalWooerDAN() throws NodeNotFoundException {
		ProbNet wooerDAN = NetsFactory.buildWooerDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(7.73, evaluator.getMEU(wooerDAN), 0.0001);
	}	
	
	@Test
	public void testEvalNtests() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet nTestsDAN = NetsFactory.buildNTestsDAN(3);
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		System.out.println(evaluator.getMEU(nTestsDAN));
	}
	@Test
	public void testEvalMediastiNetDAN() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {
		ProbNet mediastiNetDAN = NetsFactory.buildMediastinetDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		double meu = evaluator.getMEU(mediastiNetDAN);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println("MEU="+meu+" Execution time =" +ellapsedTime);
	}	
	
	@Test
	public void testEvalUsedCarBuyerDAN() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet usedCarBuyerDAN = NetsFactory.buildUsedCarBuyer();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		double meu = evaluator.getMEU(usedCarBuyerDAN);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
		Assert.assertEquals(32.96, meu, 0.0001);
	}
}
