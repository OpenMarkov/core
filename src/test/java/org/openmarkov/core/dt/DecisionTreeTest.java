package org.openmarkov.core.dt;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.factory.DANFactory;
import org.openmarkov.core.model.network.factory.IDFactory;

public class DecisionTreeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDecideTestID() {
		ProbNet decideTestID = IDFactory.buildIDDecideTest();
		
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestID);
		Assert.assertEquals(9.3929, decisionTree.getUtility(), 0.0001);
	}

	@Test
	public void testDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = DANFactory.buildDecideTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(decideTestDAN);
		Assert.assertEquals(9.3929, decisionTree.getUtility(), 0.0001);
	}
	
	@Test
	public void testDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = DANFactory.buildDatingDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(datingDAN);
		Assert.assertEquals(9.4076, decisionTree.getUtility(), 0.0001);
	}
	
	@Test
	public void testReactorDAN() throws NodeNotFoundException {
		ProbNet reactorDAN = DANFactory.buildReactorDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(reactorDAN);
		Assert.assertEquals(10.0627, decisionTree.getUtility(), 0.0001);
	}		

	@Test
	public void testDiabetesDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = DANFactory.buildDiabetesDAN();
		long startTime = System.nanoTime();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(diabetesDAN);
		Assert.assertEquals(9.8261, decisionTree.getUtility(), 0.0001);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
	}		
	
	@Test
	public void testTwoTestsDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = DANFactory.buildTwoTestDAN();
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(diabetesDAN);
		Assert.assertEquals(9.3324, decisionTree.getUtility(), 0.0001);
	}			
	
	@Test
	public void testNtests() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet nTestsDAN = DANFactory.buildNTestsDAN(3);
		DecisionTreeElement decisionTree = DecisionTreeBuilder.buildDecisionTree(nTestsDAN);
		double utility = decisionTree.getUtility();
		Assert.assertEquals(9.8066, utility, 0.0001);
	}
	
	@Test
	public void testEvalDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = DANFactory.buildDecideTestDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.3929, evaluator.getMEU(decideTestDAN), 0.0001);
	}
	
	@Test
	public void testEvalDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = DANFactory.buildDatingDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.4076, evaluator.getMEU(datingDAN), 0.0001);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
	}
	
	@Test
	public void testEvalReactorDAN() throws NodeNotFoundException {
		ProbNet reactorDAN = DANFactory.buildReactorDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(10.0627, evaluator.getMEU(reactorDAN), 0.0001);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
	}		

	@Test
	public void testEvalDiabetesDAN() throws NodeNotFoundException {
		ProbNet diabetesDAN = DANFactory.buildDiabetesDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		Assert.assertEquals(9.8261, evaluator.getMEU(diabetesDAN), 0.0001);
		System.out.println(" Execution time =" +ellapsedTime);
	}		
	
	@Test
	public void testEvalTwoTestsDAN() throws NodeNotFoundException {
		ProbNet twoTestsDAN = DANFactory.buildTwoTestDAN();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(9.3324, evaluator.getMEU(twoTestsDAN), 0.0001);
	}	
	
	@Test
	public void testEvalKingDAN() throws NodeNotFoundException {
		ProbNet wooerDAN = DANFactory.buildWooerDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		Assert.assertEquals(7.73, evaluator.getMEU(wooerDAN), 0.0001);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
	}	

	@Test
	public void testEvalNtests() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet nTestsDAN = DANFactory.buildNTestsDAN(3);
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		double meu =  evaluator.getMEU(nTestsDAN);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		Assert.assertEquals(9.8066, meu, 0.0001);
		System.out.println("MEU="+meu+" Execution time =" +ellapsedTime);
	}

	//Interesting test to keep but it takes to long to execute every time
	//@Test
	public void testEvalMediastiNetDAN() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {
		ProbNet mediastiNetDAN = DANFactory.buildMediastinetDAN();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		double meu = evaluator.getMEU(mediastiNetDAN);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		Assert.assertEquals(1.4710368294106826, meu, 0.000001);
		System.out.println("MEU="+meu+" Execution time =" +ellapsedTime);
	}	
	
	@Test
	public void testEvalUsedCarBuyerDAN() throws NodeNotFoundException, IncompatibleEvidenceException,
			UnexpectedInferenceException, NotEvaluableNetworkException {

		ProbNet usedCarBuyerDAN = DANFactory.buildUsedCarBuyer();
		long startTime = System.nanoTime();
		DecisionTreeEvaluator evaluator = new DecisionTreeEvaluator();
		double meu = evaluator.getMEU(usedCarBuyerDAN);
		long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println(" Execution time =" +ellapsedTime);
		Assert.assertEquals(32.96, meu, 0.0001);
	}
}
